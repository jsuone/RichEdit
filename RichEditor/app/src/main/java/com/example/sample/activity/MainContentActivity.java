package com.example.sample.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.sample.Impl.OnClockDataChangeListener;
import com.example.sample.Impl.OnNoteListChangeListener;
import com.example.sample.Impl.SyncManageAction;
import com.example.sample.broadcast.AlarmBroadcastReceiver;
import com.example.sample.communication.SendHttp;
import com.example.sample.fragment.BackLogListFragment;
import com.example.sample.fragment.MainFragment;
import com.example.sample.model.User;
import com.example.sample.service.BackgroundUpdateService;
import com.example.sample.util.ACache;
import com.example.sample.util.ActivityCollector;
import com.example.sample.util.Constant;
import com.example.sample.util.ImageUtils;
import com.example.sample.util.MyDatabaseHelper;
import com.example.sample.Impl.OnNoteThemeSelectedListener;
import com.example.sample.R;
import com.google.android.material.tabs.TabLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainContentActivity extends AppCompatActivity implements OnNoteThemeSelectedListener, OnClockDataChangeListener, SyncManageAction , OnNoteListChangeListener {

    private DrawerLayout drawerLayout;
    private TabLayout tabLayout;
    private List<Fragment> fragmentList;
    private WeakReference<FragmentTransaction> fragmentTransactionWeakReference;
    private Integer pos_tab = null;//当前页面
    private MyDatabaseHelper databaseHelper;
    private AlarmBroadcastReceiver broadcastReceiver;
    private ServiceConnection connection;//服务的链接
    private BackgroundUpdateService updateService;
    private BackgroundUpdateReceiver backgroundUpdateReceiver;
    private ProgressBar progressBar;
    private TextView appOnlinestatus;
    private TextView nv_title;
    View overlayView;
    private ACache aCache;
    private SendHttp sendHttp = new SendHttp();
    private Dialog dialog;
    private Dialog passwordChangeDialog;
    private Dialog initFailedDialog;
    private static String TAG = "notedata";

    private class MainContentHandler extends Handler{
        private Context context;
        void setContext(Context context){
            this.context = context;
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            switch (msg.what){
                case 2://免密成功 网络已经正常 重新发起长连接
                    String token1 = bundle.getString("token");
                    aCache.put("token",token1);
                    //查询数据库中的user信息
                    if(aCache.getAsString("user_name")==null){
                        User user1 = databaseHelper.queryUser();
                        aCache.put("user_ssn",user1.getSsn());
                        aCache.put("user_name",user1.getUsername());
                        aCache.put("lastUpdateSSN",user1.getLastUpdateSSN());
                        aCache.put("lastSyncTime",user1.getLastSyncTime());
                    }
                    updateService.initWebSocketClient();
                    break;
                case 3://免密失败
                    hideProgressBar();
                    passwordChangeDialog.show();
                    break;
                case 4://已有用户登录 跳出弹窗提示 退出应用清除数据
                    hideProgressBar();
                    dialog.show();
                    break;
                case 6://网络问题
                    hideProgressBar();
                    int ssn = (int)aCache.getAsObject("user_ssn");
                    int lastSSN =  (int) aCache.getAsObject("lastUpdateSSN");
                    if(ssn>lastSSN){
                        int msgs = R.string.offline_need_sync_status;
                        appOnlinestatus.setText(msgs);
                        appOnlinestatus.setTextColor(Color.RED);
                    }
                    Toast.makeText(MainContentActivity.this,"链接失败请检查网络",Toast.LENGTH_SHORT).show();
                    break;
                case 8://下拉刷新 进入
                    SyncManageAction action = (SyncManageAction)context;
                    action.SyncStart();
            }
        }
    }
    private MainContentHandler handler = new MainContentHandler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);

        refreshData();
        setContentView(R.layout.activity_main_content);
        databaseHelper = MyDatabaseHelper.getInstance(this);
        handler.setContext(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.clock_start_action);
        filter.addAction(Constant.clock_have_notification);
        // 设置优先级为最高
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        //时间提醒广播接受
        broadcastReceiver = new AlarmBroadcastReceiver();

        IntentFilter filter1 = new IntentFilter();
        //filter1.addAction(Constant.Service_START_ACTION);
        filter1.addAction(Constant.Service_END_ACTION);
        filter1.addAction(Constant.Service_FAILED_END_ACTION);
        filter1.addAction(Constant.Login_Grab_ACTION);
        //同步服务广播接受
        backgroundUpdateReceiver = new BackgroundUpdateReceiver();
        registerReceiver(backgroundUpdateReceiver,filter1);
        registerReceiver(broadcastReceiver,filter);
        aCache = ACache.get(this);
        initView();
    }
    void refreshData(){//刷新数据 应当是对所有数据进行一次同步与刷新 各个控件自己再去获取需要对应的数据
        //如果不对所有进行刷新 可能出现在离线进行多次操作，联网提交的数据同步出现疏漏，比如标题进行创建提交，但在另一台联网设备上
         connection = new ServiceConnection() {
           @Override
           public void onServiceConnected(ComponentName name, IBinder service) {
             BackgroundUpdateService.BackgroundUpdateBinder binder = (BackgroundUpdateService.BackgroundUpdateBinder) service;
             updateService = binder.getService();
             updateService.setContext(MainContentActivity.this);

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
};
    }
    void initView(){
        dialog = new AlertDialog.Builder(this).setMessage("已有设备在他处登录，请重新登录").setPositiveButton("知道了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                databaseHelper.deleteAllData();
                aCache.clear();
                ImageUtils.deleteAllImageFile(MainContentActivity.this);
                ActivityCollector.finishAll();
            }
        }).create();
        dialog.setCanceledOnTouchOutside(false);

        passwordChangeDialog = new AlertDialog.Builder(this).setMessage("密码被修改，请重新登录").setPositiveButton("知道了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                databaseHelper.deleteAllData();
                aCache.clear();
                ImageUtils.deleteAllImageFile(MainContentActivity.this);
                ActivityCollector.finishAll();
            }
        }).create();
        passwordChangeDialog.setCanceledOnTouchOutside(false);
        initFailedDialog = new AlertDialog.Builder(this).setMessage("初始化数据失败，请重新登录").setPositiveButton("知道了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                databaseHelper.deleteAllDataNoUser();
                String token = aCache.getAsString("token");
                String userName = aCache.getAsString("user_name");
                int ssn = (int) aCache.getAsObject("user_ssn");
                int lastSSN = (int) aCache.getAsObject("lastUpdateSSN");
                String SyncTime = aCache.getAsString("lastSyncTime");
                aCache.clear();
                aCache.put("token",token);
                aCache.put("user_name",userName);
                aCache.put("user_ssn",ssn);
                aCache.put("lastUpdateSSN",lastSSN);
                aCache.put("lastSyncTime",SyncTime);
                ImageUtils.deleteAllImageFile(MainContentActivity.this);
                ActivityCollector.finishAll();
            }
        }).create();
        initFailedDialog.setCanceledOnTouchOutside(false);
        //使用一个进度条覆盖其他界面显示最高级 当初始化数据完成再结束进度条
        Intent intent = new Intent(this, BackgroundUpdateService.class);
        startService(intent);//异步操作
        bindService(intent,connection, Context.BIND_AUTO_CREATE);//这是异步操作

        //显示加载
        progressBar = findViewById(R.id.update_progressBar);
        nv_title = findViewById(R.id.nv_title);
        drawerLayout = findViewById(R.id.drawer_layout);
        tabLayout = findViewById(R.id.bottom_tab_layout);
        appOnlinestatus = findViewById(R.id.app_online_status);
        fragmentList = new ArrayList<>();
        MainFragment mainFragment = new MainFragment();
        mainFragment.initData(drawerLayout);
        fragmentList.add(mainFragment);

        BackLogListFragment backLogListFragment = new BackLogListFragment();
        fragmentList.add(backLogListFragment);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                fragmentTransactionWeakReference = new WeakReference<>(getSupportFragmentManager().beginTransaction());//事务处理
                if(pos_tab!=null){//如果之前已经选择过
                    Fragment preFragment = fragmentList.get(pos_tab);
                    Fragment fragment = fragmentList.get(tab.getPosition());
                    if(fragment.isAdded()){//Return true if the fragment is currently added to its activity.
                        fragmentTransactionWeakReference.get().hide(preFragment).show(fragment).commit();
                    }else{
                        fragmentTransactionWeakReference.get().hide(preFragment).add(R.id.fragment_content,fragment).commit();
                    }
                }else{//初始页面
                    fragmentTransactionWeakReference.get().add(R.id.fragment_content,fragmentList.get(0)).commit();
                    WeakReference<FragmentTransaction> fragmentTransactionWeakReference1 = new WeakReference<>(getSupportFragmentManager().beginTransaction());
                    fragmentTransactionWeakReference1.get().add(R.id.fragment_content,fragmentList.get(1)).commit();
                    WeakReference<FragmentTransaction> fragmentTransactionWeakReference2 = new WeakReference<>(getSupportFragmentManager().beginTransaction());
                    fragmentTransactionWeakReference2.get().hide(fragmentList.get(1)).show(fragmentList.get(0)).commit();
                }
                pos_tab = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.home).setText("首页"));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.backlog).setText("待办"));
        // 创建一个 SpannableStringBuilder 对象，内容为 "欢迎"+userName
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder("欢迎 " + aCache.getAsString("user_name"));

