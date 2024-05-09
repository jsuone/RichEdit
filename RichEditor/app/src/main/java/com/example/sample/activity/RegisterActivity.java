package com.example.sample.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.sample.R;
import com.example.sample.communication.SendHttp;
import com.example.sample.model.TitleModel;
import com.example.sample.model.User;
import com.example.sample.util.ACache;
import com.example.sample.util.ActivityCollector;
import com.example.sample.util.DateTime;
import com.example.sample.util.ImageUtils;
import com.example.sample.util.MyDatabaseHelper;
import com.google.gson.Gson;

import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    private EditText userName;
    private EditText password;
    private Button register;
    private SendHttp sendHttp = new SendHttp();
    private ACache aCache;
    private Gson gson = new Gson();
    private MyDatabaseHelper databaseHelper;
    private ImageButton backView;
    private ProgressBar progressBar;
    private View overlayView;
    static  String TAG = "notedata";
    class RegisterHandler extends  Handler{
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();

            switch (bundle.getInt("isRepeatUserName",-1)){
                case 0://不重复
                    setError(null);
                    break;
                case 1:
                    setError("用户名已经存在");
                    break;
                default:
                    break;
            }
            switch (bundle.getInt("registerUser",-1)){
                case  0://注册失败
                    hideProgressBar();
                    String msgTixing = bundle.getString("msg");
                    Toast.makeText(RegisterActivity.this,msgTixing+"hjk",Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    hideProgressBar();
                    String token = bundle.getString("token");
                    aCache.put("token",token);
                    Log.d(TAG, "register handleMessage: token  "+token);
                    User user = gson.fromJson(bundle.getString("userData"),User.class);
                    Log.d(TAG,"注册成功接收到的user数据"+user.toString());
                    //删除之前的数据 包括图片之类
                    ImageUtils.deleteAllImageFile(RegisterActivity.this);
                    databaseHelper.deleteAllData();
                    //插入数据库 插入缓存 插入初始化标题数据
                    int ssn = user.getSsn();
                    TitleModel titleModel = new TitleModel(UUID.randomUUID().toString(),"全部","",0,false,false,
                            ssn, DateTime.getCurrentTime(),"",user.getUsername());
                    ssn++;
                    user.setSsn(ssn);
                    databaseHelper.insertDataByTitleModel(titleModel);
                    databaseHelper.insertUser(user);
                    aCache.put("user_ssn",user.getSsn());
                    aCache.put("user_name",user.getUsername());
                    Log.d(TAG,"注册成功插入缓存的user名字"+aCache.getAsString("user_name"));
                    aCache.put("lastUpdateSSN",user.getLastUpdateSSN());
                    aCache.put("lastSyncTime",user.getLastSyncTime());
                    //进入主界面
                    Intent intent = new Intent(RegisterActivity.this,MainContentActivity.class);
                    startActivity(intent);
                    break;
                case 2://网络断开或者连接异常
                    hideProgressBar();
                    Toast.makeText(RegisterActivity.this,"网络异常，请检查连接",Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    };
    RegisterHandler handler = new RegisterHandler();
    Runnable inputChecker = new Runnable() {
        @Override
        public void run() {
            // 用户停止输入一秒钟后执行的操作
            sendHttp.isRepeatUserName(userName.getText().toString(),handler);

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);
        ActivityCollector.addActivity(this);
        aCache = ACache.get(this);
        databaseHelper = MyDatabaseHelper.getInstance(this);
        initView();
    }
    void initView(){
        userName = findViewById(R.id.register_username);//用户名限制为数字字母下划线 不能有/ 不然之后把用户名作为存储图片的文件夹名称时怕有问题
        password = findViewById(R.id.register_password);
        register = findViewById(R.id.register);
        backView = findViewById(R.id.register_back);
        progressBar = findViewById(R.id.register_loading);
        //当用户名输入框失去焦点 进行用户名是否重复的请求
        userName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    //失去焦点
                    if(userName.getText()==null||userName.getText().toString().equals("")){
                        return;
                    }
                    sendHttp.isRepeatUserName(userName.getText().toString(),handler);

                }
            }
        });
        register.setOnClickListener(v->{
            //首先用户名和密码都不能为空
            if(userName.getText().toString().equals("")||password.getText().toString().equals("")||userName.getError()!=null){
                //为空或者用户名重复
                Toast.makeText(this,"用户名或密码为空",Toast.LENGTH_SHORT).show();
            }else{
                showProgressBar();
                String currentTime = DateTime.getCurrentTime();
                User user = new User(userName.getText().toString(),password.getText().toString(),0,0, currentTime);
                sendHttp.registerUser(user,handler);
            }
        });
        userName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 文字变化前的操作
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 文字变化时的操作
                // 每次文本变化时，移除之前的延迟任务，重新发送延迟消息
                handler.removeCallbacks(inputChecker);
                handler.postDelayed(inputChecker, 2000); // 延迟2秒执行
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 文字变化后的操作
            }
        });
        backView.setOnClickListener(v->{

            ActivityCollector.destroyActivity(this);
        });
    }
    public void setError(String error){
        userName.setError(error);
    }
    // 显示进度条时调用
    private void addOverlay() {
        // 获取根布局
        RelativeLayout drawerLayout = findViewById(R.id.register_layout);
        ViewGroup rootView = (ViewGroup) drawerLayout.getRootView();

        // 创建一个半透明的遮罩层
        overlayView = new View(this);
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setBackgroundColor(Color.parseColor("#80000000")); // 半透明的黑色
        // 将遮罩层添加到根布局
        rootView.addView(overlayView);
        // 设置遮罩层的点击事件拦截，防止用户点击底层的界面元素
        overlayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do nothing
            }
        });
    }

    // 移除遮罩层
    private void removeOverlay() {
        if (overlayView != null) {
            // 获取根布局
            RelativeLayout drawerLayout = findViewById(R.id.register_layout);
            ViewGroup rootView = (ViewGroup) drawerLayout.getRootView();
            // 移除遮罩层
            rootView.removeView(overlayView);
            overlayView = null; // 释放资源
        }
    }
    // 显示进度条时调用
    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        addOverlay(); // 添加遮罩层
    }
    // 隐藏进度条时调用
    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        removeOverlay(); // 移除遮罩层
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}