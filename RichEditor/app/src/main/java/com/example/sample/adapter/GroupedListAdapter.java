package com.example.sample.adapter;

import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.util.Log;
import android.view.MenuItem;
import android.widget.PopupMenu;

import com.donkingliang.groupedadapter.adapter.GroupedRecyclerViewAdapter;
import com.donkingliang.groupedadapter.holder.BaseViewHolder;
import com.example.alarmclock.Backlog;
import com.example.alarmclock.BacklogSetting;
import com.example.alarmclock.Group;
import com.example.alarmclock.MyJobService;
import com.example.sample.Impl.OnClockDataChangeListener;
import com.example.sample.Impl.OnNoteThemeSelectedListener;
import com.example.sample.Impl.SyncManageAction;
import com.example.sample.R;
import com.example.sample.activity.AddNoteActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GroupedListAdapter extends GroupedRecyclerViewAdapter {
    protected List<Group> groupArrayList;
    static String TAG = "GroupedListAdapter";
    public GroupedListAdapter(Context context, List<Group> groupArrayList) {
        super(context);
        this.groupArrayList = groupArrayList;
    }

    public GroupedListAdapter(Context context, boolean useBinding) {
        super(context, useBinding);
    }

    public List<Group> getGroupArrayList(){return  groupArrayList;}
    @Override
    public int getGroupCount() {
        return groupArrayList==null?0:groupArrayList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<Group.Item> items = groupArrayList.get(groupPosition).getItems();
        return items==null?0:items.size();
    }

    @Override
    public boolean hasHeader(int groupPosition) {
        return true;
    }

    @Override
    public boolean hasFooter(int groupPosition) {
        return false;
    }

    @Override
    public int getHeaderLayout(int viewType) {
        return com.example.alarmclock.R.layout.title_group;
    }

    @Override
    public int getFooterLayout(int viewType) {
        return 0;
    }
    public void setGroups(List<Group> groups){
        groupArrayList = groups;
        notifyDataChanged();
    }
    @Override
    public int getChildLayout(int viewType) {
        return com.example.alarmclock.R.layout.item_group;
    }

    @Override
    public void onBindHeaderViewHolder(BaseViewHolder holder, int groupPosition) {
        Group group = groupArrayList.get(groupPosition);
        holder.setText(com.example.alarmclock.R.id.backlog_title,group.getName());
    }

    @Override
    public void onBindFooterViewHolder(BaseViewHolder holder, int groupPosition) {

    }

    @Override
    public void onBindChildViewHolder(BaseViewHolder holder, int groupPosition, int childPosition) {
        Group.Item item = groupArrayList.get(groupPosition).getItems().get(childPosition);
        Backlog backlog = holder.get(com.example.alarmclock.R.id.backlog_item);
        backlog.setClockRemarkView(item.getBacklog_name());
        backlog.setClockTimeView(item.getBacklog_time());
        backlog.setClockIsEndView(item.isChecked());
        backlog.setOnCheckedClickListener(new Backlog.OnCheckedClickListener() {
            @Override
            public void onChecked(boolean isChecked) {
                Log.d(TAG, "是否选择" + isChecked);
                item.setChecked(isChecked);
                if (isChecked) {
                    //提醒已经完成 移除定时 如果是重复 创建一个新元素
                    BacklogSetting.removePendingIntent(item.getUUID(), mContext);
                    //触发移动动画效果 以及 提醒的设置与退出
                    groupArrayList.get(groupPosition).getItems().remove(childPosition);
                    //notifyChildRemoved(groupPosition, childPosition);
                    notifyItemRemoved(holder.getAdapterPosition());
                    groupArrayList.get(1).getItems().add(item);
                    notifyChildInserted(1, groupArrayList.get(1).getItems().size() - 1);

                    //list数据更新 数据库更新
                    OnClockDataChangeListener listener = (OnClockDataChangeListener) mContext;
                    listener.itemDataChangeListener(item.getUUID(), isChecked);

                } else {
                    //设置提醒未完成
                    BacklogSetting.setClock(item, mContext);
                    groupArrayList.get(groupPosition).getItems().remove(childPosition);
                    notifyChildRemoved(groupPosition, childPosition);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    try {
                        Date currentDate = new Date();
                        Date createTime = sdf.parse(item.getBacklog_time());
                        if (createTime.after(currentDate)) {
                            groupArrayList.get(0).getItems().add(item);
                            notifyChildInserted(0, groupArrayList.get(1).getItems().size() - 1);
                        } else {
                            groupArrayList.get(2).getItems().add(item);
                            notifyChildInserted(2, groupArrayList.get(1).getItems().size() - 1);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    //list数据更新 数据库更新
                    OnClockDataChangeListener listener = (OnClockDataChangeListener) mContext;
                    listener.itemDataChangeListener(item.getUUID(), isChecked);
                }
                SyncManageAction action = (SyncManageAction) mContext;
                action.SyncStart();
            }
        });
        backlog.setMoreOptionListener(v->{
            PopupMenu popupMenu = new PopupMenu(mContext, v);
            popupMenu.getMenuInflater().inflate(R.menu.clock_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem im) {
                    // 处理菜单项的点击事件
                    switch (im.getItemId()) {
                        case R.id.menu_clock_del:
                            // 处理删除按钮的点击事件 删除笔记
                            //groupArrayList.get(groupPosition).getItems().remove(childPosition);
                           //因为保持的数据源引用和fragment是同一个，所以调用接口方法进行数据改变并更新数据库再更新列表动画
                            OnClockDataChangeListener listener = (OnClockDataChangeListener) mContext;

                            listener.deleteItemDataLinstener(item.getUUID());
                            //notifyChildRemoved(groupPosition,childPosition);由于删除后其他元素位置也变化，但是这里得到的位置没有同步变化 采用原生的重新计算位置
                            notifyItemRemoved(holder.getAdapterPosition());
                            SyncManageAction action = (SyncManageAction) mContext;
                            action.SyncStart();
                            return true;
                        default:
                            return false;
                    }
                }
            });
            popupMenu.show();
        });
    }

    @Override
    public void setOnHeaderClickListener(OnHeaderClickListener listener) {
        super.setOnHeaderClickListener(listener);
    }

    @Override
    public void setOnChildClickListener(OnChildClickListener listener) {
        super.setOnChildClickListener(listener);
    }
    public Group getGroupForPosition(int position){
        return  groupArrayList.get(position);
    }
    void updateListData(Intent intent){
        int groupid = intent.getIntExtra("GID",0);
        int itemid = intent.getIntExtra("UUID",0);
     Group.Item item =  groupArrayList.get(groupid).getItems().
                get(itemid);
     item.setBacklog_name(intent.getStringExtra("title"));
     item.setBacklog_time(intent.getStringExtra("time"));
     item.setChecked(intent.getBooleanExtra("isChecked",false));
     notifyChildChanged(groupid,itemid);
    }
}
