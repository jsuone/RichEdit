package com.example.sample.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.app.UiAutomation;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.sample.R;
import com.example.sample.communication.SendHttp;
import com.example.sample.fragment.CheckSecurityFragment;
import com.example.sample.fragment.SetSecurityFragment;
import com.example.sample.util.ACache;
import com.example.sample.util.ActivityCollector;

import java.lang.ref.WeakReference;

public class SecurityQuestionActivity extends AppCompatActivity implements View.OnClickListener {
//如果没有密保 设置密保 如果有密保需要输入上次密保问题答案
    private ImageButton back;
    private WeakReference<FragmentTransaction> fragmentTransactionWeakReference;
    private SendHttp sendHttp = new SendHttp();
    private ACache aCache;
    private CheckSecurityFragment securityFragment;
    private SetSecurityFragment setSecurityFragment;
    class SecurityHandler extends Handler{
        private final WeakReference<Activity> mActivity;
        SecurityHandler(Activity activity) {
            mActivity = new WeakReference<Activity>(activity);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            fragmentTransactionWeakReference = new WeakReference<>(getSupportFragmentManager().beginTransaction());//事务处理
            switch (msg.what){
                case 11://有密保问题
                    Bundle bundle1 = msg.getData();
                    securityFragment.setSecurityQuestionView(bundle1.getString("question"));
                    fragmentTransactionWeakReference.get().add(R.id.set_security_question_fragment_list,securityFragment).commit();

                    break;
                case 12://未设置问题
                    if(securityFragment.isAdded()){
                        fragmentTransactionWeakReference.get().hide(securityFragment).add(R.id.set_security_question_fragment_list,setSecurityFragment).commit();
                    }else{
                        fragmentTransactionWeakReference.get().add(R.id.set_security_question_fragment_list,setSecurityFragment).commit();
                    }
                    break;
                case 13://网络异常
                    Toast.makeText(getApplication(),"网络异常",Toast.LENGTH_SHORT).show();
                case 41://修改完毕

                    Activity activity = mActivity.get();
                    if (activity != null) {
                        ActivityCollector.destroyActivity(activity);
                    }
                    break;
                case 21://答案正确
                    fragmentTransactionWeakReference.get().hide(securityFragment).add(R.id.set_security_question_fragment_list,setSecurityFragment).commit();
                    break;
                case 22://答案错误
                    securityFragment.setSecurityAnswerViewError("答案错误");
                    break;
            }
        }
    }
    SecurityHandler handler ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_security_question);
        handler = new SecurityHandler(this);
        aCache = ACache.get(this);
        initView();
    }
    void initView(){
        back = findViewById(R.id.set_security_question_back);
        fragmentTransactionWeakReference = new WeakReference<>(getSupportFragmentManager().beginTransaction());//事务处理
        securityFragment = new CheckSecurityFragment(handler);
        setSecurityFragment = new SetSecurityFragment(handler);
        String username = aCache.getAsString("user_name");
        String token = aCache.getAsString("token");
        sendHttp.checkSecurityQuestion(username,token,handler);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.set_security_question_back:
                ActivityCollector.destroyActivity(this);
                break;
        }
    }
}