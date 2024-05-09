package com.example.sample.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;
import com.example.sample.R;
import com.example.sample.util.ActivityCollector;
import com.example.sample.util.Constant;

import java.util.List;

public class SetLockActivity extends AppCompatActivity {
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private PatternLockView mPatternLockView;
    private PatternLockViewListener mPatternLockViewListener = new PatternLockViewListener() {
        @Override
        public void onStarted() {
            Log.d(getClass().getName(), "Pattern drawing started");
        }

        @Override
        public void onProgress(List<PatternLockView.Dot> progressPattern) {
            Log.d(getClass().getName(), "Pattern progress: " +
                    PatternLockUtils.patternToString(mPatternLockView, progressPattern));
        }

        @Override
        public void onComplete(List<PatternLockView.Dot> pattern) {
            Log.d(getClass().getName(), "Pattern complete: " +
                    PatternLockUtils.patternToString(mPatternLockView, pattern));
            String patternToString = PatternLockUtils.patternToString(mPatternLockView, pattern);
            if(isSet){
                //进入设置
                editor.putString(Constant.SP_LOCK_KEY,patternToString);
                editor.commit();
                Toast.makeText(SetLockActivity.this,"设置成功", Toast.LENGTH_SHORT).show();
                ActivityCollector.destroyActivity(SetLockActivity.this);
                return;
            }

            if(!TextUtils.isEmpty(patternToString)&&!isSet){
                if(patternToString.equals(password)){
                    //判断为正确 进入设置模式
                    mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                    isSet = true;
                    set_profile_name.setText(R.string.set_lock);
                }else {
                    mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);

                }
            }
            //1s后清除图案
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPatternLockView.clearPattern();
                }
            },1000);

        }

        @Override
        public void onCleared() {
            Log.d(getClass().getName(), "Pattern has been cleared");
        }
    };
    Boolean isSet = false;
    private TextView set_profile_name;
    private ImageButton button;
    public String password = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_lock);
        ActivityCollector.addActivity(this);
        set_profile_name = findViewById(R.id.set_profile_name);
        button = findViewById(R.id.set_lock_back);
        sp = getSharedPreferences(Constant.SP_KEY,MODE_PRIVATE);
        editor = sp.edit();
        password = sp.getString(Constant.SP_LOCK_KEY,"");
        if(password.equals("")){
            isSet = true;
            set_profile_name.setText(R.string.set_lock);
        }else{
            isSet = false;
            set_profile_name.setText(R.string.input_lock);
        }
        mPatternLockView = (PatternLockView) findViewById(R.id.set_patter_lock_view);
        mPatternLockView.addPatternLockListener(mPatternLockViewListener);
        button.setOnClickListener(v->{
            ActivityCollector.destroyActivity(this);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}