package com.example.sample.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sample.Impl.Observer;
import com.example.sample.Impl.SyncManageAction;
import com.example.sample.activity.ChangePasswordActivity;
import com.example.sample.activity.SecurityQuestionActivity;
import com.example.sample.activity.SetLockActivity;
import com.example.sample.communication.SendHttp;
import com.example.sample.util.ACache;
import com.example.sample.util.ActivityCollector;
import com.example.sample.util.Constant;
import com.example.sample.util.DataManager;
import com.example.sample.util.MyDatabaseHelper;
import com.example.sample.R;
import com.example.sample.adapter.TitleListAdapter;
import com.example.sample.model.TitleModel;

import java.util.ArrayList;
import java.util.List;

public class MainNavigation extends LinearLayout implements Observer<TitleModel> ,View.OnClickListener{
    private Context context;
    private RecyclerView recyclerView;
    private MyDatabaseHelper databaseHelper;
    private TitleModel titleModel;//标题列表的数据
    private TextView login_out;
    private TextView changedPasswordView;//如果没有设置密保，输入密码
    private TextView securityQuestionView;//密保设置需要输入密码
    private TextView set_lock_password;
    static String TAG = "notedata";
    public MainNavigation(Context context) {
        this(context,null);
    }

    public MainNavigation(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MainNavigation(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        databaseHelper = MyDatabaseHelper.getInstance(context);
        //注册观察者
        DataManager.getInstance().registerObserver(Constant.Title_KEY,this);
        getTitleData();
        initView();
    }
    public void getTitleData(){
        databaseHelper.open();
        databaseHelper.beginTransaction();//开启事务
        try {
            List<TitleModel> titleModelList = databaseHelper.queryAllTitle();
            titleModel = getBeanTitleModel(titleModelList,0,null);//得到
            databaseHelper.setTransactionSuccessful();
        }finally {
            databaseHelper.endTransaction();
        }
        DataManager.getInstance().addTitleData(Constant.Title_KEY,titleModel);

    }
    void initView(){
        View view = LayoutInflater.from(context).inflate(R.layout.navigation_main,null);
        recyclerView = view.findViewById(R.id.rv_title);
        TitleListAdapter titleListAdapter = new TitleListAdapter(context,titleModel);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(titleListAdapter);
        login_out = view.findViewById(R.id.log_out);
        changedPasswordView = view.findViewById(R.id.changed_password);
        securityQuestionView = view.findViewById(R.id.set_security_question);
        set_lock_password = view.findViewById(R.id.set_lock_password);
        addView(view);
        login_out.setOnClickListener(this);
        changedPasswordView.setOnClickListener(this);
        securityQuestionView.setOnClickListener(this);
        set_lock_password.setOnClickListener(this);
    }
    TitleModel getBeanTitleModel(List<TitleModel> titleModelList,int pos,TitleModel titleModel){
        //pos为当前level级别标题在list中的开始的位置 titlemodel是上一级标题 返回的应该是所有下级标题子集全部包含的标题
        if(pos>=titleModelList.size()){//上一级标题是最后一级标题
            return titleModel;
        }
        int level = titleModelList.get(pos).getLevel();
        if(titleModel==null){//最初的根情况处理
            titleModel = titleModelList.get(pos);
            return getBeanTitleModel(titleModelList,pos+1,titleModel);
        }

        List<TitleModel> child = new ArrayList<>();
        int now_count = 0;
        for (int i = pos; i < titleModelList.size()&&titleModelList.get(i).getLevel()==level; i++) {
            now_count++;
        }
        for (int i = pos; i < titleModelList.size()&&titleModelList.get(i).getLevel()==level; i++) {
          TitleModel temp = titleModelList.get(i);
            if(temp.getParentID().equals(titleModel.getGuid())){
                child.add(getBeanTitleModel(titleModelList,pos+now_count,temp));
            }
        }
       titleModel.setChild(child);
        return titleModel;
    }

    @Override
    public void onDataChanged(List<TitleModel> newData) {
        Log.d(TAG, "onDataChanged: 标题数据发生变化");
        if(newData!=null&&newData.size()!=0){
            titleModel = getBeanTitleModel(newData,0,null);//得到
            DataManager.getInstance().addTitleData(Constant.Title_KEY,titleModel);
            List<TitleModel> list = new ArrayList<>();
            list.add(titleModel);
            DataManager.getInstance().notifyObservers(Constant.Title_List_KEY,list);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.log_out:
                //TODO 发起退出登录请求 关闭长连接 再清除所有数据 如果没有网络 就清除数据库
                SendHttp sendHttp = new SendHttp();
                ACache aCache = ACache.get(getContext());
                String username = aCache.getAsString("user_name");
                String token = aCache.getAsString("token");
                sendHttp.loginOut(username,token);
                SyncManageAction action = (SyncManageAction) getContext();
                action.SyncStop();
                databaseHelper.deleteAllData();
                aCache.clear();
                ActivityCollector.finishAll();
                System.exit(0);
                break;
            case R.id.changed_password:
                Intent intent = new Intent(getContext(), ChangePasswordActivity.class);
                getContext().startActivity(intent);
                break;
            case R.id.set_security_question:
                Intent intent1 = new Intent(getContext(), SecurityQuestionActivity.class);
                getContext().startActivity(intent1);
                break;
            case R.id.set_lock_password:
                Intent intent2 = new Intent(getContext(), SetLockActivity.class);
                getContext().startActivity(intent2);
                break;
        }
    }
}
