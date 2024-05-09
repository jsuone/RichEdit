package com.example.sample.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sample.Impl.Observer;
import com.example.sample.Impl.SyncManageAction;
import com.example.sample.util.ACache;
import com.example.sample.util.Constant;
import com.example.sample.util.DataManager;
import com.example.sample.util.MyDatabaseHelper;
import com.example.sample.Impl.OnNoteThemeSelectedListener;
import com.example.sample.R;
import com.example.sample.model.TitleModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TitleListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Observer<TitleModel> {
    //初始的时候选择全部标题
    private TitleModel titleModel;
    private Context context;
    static int count =0;//给得到对应数据的递归方法计数的，方便找到对应位置的数据
    static int sum = 0;//当前列表子项所有的数量
    private TitleViewHolder pos_viewHolder;//当前选中的元素 控制背景变化
    private TitleModel pos_titleModel;//当前选中的元素的数据
    private TitleViewHolder top_viewHolder;//最高级的主题
    private MyDatabaseHelper databaseHelper;
    private ACache aCache;
    public TitleListAdapter(Context context,TitleModel titleModel) {
        //super(); 本身继承一个抽象类，不需要加
        this.titleModel = titleModel;
        this.context = context;
        databaseHelper = MyDatabaseHelper.getInstance(context);
        aCache = ACache.get(context);
        //注册观察者
        DataManager.getInstance().registerObserver(Constant.Title_List_KEY,this);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_title_item,parent,false);
        return new TitleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        count = 0;
        TitleModel titleModel1 = getTitleModelByPosition(titleModel,position);
        TitleViewHolder titleViewHolder = (TitleViewHolder) holder;
        titleViewHolder.setTitleName(titleModel1.getTitle());
        titleViewHolder.setChecked(titleModel1.isOpen());

        titleViewHolder.setToggleButtonOnclickListen(new View.OnClickListener() {//不能用checkchange监听，因为这样在初始化计算布局的时候如果有列表是展开就会和默认的关闭不同，触发checkchange监听，又刷新列表，官方是不允许在计算布局的时候再次刷新列表的
            @Override
            public void onClick(View v) {
                titleModel1.setOpen(titleViewHolder.getChecked());
                if(titleModel1.getChild()!=null){//如果没有子项不进行列表的刷新
                    notifyItemRangeChanged(holder.getAdapterPosition()+1,sum-holder.getAdapterPosition());
                }
            }
        });

        titleViewHolder.setTextViewOnclickListen(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TitleViewHolder pre = pos_viewHolder;
                pos_viewHolder = titleViewHolder;
                pos_titleModel = titleModel1;
                if(pre!=null){
                    pre.setBackgroudColor(false);
                }

                pos_viewHolder.setBackgroudColor(true);
                onNoteThemeSelectedListen(titleModel1);
            }
        });
        titleViewHolder.setImageButtonOnclickListen(v->{
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    // 处理菜单项的点击事件
                    switch (item.getItemId()) {
                        case R.id.menu_add:
                            // 处理添加按钮的点击事件 为对应主题添加子级主题
                            createTitleNameDialog(titleModel1,holder.getAdapterPosition());
                            return true;
                        case R.id.menu_delete:
                            // 处理删除按钮的点击事件  如果是最高级主题 只删除子项 其余主题删除自身及子项以及笔记 如果选中的主题被删除，改为选中最高级的主题
                            deleteTitleAndNote(titleModel1,holder.getAdapterPosition());
                            return true;
                        default:
                            return false;
                    }
                }
            });
            popupMenu.show();
        });
        if(position == 0){//选中最初的根标题 全部
            titleViewHolder.callBackOnclick();//调用上面注册的点击事件，表示最开始展示全部的笔记
            top_viewHolder = titleViewHolder;
        }else if(pos_titleModel!=null&&pos_titleModel.getGuid().equals(titleModel1.getGuid())){//在当前主题被折叠由展开重新刷新的时候，重新触发点击事件进行上色
            //titleViewHolder.callBackOnclick();//调用上面注册的点击事件，表示最开始展示全部的笔记
            TitleViewHolder pre = pos_viewHolder;
            pos_viewHolder = titleViewHolder;
            pos_titleModel = titleModel1;
            if(pre!=null){
                pre.setBackgroudColor(false);
            }
            pos_viewHolder.setBackgroudColor(true);
        }
        int left = 0;
        if(titleModel1.getLevel()<15){//级别超过十五不再移动左边距，防止过长
             left= (int) titleModel1.getLevel()*20;
        }else{
            left = 8*20;
        }
        titleViewHolder.setLeftMargin(left);

    }
    public void deleteTitleAndNote(TitleModel data,int position){
        // 处理删除按钮的点击事件  如果是最高级主题 只删除子项 其余主题删除自身及子项以及笔记 如果选中的主题被删除，改为选中最高级的主题
        //首先适配器中数据源进行改变，再执行数据库操作，再发起同步请求,再执行列表的刷新，或者担心同步和列表刷新冲突，将刷新放前面
        List<String> titleName = new ArrayList<>();
       TitleModel t =  getTitleModelByParentID(data, pos_titleModel.getParentID());//判断被选中的是不是被删除的子集
        OnNoteThemeSelectedListener listener = (OnNoteThemeSelectedListener) context;
        if(data.getLevel()==0){
            getAllTitleByParentID(titleName,data);//要在数据源即下一段代码发生变化之前，才有效
            //titleName.remove(data.getGuid());
            data.setChild(null);
            notifyItemRangeRemoved(position+1,sum-1);
            //数据库操作
        }else{
            TitleModel model = getTitleModelByParentID(titleModel,data.getParentID());
            int num = calculateCount(data);//计算在列表有显示的子项数据数量
            model.getChild().remove(data);
            notifyItemRangeRemoved(position,num);
            getAllTitleByParentID(titleName,data);
        }
        //将被删除的主题数据传递过去 对笔记进行删除
        listener.onNoteThemeDeleted(titleName);
        if(data.getLevel()==0){
            titleName.remove(data.getGuid());
            listener.setTopTile(data.getGuid());
        }
        //数据库操作
        databaseHelper.updateTitleDelStateByGUID(titleName);
        //TODO 如果是被选中的主题被删或者被选中的主题是被删除主题的子级 选择最高级的主题  或者被选中的主题是被删除主题的父级要刷新
        if(data.getGuid().equals(pos_titleModel.getGuid())||t!=null){
            top_viewHolder.callBackOnclick();
        }else if(getTitleModelByParentID(pos_titleModel, data.getParentID())!=null){
            pos_viewHolder.callBackOnclick();
        }
        //TODO 执行同步
        SyncManageAction action = (SyncManageAction) context;
        action.SyncStart();
        return;
    }
