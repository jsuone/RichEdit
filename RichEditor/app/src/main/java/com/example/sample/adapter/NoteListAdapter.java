package com.example.sample.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sample.Impl.Observer;
import com.example.sample.Impl.OnNoteListChangeListener;
import com.example.sample.Impl.OnNoteThemeSelectedListener;
import com.example.sample.Impl.SyncManageAction;
import com.example.sample.activity.AddNoteActivity;
import com.example.sample.util.ACache;
import com.example.sample.util.Constant;
import com.example.sample.util.DataManager;
import com.example.sample.util.ImageUtils;
import com.example.sample.util.MyDatabaseHelper;

import com.example.sample.R;
import com.example.sample.model.NoteModel;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class NoteListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Observer<NoteModel> {
    private Context context;
    private MyDatabaseHelper myDatabaseHelper;
    private List<NoteModel> noteModelList;
    ActivityResultLauncher<Intent> intentActivityResultLauncher;
    Gson gson;
    ACache aCache;
    private  int pos_cnt = 10;//当前子项数量

    public NoteListAdapter(Context context, List<NoteModel> noteModelList,ActivityResultLauncher<Intent> intentActivityResultLauncher) {
        this.context = context;
        this.noteModelList = noteModelList;
        //TODO 按时间排序 每次取出十条显示
        Collections.sort(this.noteModelList,new Comparator<NoteModel>() {
            @Override
            public int compare(NoteModel o1, NoteModel o2) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date dt1 = sdf.parse(o1.getCreateTime());
                    Date dt2 = sdf.parse(o2.getCreateTime());
                    if (dt1.getTime() > dt2.getTime()) {
                        return -1;//如果o1大于o2的时间，位于前方
                    } else if (dt1.getTime() < dt2.getTime()) {
                        return 1;
                    } else {
                        return 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });

        myDatabaseHelper = MyDatabaseHelper.getInstance(context);
        this.intentActivityResultLauncher = intentActivityResultLauncher;
        DataManager.getInstance().registerObserver(Constant.Note_List_KEY,this);
        gson = new Gson();
        aCache = ACache.get(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_note_item,parent,false);
        return new NoteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NoteModel noteModel = noteModelList.get(position);
        NoteViewHolder noteViewHolder = (NoteViewHolder) holder;
        noteViewHolder.setCreate_time(noteModel.getCreateTime());
        noteViewHolder.setTheme(noteModel.getTheme());
        noteViewHolder.setMore_optionOnClickListen(v->{
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.getMenuInflater().inflate(R.menu.note_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    // 处理菜单项的点击事件
                    switch (item.getItemId()) {
                        case R.id.menu_note_del:
                            // 处理删除按钮的点击事件 删除笔记
                            List<String> list = new ArrayList<>();
                            list.add(noteModel.getGuid());
                            noteModelList.remove(noteModel);
                            pos_cnt = pos_cnt-1;
                            notifyItemRemoved(holder.getAdapterPosition());
                            OnNoteThemeSelectedListener listener = (OnNoteThemeSelectedListener) context;
                            listener.deleteNoteByNoteID(list);
                            //删除笔记附带的图片
                            ImageUtils.deleteImageFileByNoteID(noteModel.getGuid(),context);
                            myDatabaseHelper.updateNoteDataIsDelByNoteID(list);
                            SyncManageAction syncManageAction = (SyncManageAction) context;
                            syncManageAction.SyncStart();
                            return true;
                       /* case R.id.menu_note_modify:
                            //修改笔记
                            Intent intent = new Intent(context, AddNoteActivity.class);
                            intent.putExtra("note_manage","modify");
                            //TODO 如果笔记内容未被查询，查出来存入缓存，如果缓存没有再去
                            if(aCache.getAsString(noteModel.getGuid())==null){
                                String content = myDatabaseHelper.queryNoteContentByGUID(noteModel.getGuid());
                                aCache.put(noteModel.getGuid(),content);
                            }
                            intent.putExtra("note_data",gson.toJson(noteModel));
                            intentActivityResultLauncher.launch(intent);
                            return true;*/
                        default:
                            return false;
                    }
                }
            });
            popupMenu.show();
        });
        //设置列表子项的点击事件 查看笔记
        noteViewHolder.setViewOnClickListener(v->{
            //修改笔记
            Intent intent = new Intent(context, AddNoteActivity.class);
            intent.putExtra("note_manage","modify");
            //TODO 如果笔记内容未被查询，查出来存入缓存，如果缓存没有再去
            if(aCache.getAsString(noteModel.getGuid())==null){
                String content = myDatabaseHelper.queryNoteContentByGUID(noteModel.getGuid());
                aCache.put(noteModel.getGuid(),content);
            }
            intent.putExtra("note_data",gson.toJson(noteModel));
            intentActivityResultLauncher.launch(intent);
        });
    }

    @Override
    public int getItemCount() {
        if(noteModelList.size()<=10){
            pos_cnt = noteModelList.size();
            return noteModelList.size();
        }else{
            return pos_cnt;
        }

    }

    public void loadMore(){
        OnNoteListChangeListener listener = (OnNoteListChangeListener) context;
        if(pos_cnt==noteModelList.size()){
            listener.finishLoadMoreNoData();
            //如果加载完毕
        }else if(pos_cnt<noteModelList.size()){
            //如果还能加载更多
            int sub = noteModelList.size()-pos_cnt;
            if(sub<10){
                int temp = pos_cnt;
                pos_cnt = pos_cnt+sub;
                notifyItemRangeInserted(temp,sub);
                listener.finishLoadMore();
            }else{
                int temp = pos_cnt;
                pos_cnt = pos_cnt+10;
                notifyItemRangeInserted(temp,10);
                listener.finishLoadMore();
            }

        }
    }
    @Override
    public void onDataChanged(List<NoteModel> newData) {
        noteModelList = newData;
        Collections.sort(noteModelList,new Comparator<NoteModel>() {
            @Override
            public int compare(NoteModel o1, NoteModel o2) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date dt1 = sdf.parse(o1.getCreateTime());
                    Date dt2 = sdf.parse(o2.getCreateTime());
                    if (dt1.getTime() > dt2.getTime()) {
                        return -1;//如果o1大于o2的时间，位于前方
                    } else if (dt1.getTime() < dt2.getTime()) {
                        return 1;
                    } else {
                        return 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
        OnNoteListChangeListener listener = (OnNoteListChangeListener) context;
        listener.setNoMoreData();//允许刷新 否则列表无法触发刷新
        //每当数据源切换初始化一pos_cnt,不然保留着上一数据源的大小，依照此进行显示和需求不符
        if(noteModelList.size()<=10){
            pos_cnt = noteModelList.size();
        }else{
             pos_cnt = 10;
        }
        notifyDataSetChanged();
    }
    static class NoteViewHolder extends RecyclerView.ViewHolder{
        private TextView create_timeView;
        private TextView themeView;
        private TextView truncated_contentView;
        private ImageButton more_option;
        private View view;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            create_timeView = itemView.findViewById(R.id.note_create_time);
            themeView = itemView.findViewById(R.id.note_theme);
            more_option = itemView.findViewById(R.id.note_more_option);
        }
        public void setCreate_time(String time){
            create_timeView.setText(time);
        }
        public void setTheme(String theme){
            themeView.setText(theme);
        }
        public void setMore_optionOnClickListen(View.OnClickListener listen){
            more_option.setOnClickListener(listen);
        }
        public void setViewOnClickListener(View.OnClickListener listener){
            view.setOnClickListener(listener);
        }
    }
}