// 使用 TypefaceSpan 设置艺术字，这里以serif为例
        TypefaceSpan typefaceSpan = new TypefaceSpan("serif");

// 使用 ForegroundColorSpan 设置颜色
        int color = ContextCompat.getColor(this, R.color.teal_200);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);

// 在 "欢迎 "这个部分文字上设置样式
        spannableStringBuilder.setSpan(typefaceSpan, 0, 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(colorSpan, 0, 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        nv_title.setText(spannableStringBuilder);
        showProgressBar();
    }

    @Override
    public void onNoteThemeSelected(List<String> titleGUID) {
        MainFragment mainFragment = (MainFragment) fragmentList.get(0);
        mainFragment.showNoteByTitleGUID(titleGUID);
    }

    @Override
    public void onNoteThemeDeleted(List<String> titleGUID) {
        MainFragment mainFragment = (MainFragment) fragmentList.get(0);
        mainFragment.deleteNoteByTitleGUID(titleGUID);
    }

    @Override
    public void updateThemeList(List<String> titleGUID) {
        MainFragment mainFragment = (MainFragment) fragmentList.get(0);
        mainFragment.updateTitleGUID(titleGUID);
    }

    @Override
    public void deleteNoteByNoteID(List<String> noteGUID) {
        MainFragment mainFragment = (MainFragment) fragmentList.get(0);
        mainFragment.deleteNoteByNoteID(noteGUID);
    }

    @Override
    public void setTopTile(String uuid) {
        MainFragment mainFragment = (MainFragment) fragmentList.get(0);
        mainFragment.setTopTitle(uuid);
    }

    @Override
    public void finishLoadMore() {
        MainFragment mainFragment = (MainFragment) fragmentList.get(0);
        mainFragment.finishLoadMore();
    }

    @Override
    public void finishLoadMoreNoData() {
        MainFragment mainFragment = (MainFragment) fragmentList.get(0);
        mainFragment.finishLoadMoreNoData();
    }

    @Override
    public void setNoMoreData() {
        MainFragment mainFragment = (MainFragment) fragmentList.get(0);
        mainFragment.setNoMoreData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(backgroundUpdateReceiver);
        unbindService(connection);
        Log.d(TAG, "onDestroy: 服务解绑");
    }

    @Override
    public void notificaitonDataChangeListener(String uuid, Boolean change,Boolean extraTime) {//触发了提醒事件的回调
        BackLogListFragment backLogListFragment = (BackLogListFragment) fragmentList.get(1);
        backLogListFragment.addItemToGroup(uuid,change,extraTime);
    }

    @Override
    public void itemDataChangeListener(String uuid, Boolean change) {
        BackLogListFragment backLogListFragment = (BackLogListFragment) fragmentList.get(1);
        backLogListFragment.updateItemOfGroup(uuid,change,null);
    }

    @Override
    public void deleteItemDataLinstener(String uuid) {
        BackLogListFragment backLogListFragment = (BackLogListFragment) fragmentList.get(1);
       backLogListFragment.deleteItemData(uuid);
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
    // 显示进度条时调用
    private void addOverlay() {
        // 获取根布局
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
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
            DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
            ViewGroup rootView = (ViewGroup) drawerLayout.getRootView();
            // 移除遮罩层
            rootView.removeView(overlayView);
            overlayView = null; // 释放资源
        }
    }
    // 显示进度条时调用
    private void showProgressBar() {
        if(progressBar.getVisibility()!=View.VISIBLE){
            progressBar.setVisibility(View.VISIBLE);
            addOverlay(); // 添加遮罩层
        }

    }
    // 隐藏进度条时调用
    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        removeOverlay(); // 移除遮罩层
    }

    @Override
    public synchronized void SyncStart() {
        showProgressBar();
        if(updateService.isOffConnection()){
            //长连接断开 先检查网络状态 有网再验证一下登录状态 正常再度发起长连接 不正常比如密码被用户在其他设备登录修改 提醒当前用户 清除数据退出应用
            //有其他用户正在使用 弹出下线 因为离线不退出 其他用户能够登录此账户肯定是确认服务器发送了下线通知给当前设备
            //如果未登录 说明有其他用户登录此账户并退出登录状态
            if(IsHaveInternet(this)){
                //有网 验证登录状态 进入主页面 如果是离线肯定有token
                String username = aCache.getAsString("user_name");
                String token = aCache.getAsString("token");
                sendHttp.secretFreeLogin(username,token,handler);
                return;
            }else
            {
                hideProgressBar();
                int ssn = (int)aCache.getAsObject("user_ssn");
                int lastSSN =  (int) aCache.getAsObject("lastUpdateSSN");
                if(ssn>lastSSN){
                int msg = R.string.offline_need_sync_status;
                    appOnlinestatus.setText(msg);
                    appOnlinestatus.setTextColor(Color.RED);
            }
                Toast.makeText(this,"请检查网络状态",Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Random rand = new Random();
        // 生成0.3秒（300毫秒）到1秒（1000毫秒）之间的随机毫秒数
        int delay = rand.nextInt(700) + 300;
      /*  try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        updateService.syncDataStart();
    }

    @Override
    public void SyncStop() {
        updateService.syncDataStop();
    }
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

    @Override
    public MainContentHandler getHandler() {
        return handler;
    }

    class BackgroundUpdateReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "主界面 onReceive: 接收到广播"+intent.getAction());
/*            //接收到服务启动完成的广播  由于本身是在子线程启动长连接，可能服务启动完成发送广播，长连接还未开始 所以放弃
            if(intent.getAction().equals(Constant.Service_START_ACTION)){
                //应用进入后 开始同步数据
                Log.d(TAG, "主界面 onReceive: 开始启动同步服务");
                updateService.syncDataStart();
            }else*/
                if(intent.getAction().equals(Constant.Service_END_ACTION)){
                Log.d(TAG, "主界面 onReceive: 同步服务完成");
                //int color = ContextCompat.getColor(context, R.color.);
                appOnlinestatus.setText(R.string.online_status);
                appOnlinestatus.setTextColor(Color.GREEN);
                hideProgressBar();
            }else if(intent.getAction().equals(Constant.Login_Grab_ACTION)){
                    //登录冲突
                    dialog.show();
                    databaseHelper.deleteAllData();
                    aCache.clear();
                    ActivityCollector.finishAll();
                }
                else if(intent.getAction().equals(Constant.Service_FAILED_END_ACTION)){
                    hideProgressBar();
                    //如果是用户密码登录需要服务器传过来的数据 此时同步时间由于图片可能时间有点长，如果同步失败需要提醒用户重新登录进行初始化
                    //如果是退出登录可能链接关闭后，缓存清除才收到广播 内容会为空进行处理
                    int msg = R.string.offline_status;
                    if( aCache.getAsObject("user_ssn")!=null&&aCache.getAsObject("lastUpdateSSN")!=null){
                        int ssn = (int)aCache.getAsObject("user_ssn");
                        int lastSSN =  (int) aCache.getAsObject("lastUpdateSSN");
                        if(ssn==0&&lastSSN==0){
                            initFailedDialog.show();
                        }else if(ssn>lastSSN){
                            msg = R.string.offline_need_sync_status;

                        }
                    }
                    appOnlinestatus.setText(msg);

                    appOnlinestatus.setTextColor(Color.RED);
                }
        }
    }
}