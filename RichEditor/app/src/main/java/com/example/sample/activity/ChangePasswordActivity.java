package com.example.sample.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.sample.Impl.SyncManageAction;
import com.example.sample.R;
import com.example.sample.communication.SendHttp;
import com.example.sample.fragment.CheckPasswordFragment;
import com.example.sample.fragment.CheckSecurityFragment;
import com.example.sample.fragment.SetPasswordFragment;
import com.example.sample.util.ACache;
import com.example.sample.util.ActivityCollector;
import com.example.sample.util.MyDatabaseHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ChangePasswordActivity extends AppCompatActivity implements View.OnClickListener {
    //如果有密保 验证密保和旧密码再修改密码
    private WeakReference<FragmentTransaction> fragmentTransactionWeakReference;
    private ImageButton back;
    private SendHttp sendHttp = new SendHttp();
    private ACache aCache;
    CheckSecurityFragment securityFragment;
    CheckPasswordFragment passwordFragment;
    SetPasswordFragment setPasswordFragment;
    MyDatabaseHelper databaseHelper;
    class ChangePasswordHandler extends Handler{
        private final WeakReference<Activity> mActivity;
        ChangePasswordHandler(Activity activity) {
            mActivity = new WeakReference<Activity>(activity);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            fragmentTransactionWeakReference = new WeakReference<>(getSupportFragmentManager().beginTransaction());//事务处理
            //处理密保验证和旧密码验证页面的回调
            switch (msg.what){
                case 11://有密保问题
                    Bundle bundle1 = msg.getData();
                    securityFragment.setSecurityQuestionView(bundle1.getString("question"));
                    fragmentTransactionWeakReference.get().add(R.id.changed_password_fragment_list,securityFragment).commit();

                    break;
                case 12://未设置问题
                    if(securityFragment.isAdded()){
                        fragmentTransactionWeakReference.get().hide(securityFragment).add(R.id.changed_password_fragment_list,passwordFragment).commit();
                    }else{
                        fragmentTransactionWeakReference.get().add(R.id.changed_password_fragment_list,passwordFragment).commit();
                    }
                    break;
                case 13://网络异常
                    Toast.makeText(getApplication(),"网络异常",Toast.LENGTH_SHORT).show();
                case 41://修改完毕
                    Activity activity = mActivity.get();
                    if (activity != null) {
                     ActivityCollector.destroyActivity(activity);
                    }
                    if(msg.what!=41){
                        //网络异常不执行退出用新密码登录
                        break;
                    }
                    String username = aCache.getAsString("user_name");
                    String token = aCache.getAsString("token");
                    sendHttp.loginOut(username,token);
                    databaseHelper.deleteAllData();
                    aCache.clear();
                    ActivityCollector.finishAll();
                    break;
                case 21://答案正确
                    fragmentTransactionWeakReference.get().hide(securityFragment).add(R.id.changed_password_fragment_list,passwordFragment).commit();
                    break;
                case 22://答案错误
                    securityFragment.setSecurityAnswerViewError("答案错误");
                    break;
                case 31://密码正确
                    fragmentTransactionWeakReference.get().hide(passwordFragment).add(R.id.changed_password_fragment_list,setPasswordFragment).commit();
                    break;
                case 32://密码错误
                    passwordFragment.setCheckPasswordViewError("密码错误");
                    break;
            }
        }
    }
    private ChangePasswordHandler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_change_password);
        aCache = ACache.get(this);
        databaseHelper =MyDatabaseHelper.getInstance(this);
        handler  = new ChangePasswordHandler(this);
        initView();
    }
    void initView(){
        back = findViewById(R.id.changed_password_back);
        fragmentTransactionWeakReference = new WeakReference<>(getSupportFragmentManager().beginTransaction());//事务处理

        securityFragment = new CheckSecurityFragment(handler);
        passwordFragment = new CheckPasswordFragment(handler);
        setPasswordFragment = new SetPasswordFragment(handler);
        String username = aCache.getAsString("user_name");
        String token = aCache.getAsString("token");
        sendHttp.checkSecurityQuestion(username,token,handler);

        back.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.changed_password_back:
                ActivityCollector.destroyActivity(this);
                break;
        }
    }
}