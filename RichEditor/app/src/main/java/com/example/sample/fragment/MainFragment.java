package com.example.sample.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sample.Impl.Observer;
import com.example.sample.Impl.OnNoteListChangeListener;
import com.example.sample.Impl.SyncManageAction;
import com.example.sample.adapter.NoteListAdapter;
import com.example.sample.util.ACache;
import com.example.sample.util.Constant;
import com.example.sample.util.DataManager;
import com.example.sample.util.ImageUtils;
import com.example.sample.util.MyDatabaseHelper;
import com.example.sample.model.NoteModel;
import com.example.sample.R;
import com.example.sample.activity.AddNoteActivity;
import com.example.sample.widget.RefreshableView;
import com.google.gson.Gson;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class MainFragment extends Fragment implements Observer<NoteModel> {
    private ImageButton nv_btn;
    private ImageButton toolbar_add_note;
    private DrawerLayout drawerLayout;
    private MyDatabaseHelper databaseHelper;
    private RecyclerView noteList;
    private SmartRefreshLayout refreshableView;
    private ACache aCache;
    private Gson gson;
    private List<String> titleList;//当前选中主题
    ActivityResultLauncher<Intent> intentActivityResultLauncher;
    NoteListAdapter noteListAdapter;
    private Map<String, List<NoteModel>>  noteMap;//不存储主要内容，只存储标题等小数据 每点击一个数据项再把对应笔记的内容加载到缓存中
    //本类的数据是所有笔记的数据，一般的删除增加修改，引用集合没变，不需要使用notify提醒，如果是同步需要才使用提醒数据更新
    public void initData(DrawerLayout drawerLayout){
        this.drawerLayout = drawerLayout;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main,container,false);
        nv_btn = view.findViewById(R.id.nv_btn);
        noteList = view.findViewById(R.id.note_list);
        toolbar_add_note = view.findViewById(R.id.toolbar_add_note);
        refreshableView = view.findViewById(R.id.refreshable_view);//下拉刷新
        refreshableView.setRefreshHeader(new ClassicsHeader(getContext()));
        refreshableView.setRefreshFooter(new ClassicsFooter(getContext()));
        databaseHelper = MyDatabaseHelper.getInstance(getContext());
        aCache = ACache.get(getContext());
        gson = new Gson();
        //注册观察者
        DataManager.getInstance().registerObserver(Constant.Note_ALL_KEY,this);
        initView();
        getNoteData();//得到数据
        noteListAdapter = new NoteListAdapter(getContext(),new ArrayList<>(),intentActivityResultLauncher);
        noteList.setAdapter(noteListAdapter);
        noteList.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }
    public void initView(){
        nv_btn.setOnClickListener(v->{
            if(drawerLayout!=null){
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                if(result.getResultCode() == Constant.NOTE_ADD_FINISH){
                    Intent intent = result.getData();
                    NoteModel data =gson.fromJson( intent.getStringExtra("result"),NoteModel.class);
                    if(noteMap.get(data.getTitle_guid())!=null){
                        List<NoteModel> list = noteMap.get(data.getTitle_guid());
                        List<NoteModel> list1 = new ArrayList<>();
                        list1.add(data);
                        list1.addAll(list);
                        noteMap.put(data.getTitle_guid(),list1);
                    }else {
                        //如果对应主题本来没有笔记
                        List<NoteModel> list = new ArrayList<>();
                        list.add(data);
                        noteMap.put(data.getTitle_guid(),list);
                    }
                    //TODO 先存入缓存 再     数据库操作
                    aCache.put(data.getGuid(),data.getNote_context());
                    databaseHelper.insertNoteData(data);
                    if(titleList.contains(data.getTitle_guid())){
                        showNoteByTitleGUID(titleList);
                    }
                    SyncManageAction syncManageAction = (SyncManageAction) getContext();
                    syncManageAction.SyncStart();

                }else if(result.getResultCode() == Constant.NOTE_COMPILE_FINISH){
                    Intent intent = result.getData();
                    NoteModel data =gson.fromJson( intent.getStringExtra("result"),NoteModel.class);
                    String firstTitleGUID = intent.getStringExtra("extradata");
                    if(firstTitleGUID.equals(data.getTitle_guid())){
                        //如果所属主题没有改变
                        List<NoteModel> list = noteMap.get(data.getTitle_guid());
                        List<NoteModel> list1 = new ArrayList<>();
                        list1.addAll(list);
                        Iterator<NoteModel> iterator = list1.iterator();
                        while (iterator.hasNext()) {
                            NoteModel item = iterator.next();
                            if (item.getGuid().equals(data.getGuid())) {
                                // 删除当前元素
                                iterator.remove();
                                break;
                            }
                        }
                        list1.add(data);
                        noteMap.put(data.getTitle_guid(),list1);
                        //TODO 笔记更新数据库操作
                        databaseHelper.updateNoteData(data);

                    }else{
                        List<NoteModel> list = noteMap.get(firstTitleGUID);
                        List<NoteModel> list1 = new ArrayList<>();
                        list1.addAll(list);
                        Iterator<NoteModel> iterator = list1.iterator();
                        while (iterator.hasNext()) {
                            NoteModel item = iterator.next();
                            if (item.getGuid().equals(data.getGuid())) {
                                // 删除当前元素
                                iterator.remove();
                                break;
                            }
                        }
                        noteMap.put(firstTitleGUID,list1);
                        List<NoteModel> models = noteMap.get(data.getTitle_guid());
                        List<NoteModel> list2 = new ArrayList<>();
                        if(models!=null){
                            list2.addAll(models);
                        }
                        list2.add(data);
                        noteMap.put(data.getTitle_guid(),list2);
                        //TODO 笔记更新数据库操作 更新两个表
                        databaseHelper.beginTransaction();
                        try{
                            databaseHelper.updateNoteData(data);
                            databaseHelper.setTransactionSuccessful();
                        }finally {
                            databaseHelper.endTransaction();
                        }
                    }

                    //更新缓存 aCache.put(data.getGuid(),data.getNote_context());在addactivity已经更新过了
                    //页面刷新
                    showNoteByTitleGUID(titleList);
                    SyncManageAction syncManageAction = (SyncManageAction) getContext();
                    syncManageAction.SyncStart();
                }
            }
        });
        toolbar_add_note.setOnClickListener(v->{
            Intent intent = new Intent(getActivity(), AddNoteActivity.class);
            intent.putExtra("note_manage","add");
            intentActivityResultLauncher.launch(intent);
        });
/*        refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                SyncManageAction action = (SyncManageAction) getContext();
                Handler handler = action.getHandler();
                Message message = handler.obtainMessage();
                message.what = 8;
                handler.sendMessage(message);
                refreshableView.finishRefreshing();
            }
        },0);//id用来区分不同的下拉栏*/
        refreshableView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refreshableView.finishRefresh(500);
                SyncManageAction action = (SyncManageAction) getContext();
                Handler handler = action.getHandler();
                Message message = handler.obtainMessage();
                message.what = 8;
                handler.sendMessage(message);
            }
        });
        refreshableView.setOnLoadMoreListener(new OnLoadMoreListener(){
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                noteListAdapter.loadMore();
            }
        });
    }
    public void finishLoadMore(){
        refreshableView.finishLoadMore();
    }
    public void finishLoadMoreNoData(){
        refreshableView.finishLoadMoreWithNoMoreData();
    }
    public void setNoMoreData(){
        refreshableView.setNoMoreData(false);
    }
    //按主题id显示对应笔记
    public void showNoteByTitleGUID(List<String> titleID){
        titleList = titleID;
        List<NoteModel> noteModelList = new ArrayList<>();
        if(noteMap==null||noteMap.size()==0){
            return;
        }
        for (String s : titleID) {
            if(noteMap.get(s)!=null){
                noteModelList.addAll(noteMap.get(s));
            }

        }
        DataManager.getInstance().notifyObservers(Constant.Note_List_KEY,noteModelList);//提醒list列表数据发生改变
    }

    public void updateTitleGUID(List<String> tileID){
        titleList = tileID;
    }

    //按主题删除对应笔记
    public void deleteNoteByTitleGUID(List<String> titleID){
        List<NoteModel> list = new ArrayList<>();
        for (String s : titleID) {
            if(noteMap.get(s)!=null){
                list.addAll(noteMap.get(s));
                noteMap.remove(s);
            }
        }
        //删除本地的图片
        for (NoteModel noteModel : list) {
            ImageUtils.deleteImageFileByNoteID(noteModel.getGuid(),getContext());
        }
        //TODO 执行数据库删除操作
        if(titleID.size()!=0){
            databaseHelper.updateNoteDataIsDelByTitleID(titleID);
        }
        //TODO 同步

    }
    //由于删除最高级主题后，map中没有主题对应数据，需要title重新将最高级主题塞入达成变化
    public void setTopTitle(String uuid){
        if(noteMap.size()==0){
            List<NoteModel> noteModelList = new ArrayList<>();
            noteMap.put(uuid,noteModelList);
        }
    }
    //删除某一笔记 数据库的删除在notelistadapter类
    public void deleteNoteByNoteID(List<String> noteGUID) {
        List<NoteModel> temp = new ArrayList<>();
        String key1 = null;
        for (String s : noteGUID) {
            for (Map.Entry<String, List<NoteModel>> entry : noteMap.entrySet()) {
                String key  = entry.getKey(); // 获取键
                List<NoteModel> value = entry.getValue(); // 获取值
                List<NoteModel> value1 = new ArrayList<>();
                value1.addAll(value);
                Iterator<NoteModel> iterator  = value1.iterator();
                 boolean i = false;
                while (iterator.hasNext()) {
                    NoteModel item = iterator.next();
                    if (item.getGuid().equals(s)) {
                        // 删除当前元素
                        iterator.remove();
                        i = true;
                        break;
                    }
                }
                if(i){temp = value1;key1 = key;break;}
            }
            noteMap.put(key1,temp);
            ImageUtils.deleteImageFileByNoteID(s,getContext());
        }
    }

    public void getNoteData(){
        List<NoteModel> noteModelList ;
        noteModelList = databaseHelper.queryNoteBySQL();
//            Cursor cursor = databaseHelper.query("note as n,note_title as nt",new String[]{"n.guid","theme","create_time","ssn",
//                            "update_time","is_del","user_guid","nt.title_guid"},
//                    "n.guid=nt.note_guid and n.is_del = ?",new String[]{"0"},null,null,"title_guid",null);
//
            if(!(noteModelList ==null) &&noteModelList.size()!=0) {
                noteMap = getBeanNoteModelMap(noteModelList);
            }else {
                noteMap = new ConcurrentHashMap<>();//如果是空也要初始化一下
            }


    }
    Map<String,List<NoteModel>> getBeanNoteModelMap(List<NoteModel> list){
        Map<String,List<NoteModel>> listMap = new ConcurrentHashMap<>();
        String titleGuid = list.get(0).getTitle_guid();
        int pos = 0;//相同标题的起始位置
        for (int i = 0; i < list.size(); i++) {
            if(i==list.size()-1){
                if(!list.get(i).getTitle_guid().equals(titleGuid)){
                    //先把前面的笔记存入 再移动头部到新主题的唯一笔记
                    listMap.put(titleGuid,list.subList(pos,i));
                    listMap.put(list.get(i).getTitle_guid(),list.subList(i,i+1));
                    break;//这是末尾只有一个主题的对应的唯一笔记
                }
                //末尾有一个主题对应的两个以上的笔记 不需要再找后续有不同的主题
                listMap.put(titleGuid,list.subList(pos,i+1));
            }
            else if(!list.get(i).getTitle_guid().equals(titleGuid)){
                listMap.put(titleGuid,list.subList(pos,i));
                pos = i;
                titleGuid = list.get(i).getTitle_guid();
            }
        }
        //
        //fromIndex – low endpoint (inclusive) of the subList 包含
       // toIndex – high endpoint (exclusive) of the subList 不包含
        return listMap;
    }

    @Override
    public void onDataChanged(List<NoteModel> newData) {
        if(!(newData ==null) &&newData.size()!=0) {
            noteMap = getBeanNoteModelMap(newData);
        }else {
            noteMap = new ConcurrentHashMap<>();//如果是空也要初始化一下
        }
    }
}
