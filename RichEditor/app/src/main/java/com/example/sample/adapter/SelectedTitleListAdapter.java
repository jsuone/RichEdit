package com.example.sample.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sample.R;
import com.example.sample.model.TitleModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectedTitleListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private TitleModel titleModel;
    private Map<String,Boolean> isOpenState;//当前主题展开状态数据 不能直接修改titlemodel，否则会影响侧滑栏
    private String pos_titleGUID;//笔记的所属主题
    private int init_position = -1;//初次笔记的位置
    static int sum = 0;//当前列表子项所有的数量
    static int count =0;//给得到对应数据的递归方法计数的，方便找到对应位置的数据
    private SelectedTitleListAdapter.TitleViewHolder pos_viewHolder;//当前选中的元素 控制背景变化
    private TitleModel pos_titleModel;//当前选中的元素的数据
    private StringBuilder pos_Path;//当前主题的目录路径


    public SelectedTitleListAdapter(Context context, TitleModel titleModel, String pos_titleGUID) {
        this.context = context;
        this.titleModel = titleModel;
        this.pos_titleGUID = pos_titleGUID;
        isOpenState = new HashMap<>();
        pos_Path = new StringBuilder();
        restoreStateToMap(titleModel);
        locateAndExpandItem(pos_titleGUID);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_title_item,parent,false);
        return new SelectedTitleListAdapter.TitleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        count = 0;
        TitleModel titleModel1 = getTitleModelByPosition(titleModel,position);
        SelectedTitleListAdapter.TitleViewHolder titleViewHolder = (SelectedTitleListAdapter.TitleViewHolder) holder;
        titleViewHolder.setTitleName(titleModel1.getTitle());
        titleViewHolder.setChecked(isOpenState.get(titleModel1.getGuid()));
        titleViewHolder.setImageButtonVisibly();
        titleViewHolder.setToggleButtonOnclickListen(new View.OnClickListener() {//不能用checkchange监听，因为这样在初始化计算布局的时候如果有列表是展开就会和默认的关闭不同，触发checkchange监听，又刷新列表，官方是不允许在计算布局的时候再次刷新列表的
            @Override
            public void onClick(View v) {
                //titleModel1.setOpen(titleViewHolder.getChecked());
                isOpenState.put(titleModel1.getGuid(),titleViewHolder.getChecked());
                if(titleModel1.getChild()!=null){//如果没有子项不进行列表的刷新
                    notifyItemRangeChanged(holder.getAdapterPosition()+1,sum-holder.getAdapterPosition());
                }
            }
        });

        titleViewHolder.setTextViewOnclickListen(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectedTitleListAdapter.TitleViewHolder pre = pos_viewHolder;
                pos_viewHolder = titleViewHolder;
                pos_titleModel = titleModel1;
                if(pre!=null){
                    pre.setBackgroudColor(false);
                }
                pos_viewHolder.setBackgroudColor(true);
            }
        });
        if(pos_titleGUID.equals(titleModel1.getGuid())){//初始化选中的标题
            titleViewHolder.callBackOnclick();//调用上面注册的点击事件，表示最开始展示全部的笔记
            pos_viewHolder = titleViewHolder;
            init_position = holder.getAdapterPosition();
        }
        int left = (int) titleModel1.getLevel()*20;
        titleViewHolder.setLeftMargin(left);
    }

    @Override
    public int getItemCount() {
        int cnt = 0;
        if(titleModel == null||titleModel.getTitle().equals("")){
            return cnt;
        }
        sum = calculateCount(titleModel);
        return sum;
    }

    public int calculateCount(TitleModel titleModel){//递归计算总数
        Boolean isOpen = isOpenState.get(titleModel.getGuid());
        if(titleModel.getChild()==null||!isOpen){//没有子项或者不展开
            return 1;
        }else {
            List<TitleModel> child = titleModel.getChild();
            int cnt = 1;//本身占一个元素 之后再加上子元素数目
            for (TitleModel model : child) {

                cnt = cnt+calculateCount(model);
            }
            return cnt;
        }

    }
    public void locateAndExpandItem(String targetGuid) {
        //找到当前笔记所属的主题并展开主题所属的父类
        TitleModel temp = getTitleModelByParentID(titleModel,targetGuid);

        isOpenState.put(temp.getGuid(), true);
        if(temp.getParentID()==null||temp.getParentID().equals("")){
            pos_Path.insert(0,temp.getTitle());
            return;
        }
        pos_Path.insert(0,"/"+temp.getTitle());
        locateAndExpandItem(temp.getParentID());

    }

    public void restoreStateToMap(TitleModel titleModel){//将展开状态保存到map中
        isOpenState.put(titleModel.getGuid(),titleModel.isOpen());
        if(titleModel.getChild()==null){
            return;
        }else {
            List<TitleModel> child = titleModel.getChild();
            for (TitleModel model : child) {
                restoreStateToMap(model);
            }
        }
    }
    public TitleModel getTitleModelByParentID(TitleModel titleModel,String guid){
        if(titleModel.getGuid().equals(guid)){
            return titleModel;
        }
        if(titleModel.getChild()!=null){
            List<TitleModel> models = titleModel.getChild();
            for (TitleModel model : models) {
                TitleModel temp =  getTitleModelByParentID(model,guid);
                if(temp!=null){return temp;}
            }
        }
        return null;
    }
    public TitleModel getTitleModelByPosition(TitleModel titleModel,int position){
        //选择展开的所有数据应当显示
        if(count==position) return titleModel;
        else if(titleModel.getChild()==null||!isOpenState.get(titleModel.getGuid())) return null;
        else if(count < position){
            List<TitleModel> child = titleModel.getChild();
            for (TitleModel model : child) {
                count++;
                TitleModel temp = getTitleModelByPosition(model,position);
                if(temp!=null) return temp;
            }
        }
        return null;
    }
    public Map<String,String> getSelectedData(){//第一个是主题的唯一标识guid 第二个是当前主题的目录显示 类似/../..
        pos_Path.setLength(0);
        locateAndExpandItem(pos_titleModel.getGuid());//刷新路径
        Map<String,String> data= new HashMap<>();
        data.put(pos_titleModel.getGuid(),pos_Path.toString());
        return data;
    }
    public int getInitPosition(){
        return init_position;
    }
    static class TitleViewHolder extends RecyclerView.ViewHolder{
        private ToggleButton toggleButton;
        private TextView titleName;
        private LinearLayout view;
        private ImageButton imageButton;
        public TitleViewHolder(@NonNull View itemView) {
            super(itemView);
            view = (LinearLayout) itemView;
            toggleButton = itemView.findViewById(R.id.title_isopen);
            titleName = itemView.findViewById(R.id.title_name);
            imageButton = itemView.findViewById(R.id.title_more_option);
        }
        public void setTitleName(String Title){
            titleName.setText(Title);
        }
        public void setLeftMargin(int leftMargin){
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) toggleButton.getLayoutParams();
            layoutParams.leftMargin = leftMargin;
            toggleButton.setLayoutParams(layoutParams);
        }
        public void setChecked(boolean isOpen){
            toggleButton.setChecked(isOpen);
        }
        public void setToggleButtonOnclickListen( View.OnClickListener clickListener ){
            toggleButton.setOnClickListener(clickListener);
        }
        public void setTextViewOnclickListen(View.OnClickListener clickListener){
            titleName.setOnClickListener(clickListener);

        }
        public void setImageButtonOnclickListen(View.OnClickListener clickListener){
            imageButton.setOnClickListener(clickListener );
        }
        public void setImageButtonVisibly(){
            imageButton.setVisibility(View.GONE);
        }
        public Boolean callBackOnclick(){
            return titleName.callOnClick();
        }
        public Boolean callToggleBackOnclick(){return toggleButton.callOnClick();}
        public boolean getChecked(){
            return toggleButton.isChecked();
        }
        public void setBackgroudColor(Boolean isSelected){
            if(isSelected){
                view.setBackgroundColor(Color.YELLOW);
            }else {
                view.setBackgroundColor(Color.TRANSPARENT);
            }
        }

    }
}
