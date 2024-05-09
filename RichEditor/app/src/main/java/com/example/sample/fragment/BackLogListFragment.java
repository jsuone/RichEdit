package com.example.sample.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.donkingliang.groupedadapter.adapter.GroupedRecyclerViewAdapter;
import com.donkingliang.groupedadapter.holder.BaseViewHolder;
import com.example.alarmclock.BacklogCompileActivity;
import com.example.alarmclock.BacklogSetting;
import com.example.alarmclock.Group;
import com.example.sample.Impl.Observer;
import com.example.sample.Impl.SyncManageAction;
import com.example.sample.adapter.GroupedListAdapter;
import com.example.sample.R;
import com.example.sample.util.ACache;
import com.example.sample.util.Constant;
import com.example.sample.util.DataManager;
import com.example.sample.util.MyDatabaseHelper;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class BackLogListFragment extends Fragment implements View.OnClickListener, Observer<Group.Item> {
    private RecyclerView recyclerView;
    private List<Group> groupList;
    private ImageButton backlogAdd;
    private MyDatabaseHelper databaseHelper;
    private Gson gson = new Gson();
    private ACache aCache;
    GroupedListAdapter adapter;
    ActivityResultLauncher<Intent> intentActivityResultLauncher;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
      View view  = inflater.inflate(R.layout.fragment_backlog_list,container,false);
      databaseHelper = MyDatabaseHelper.getInstance(getContext());
      recyclerView = view.findViewById(R.id.clock_rv);
      backlogAdd = view.findViewById(R.id.backlog_add);
      backlogAdd.setOnClickListener(this);
      DataManager.getInstance().registerObserver(Constant.Clock_ALL_KEY,this);
      initData();
      adapter = new GroupedListAdapter(getContext(),groupList);
      recyclerView.setAdapter(adapter);
      recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
      adapter.setOnChildClickListener(new GroupedRecyclerViewAdapter.OnChildClickListener() {
          @Override
          public void onChildClick(GroupedRecyclerViewAdapter adapter, BaseViewHolder holder, int groupPosition, int childPosition) {
              //打开编辑界面
              GroupedListAdapter adapter1 = (GroupedListAdapter) adapter;
              List<Group> list = adapter1.getGroupArrayList();
              Intent intent = new Intent(getActivity(),BacklogCompileActivity.class);
              intent.putExtra("itemdata",gson.toJson(list.get(groupPosition).getItems().get(childPosition)));
              intentActivityResultLauncher.launch(intent);
          }
      });
      return view;
    }
    void initData(){
        List<Group.Item> allItems = databaseHelper.queryAllBacklogList();
      groupList = getClockData(allItems);

      aCache = ACache.get(getContext());
         intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                if(result.getResultCode() == Activity.RESULT_OK){
                    //增加
                    Intent intent = result.getData();
                    Group.Item item = gson.fromJson(intent.getStringExtra("result"), Group.Item.class);
                    int ssn = (int) aCache.getAsObject("user_ssn");
                    item.setSsn(ssn);
                    ssn++;
                    aCache.put("user_ssn",ssn);
                    item.setUserGuid(aCache.getAsString("user_name"));
                    insertItemToGroup(item);
                    //提醒的定时器早就设定好了，只需要数据更新就行 过时 已经完成重复才需要列表变化 筛选出过时重复的，新建一条数据，设定定时器，其余一样加入列表数据

                    adapter.setGroups(groupList);
                }else if(result.getResultCode() == Activity.RESULT_CANCELED){
                    //修改
                    Intent intent = result.getData();
                    Group.Item item = gson.fromJson(intent.getStringExtra("result"), Group.Item.class);
                    int ssn = (int) aCache.getAsObject("user_ssn");
                    item.setSsn(ssn);
                    ssn++;
                    aCache.put("user_ssn",ssn);
                    updateItemOfGroup(null,null,item);
                    adapter.setGroups(groupList);

                }else if(result.getResultCode() == Activity.RESULT_FIRST_USER){
                    return;
                }

                SyncManageAction action = (SyncManageAction) getContext();
                action.SyncStart();
            }
        });
    }
    public void insertItemToGroup(Group.Item item){
        addItemToGroup(item);
        databaseHelper.insertBacklog(item);
        addItemToGroup(item,groupList);
    }
    public void updateItemOfGroup(String uuid, Boolean change,Group.Item data){
        //更新或者添加数据到集合，第三个参数可空，当是创建可以传入数据便于添加 过时 已经完成重复才需要列表变化 待完成 重复不需要
        Group.Item item;
        if(data==null){//如果是操作导致显示列表的变化，设定时间内容没有改变
            item =  findAndRemoveItemByUUID(uuid,groupList);//先移除当前元素
            item.setChecked(change);
        }else {//如果是对提醒进行编辑界面，以item数据为基础

            item = data;
            findAndRemoveItemByUUID(item.getUUID(), groupList);
        }
        addItemToGroup(item);
        databaseHelper.updateBacklog(item);
        addItemToGroup(item,groupList);
    }
    public void addItemToGroup(Group.Item item){//这是对数据进行处理再添加 比如重复按键需要再创建一个加入数据库之类的处理 但是item不会进行放入数据库因为不知道创建还是修改
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currentTime = new Date();

        item.setUpdateTime(format.format(currentTime));
        int ssn = (int) aCache.getAsObject("user_ssn");
        item.setSsn(ssn);
        ssn++;
        aCache.put("user_ssn",ssn);


        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if(item.getRepeat()){
            Date parse = null;
            try {
                parse = fmt.parse(item.getBacklog_time());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if(item.isChecked()|| currentTime.after(parse)) {
                //如果重复提醒 创建一个新的元素
                Group.Item repeatItem = new Group.Item(item);
                repeatItem.setUUID(UUID.randomUUID().toString());
                //重复时候 如果设定时间过期很久 先加到现在，时间还是过期换明天 新的元素时间往后以一天

                try {
                    Date newDate = fmt.parse(repeatItem.getBacklog_time());
                    Calendar calendar = Calendar.getInstance();
                    Calendar currentCalendar = Calendar.getInstance();
                    currentCalendar.setTime(currentTime);
                    // 将 newDate 设置为 Calendar 对象的时间
                    calendar.setTime(newDate);
                    //将时间拉倒现在的年月日 再如果过期 再加一天 防止时间差距过大 只加一天不够
                    calendar.set(Calendar.YEAR,currentCalendar.get(Calendar.YEAR));
                    calendar.set(Calendar.MONTH,currentCalendar.get(Calendar.MONTH));
                    calendar.set(Calendar.DAY_OF_MONTH,currentCalendar.get(Calendar.DAY_OF_MONTH));
                    if(!calendar.after(currentCalendar)){
                        //如果依然过期
                        // 将 Calendar 对象的日期向后移动一天
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    // 获取移动后的日期
                    Date newDateAfterOneDay = calendar.getTime();
                    repeatItem.setBacklog_time(fmt.format(newDateAfterOneDay));
                    repeatItem.setChecked(false);//设置为待完成 防止复制已经完成的
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //设置提醒
                BacklogSetting.setClock(repeatItem, getContext());
                //前一个设置为不重复
                item.setRepeat(false);
                addItemToGroup(repeatItem, groupList);
                databaseHelper.insertBacklog(repeatItem);

            }
        }
    }
    public void addItemToGroup(String uuid, Boolean change,Boolean extraTime)  {//通知完成的回调 列表刷新因为不会有待完成 重复的情况所有直接按重复划分情况
     Group.Item item =  findAndRemoveItemByUUID(uuid,groupList);//先移除当前元素
        item.setChecked(change);
        addItemToGroup(item);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
         //如果当前提醒数据不延迟
         if(extraTime){
             //延迟
             Date newDate = new Date();

             Calendar calendar = Calendar.getInstance();

             // 将 newDate 设置为 Calendar 对象的时间
             calendar.setTime(newDate);

             // 将 Calendar 对象的日期向后移动十分钟
             calendar.add(Calendar.MINUTE, 10);

             // 获取移动后的日期
             Date newDateAfterOneDay = calendar.getTime();
             item.setBacklog_time(fmt.format(newDateAfterOneDay));
             item.setChecked(false);//保证未选中
             //重新定时
             BacklogSetting.setClock(item,getContext());
         }
         //数据库修改
        databaseHelper.updateBacklog(item);
         addItemToGroup(item,groupList);
         adapter.setGroups(groupList);
        SyncManageAction action = (SyncManageAction) getContext();
        action.SyncStart();
    }
    public void deleteItemData(String uuid){
        Group.Item item = findAndRemoveItemByUUID(uuid,groupList);//删除fragment中的数据
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currentTime = new Date();

        item.setUpdateTime(format.format(currentTime));
        int ssn = (int) aCache.getAsObject("user_ssn");
        item.setSsn(ssn);
        ssn++;
        aCache.put("user_ssn",ssn);

        //删除定时器
        BacklogSetting.removePendingIntent(item.getUUID(),getContext());
        //数据库更新
        databaseHelper.deleteBacklogByUpdateStateDel(item);


    }
    private Group.Item findAndRemoveItemByUUID(String UUID, List<Group> groups) {
        // 遍历所有的分组
        for (Group group : groups) {
            // 获取分组中的项的迭代器
            Iterator<Group.Item> iterator = group.getItems().iterator();
            // 遍历每个分组中的项
            while (iterator.hasNext()) {
                Group.Item item = iterator.next();
                // 如果找到匹配的 UUID，从集合中移除该项并返回
                if (item.getUUID().equals(UUID)) {
                    iterator.remove(); // 从集合中移除当前项
                    return item; // 返回匹配的项
                }
            }
        }
        // 如果未找到匹配的项，返回 null
        return null;
    }

    private List<Group> getClockData(List<Group.Item> allItems){
        //List<Group.Item> allItems = databaseHelper.queryAllBacklogList();
        //分为未完成 过期 以及完成

// 创建三个分组
        Group unfinishedGroup = new Group("待完成", new ArrayList<>());
        Group finishedGroup = new Group("已完成", new ArrayList<>());
        Group expiredGroup = new Group("过期", new ArrayList<>());

// 获取当前时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date currentDate = new Date();

// 格式化当前时间
        String currentTime = sdf.format(currentDate);
        // 遍历所有 Item 对象，并根据属性放入相应的分组中
        for (Group.Item item : allItems) {
            try {
                // 将数据库中的 createTime 字符串解析为 Date 对象

                Date createTime = sdf.parse(item.getBacklog_time());

                // 判断项目是否过期
                if (!item.isChecked()) {
                    // 未完成
                    if (createTime.after(currentDate)) {
                        unfinishedGroup.getItems().add(item);
                    } else {
                       expiredGroup.getItems().add(item);
                    }
                }else{
                    finishedGroup.getItems().add(item);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
// 将分组添加到分组列表中
        List<Group> groups = new ArrayList<>();
        groups.add(unfinishedGroup);
        groups.add(finishedGroup);
        groups.add(expiredGroup);

        List<Group.Item> unfinishItem = unfinishedGroup.getItems();
        for (Group.Item item : unfinishItem) {
            BacklogSetting.setClock(item,getContext());
        }
        return groups;

    }
    private void addItemToGroup(Group.Item item, List<Group> groups) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date currentDate = new Date();
            Date createTime = sdf.parse(item.getBacklog_time());
            if (!item.isChecked()) {
                // 未完成
                if (createTime.after(currentDate)) {
                    findOrCreateGroup("待完成", groups).getItems().add(item);
                } else {
                    findOrCreateGroup("过期", groups).getItems().add(item);
                }
            } else {
                // 已完成
                findOrCreateGroup("已完成", groups).getItems().add(item);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    private Group findOrCreateGroup(String groupName, List<Group> groups) {
        for (Group group : groups) {
            if (group.getName().equals(groupName)) {
                return group;
            }
        }
        // 如果找不到相应的组，则创建一个新组并添加到列表中
        Group newGroup = new Group(groupName, new ArrayList<>());
        groups.add(newGroup);
        return newGroup;
    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.backlog_add:
                Intent intent = new Intent(getContext(), BacklogCompileActivity.class);
            intentActivityResultLauncher.launch(intent);
                break;
            default: break;
        }
    }

    @Override
    public void onDataChanged(List<Group.Item> newData) {
        if(newData!=null&&newData.size()!=0){
            groupList = getClockData(newData);
            adapter.setGroups(groupList);
        }

    }
}