public void createTitleNameDialog(TitleModel data,int position){

    Dialog dialog = new Dialog(context);
    dialog.setContentView(R.layout.dialog_create_title);
    dialog.setCanceledOnTouchOutside(false);
    dialog.getWindow().setGravity(Gravity.CENTER);
    Button cancel = dialog.findViewById(R.id.title_dialog_cancel);
    Button confirm = dialog.findViewById(R.id.title_dialog_confirm);
    EditText editText = dialog.findViewById(R.id.input_title);
    cancel.setOnClickListener(v->{
        dialog.dismiss();
    });
    confirm.setOnClickListener(v->{
        String titleName = editText.getText().toString();
        if(titleName.equals("")){
            dialog.dismiss();
            Toast.makeText(context,"输入不能为空",Toast.LENGTH_SHORT).show();
        }else {
            List<TitleModel> child = data.getChild();
            if(child==null){
                child = new ArrayList<>();
            }
            //首先适配器中数据源进行改变，再执行数据库操作，再发起同步请求,再执行列表的刷新，或者担心同步和列表刷新冲突，将刷新放前面
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // 获取当前时间
            Date currentDate = new Date();

            // 格式化当前时间
            String create_time = sdf.format(currentDate);
            int ssn = (int) aCache.getAsObject("user_ssn");
            TitleModel newTitle = new TitleModel(UUID.randomUUID().toString(),titleName,data.getGuid(), data.getLevel()+1,
                    false,false,
                    ssn,
                    create_time,"",aCache.getAsString("user_name"));
            ssn++;
            aCache.put("user_ssn",ssn);
            child.add(0,newTitle);
            data.setChild(child);//感觉这一步没有也可以，之后再试
            //进行列表的刷新  如果是展开状态直接插入一个就行 如果不是展开，变为展开显示，不然列表显示会重复下一项
            if(data.isOpen()){
                notifyItemInserted(position+1);
            }/*else{ 不起作用 无法触发对应点击事件 不是很重要 先不管
                titleViewHolder.callToggleBackOnclick();
            }*/
            List<String> list = new ArrayList<>();
            getAllTitleByParentID(list,pos_titleModel);
            //更新notelist拥有的当前选中主题的子主体
            OnNoteThemeSelectedListener listener = (OnNoteThemeSelectedListener) context;
            listener.updateThemeList(list);
            databaseHelper.insertDataByTitleModel(newTitle);
            dialog.dismiss();
            //进行发起同步 // TODO
            SyncManageAction action = (SyncManageAction) context;
            action.SyncStart();

        }

    });
    dialog.show();

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
        if(titleModel.getChild()==null||!titleModel.isOpen()){//没有子项或者不展开
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
    public TitleModel getTitleModelByPosition(TitleModel titleModel,int position){
        //选择展开的所有数据应当显示
        if(count==position) return titleModel;
        else if(titleModel.getChild()==null||!titleModel.isOpen()) return null;
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
    private void onNoteThemeSelectedListen(TitleModel titleModel){//调用笔记页面的切换对应主题的方法
        //查出所有属于titl的guid
      List<String> allTitle = new ArrayList<>();
      getAllTitleByParentID(allTitle,titleModel);
        OnNoteThemeSelectedListener onNoteThemeSelectedListener = (OnNoteThemeSelectedListener)context;
        onNoteThemeSelectedListener.onNoteThemeSelected(allTitle);
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
    public void getAllTitleByParentID(List<String> AllTitle,TitleModel titleModel1){
 /*       if(titleModel1.getGuid().equals(titleGUID)) {
            AllTitle.add(titleGUID);
        }
        if(titleModel1.getChild()==null){return;}
            List<TitleModel> child = titleModel1.getChild();  a711587e-feb3-4aa6-b392-7fcaea99cfa5
                for (TitleModel model : child) {
                    getAllTitleByParentID(model.getGuid(), AllTitle,model);  af5e9af5-cbb6-4657-970c-db86ffa9caff
                }*/
        AllTitle.add(titleModel1.getGuid());
        if(titleModel1.getChild()!=null){
            List<TitleModel> child = titleModel1.getChild();
            for (TitleModel model : child) {
                getAllTitleByParentID(AllTitle,model);
            }
        }else{
            return;
        }

    }

    @Override
    public void onDataChanged(List<TitleModel> newData) {
        titleModel = newData.get(0);
        count =0;//给得到对应数据的递归方法计数的，方便找到对应位置的数据
        sum = 0;
        notifyDataSetChanged();
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
