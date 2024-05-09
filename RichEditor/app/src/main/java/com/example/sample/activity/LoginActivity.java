package com.example.sample.activity;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.sample.communication.SendHttp;
import com.example.sample.model.User;
import com.example.sample.util.ACache;
import com.example.sample.util.ActivityCollector;
import com.example.sample.util.DateTime;
import com.example.sample.util.GlideApp;
import com.example.sample.util.ImageUtils;
import com.example.sample.util.MyDatabaseHelper;
import com.example.sample.R;
import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    private Button btnLogin;
    private Context context;

    private EditText etUserName;
    private EditText etPassWord;
    private Button registerView;
    private ProgressBar progressBar;

    private ImageView isShowPassword;
    private View overlayView;

    private String userName;
    private String pwd;
    private ACache aCache;
    private static Integer saveTime = 604800;
    private MyDatabaseHelper databaseHelper;
    private static final int MY_PERMISSION_REQUEST_CODE = 10000;
    private SendHttp sendHttp = new SendHttp();
    private Gson gson = new Gson();
    private Dialog dialog;
    private Boolean isShow = false;

    class LoginHanlder extends Handler{
        //登录保存token到缓存中         String token = "fadfsaf-fdasfasd";
        //        aCache.put("token",token,saveTime);
        //        aCache.put("user_ssn",0);
        //        aCache.put("user_name","li");
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int i = msg.what;//密码登录成功的保存用户信息在数据库，而且密码登录设计就是需要初始化，同步序列号都是0，与云端比对进行  免密登录成功则不需要保存用户信息在数据库
            //因为免密登录肯定登录过一次同步过了一次，
            Bundle bundle = msg.getData();

            switch (i){
                case 1://密码登录成功
                    String token = bundle.getString("token");
                    User user = gson.fromJson(bundle.getString("user"),User.class);
                    user.setLastUpdateSSN(0);
                    user.setSsn(0);
                    user.setLastSyncTime(DateTime.getCurrentTime());
                    databaseHelper.insertUser(user);
                    aCache.put("token",token,saveTime);
                    aCache.put("user_ssn",user.getSsn());
                    aCache.put("user_name",user.getUsername());
                    aCache.put("lastUpdateSSN",user.getLastUpdateSSN());
                    aCache.put("lastSyncTime",user.getLastSyncTime());

                    //进入主界面
                    Intent intent = new Intent(LoginActivity.this,MainContentActivity.class);
                    startActivity(intent);
                    break;
                case 2://免密登录成功
                    String token1 = bundle.getString("token");
                    aCache.put("token",token1);
                    int ssn = (int) aCache.getAsObject("user_ssn");
                    //查询数据库中的user信息
                    if(aCache.getAsString("user_name")==null){
                        User user1 = databaseHelper.queryUser();
                        aCache.put("user_ssn",user1.getSsn());
                        aCache.put("user_name",user1.getUsername());
                        aCache.put("lastUpdateSSN",user1.getLastUpdateSSN());
                        aCache.put("lastSyncTime",user1.getLastSyncTime());
                    }
                    hideProgressBar();
                    //进入主界面
                    Intent intent1 = new Intent(LoginActivity.this,MainContentActivity.class);
                    startActivity(intent1);
                    break;
                case 3: //失败
                    ImageUtils.deleteAllImageFile(LoginActivity.this);
                    databaseHelper.deleteAllData();
                    aCache.remove("user_ssn");
                    aCache.remove("user_name");
                    aCache.remove("lastUpdateSSN");
                    aCache.remove("lastSyncTime");
                    aCache.remove("token");
                    hideProgressBar();
                    Bundle bundle1 = msg.getData();
                    String message = bundle1.getString("msg");
                    Toast.makeText(context, " "+message, Toast.LENGTH_SHORT).show();
                    break;
                case 4: //是否抢占登录
                    String token2 = bundle.getString("token");
                    String json = bundle.getString("user");
                    if(json!=null){
                        User user2 = gson.fromJson(json,User.class);
                        user2.setLastUpdateSSN(0);
                        user2.setSsn(0);
                        user2.setLastSyncTime(DateTime.getCurrentTime());
                        databaseHelper.insertUser(user2);
                        aCache.put("user_ssn",user2.getSsn());
                        aCache.put("user_name",user2.getUsername());
                        aCache.put("lastUpdateSSN",user2.getLastUpdateSSN());
                        aCache.put("lastSyncTime",user2.getLastSyncTime());
                    }
                    aCache.put("token",token2,saveTime);
                    dialog.show();
                    break;
                case 5: //登录发生网络问题
                    hideProgressBar();
                    Toast.makeText(LoginActivity.this,"链接失败请检查网络",Toast.LENGTH_SHORT).show();
                    break;
                case 6://  5是免密登录发生网络问题
                    //查询数据库中的user信息
                    if(aCache.getAsString("user_name")==null){
                        User user1 = databaseHelper.queryUser();
                        aCache.put("user_ssn",user1.getSsn());
                        aCache.put("user_name",user1.getUsername());
                        aCache.put("lastUpdateSSN",user1.getLastUpdateSSN());
                        aCache.put("lastSyncTime",user1.getLastSyncTime());
                    }
                    hideProgressBar();
                    //进入主界面
                    Intent intent2 = new Intent(LoginActivity.this,MainContentActivity.class);
                    startActivity(intent2);
                    break;
            }
        }
    }
    private LoginHanlder hanlder = new LoginHanlder();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        aCache = ACache.get(this);
        if (!commonROMPermissionCheck(this)) {
            requestAlertWindowPermission();
        }
        databaseHelper = MyDatabaseHelper.getInstance(this);
        dialog =new AlertDialog.Builder(this).setMessage("账户正在使用中，确定抢占吗,如非本人使用,登录后请修改密码确保安全").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent1 = new Intent(LoginActivity.this,MainContentActivity.class);
                        startActivity(intent1);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ImageUtils.deleteAllImageFile(LoginActivity.this);
                        databaseHelper.deleteAllData();
                        aCache.remove("user_ssn");
                        aCache.remove("user_name");
                        aCache.remove("lastUpdateSSN");
                        aCache.remove("lastSyncTime");
                        aCache.remove("token");
                        dialog.dismiss();
                    }
                }).create();
        dialog.setCanceledOnTouchOutside(false);
        setContentView(R.layout.activity_login);
        istoken();

        applyPermission();

        if(aCache.getAsString("token")==null){
            //TODO 免密不会到这 删除数据库中所有数据
            Log.d(TAG, "onCreate: 删除所有数据");
            ImageUtils.deleteAllImageFile(this);
            databaseHelper.open();
            databaseHelper.deleteAllData();
        }

        initView();
    }
    void initView(){
        context = LoginActivity.this;
        btnLogin = (Button) findViewById(R.id.login);
        etUserName = (EditText) findViewById(R.id.username);
        etPassWord = (EditText) findViewById(R.id.password);
        registerView = findViewById(R.id.go_to_register);
        isShowPassword = findViewById(R.id.is_show_password);

        isShowPassword.setOnClickListener(v->{
            if(!isShow){
                //密码变为可见
                isShowPassword.setSelected(true);
                isShow = true;
                etPassWord.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }else{
                isShowPassword.setSelected(false);
                isShow = false;
                etPassWord.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
        // 点击登录
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 如果用户名密码为空 则弹出提示
                if (isCheckNull()) {
                    Toast.makeText(context, "用户名密码不能为空 ", Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                    // TODO 联网登陆 异步操作
                    // 开启线程，通过post方式将将用户名密码为提交到服务器，将获得结果在Handler中处理
                    // 登陆成功，保存用户名，密码密文到SharedPreferences
                    login();
                    // saveInfo(); //保存信息这一步应该在Handler中完成

            }
        });
        //转去注册页面
        registerView.setOnClickListener(v->{
            Intent intent = new Intent(this,RegisterActivity.class);
            startActivity(intent);

        });

    }
    /**
     * 联网登陆
     */
    private void login() {//用户和token一定要对应
        sendHttp.loginByUser(etUserName.getText().toString(),etPassWord.getText().toString(),hanlder);

    }


    /**
     * 提交空校验
     *
     * @return
     */
    private Boolean isCheckNull() {
        userName = etUserName.getText().toString();
        pwd = etPassWord.getText().toString();
        return ( TextUtils.isEmpty(userName)
                 || TextUtils.isEmpty(pwd));
    }

    /**
     * 是否联网网络
     *
     * @param context
     * @return
     */
    public boolean IsHaveInternet(Context context) {
        try {
            ConnectivityManager manger = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo info = manger.getActiveNetworkInfo();
            return (info != null && info.isConnected());
        } catch (Exception e) {
            return false;
        }
    }
    void istoken(){
        progressBar = findViewById(R.id.loading);
        showProgressBar();
        String token1=aCache.getAsString("token");//从缓存中取出token数据
        //token不为空则直接进入主界面
        if (token1!=null){
            sendHttp.secretFreeLogin(aCache.getAsString("user_name"),token1,hanlder);
            return;
        }
        hideProgressBar();
    }
    void applyPermission(){//动态权限申请
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.SET_ALARM,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.SCHEDULE_EXACT_ALARM
        };
        List<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, permission)) {
                permissionList.add(permission);
                // 进入到这里代表没有权限.

            }
        }
        if (!permissionList.isEmpty()){
            String[] applypermissions = permissionList.toArray(new String[permissionList.
                    size()]);

            ActivityCompat.requestPermissions(LoginActivity.this, applypermissions, 1);
        }

    }

    /*权限申请的回调*/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }

                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }
    private static final int REQUEST_CODE = 1;
    //判断权限
    private boolean commonROMPermissionCheck(Context context) {
        Boolean result = true;
        if (Build.VERSION.SDK_INT  == 23) {
            try {
                Class clazz = Settings.class;
                Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
                result = (Boolean) canDrawOverlays.invoke(null, context);
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        return result;
    }
    //申请权限 请求应用程序悬浮窗权限。这
    private void requestAlertWindowPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_CODE);
    }
    @Override
//处理回调
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                Log.i(TAG, "onActivityResult granted");
            }
        }
    }
    // 显示进度条时调用
    private void addOverlay() {
        // 获取根布局
        RelativeLayout drawerLayout = findViewById(R.id.container);
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
            RelativeLayout drawerLayout = findViewById(R.id.container);
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseHelper.close();
        ActivityCollector.removeActivity(this);
    }
}