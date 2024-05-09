package com.example.alarmclock;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class BacklogCompileActivity extends AppCompatActivity implements View.OnClickListener{
    private BacklogSetting backlogSetting;
    private ImageButton back;
    private ImageButton gou;
    private Group.Item itemData;//当前的数据源
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backlog_compile);
        initData();
        initView();

    }
    void initData(){
        Intent intent = getIntent();
        Gson gson = new Gson();
        itemData = gson.fromJson(intent.getStringExtra("itemdata"), Group.Item.class);

    }
    void initView(){
        backlogSetting = findViewById(R.id.backlog_setting_compile);
        back = findViewById(R.id.backlog_compile_back);
        gou = findViewById(R.id.backlog_compile_gou);

        back.setOnClickListener(this);
        gou.setOnClickListener(this);
        if(itemData!=null){
            //修改
            backlogSetting.setCheckBox(itemData.isChecked());
            backlogSetting.setRepeat(itemData.getRepeat());
            backlogSetting.setEditText(itemData.getBacklog_name());
            backlogSetting.setDateTimePicker(itemData.getBacklog_time());

        }
        else{
            //创建阶段 禁止表示任务已经完成
            backlogSetting.setEnableCheckBox(false);
                Date date = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String currentTime = format.format(date);
                backlogSetting.setDateTimePicker(currentTime);
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.backlog_compile_back){
            setResult(Activity.RESULT_FIRST_USER);
            finish();
        }else if(id == R.id.backlog_compile_gou){
            Intent intent = new Intent();
            Gson gson = new Gson();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 获取当前时间
            Date currentDate = new Date();
            // 格式化当前时间
            String create_time = sdf.format(currentDate);
            String uuid;
            if(itemData==null){
                uuid = UUID.randomUUID().toString();
                itemData = new Group.Item(uuid,backlogSetting.getBacklogName(),backlogSetting.getFormat(),
                        backlogSetting.getIsChecked(),backlogSetting.getIsRepeat(),false,create_time,"");
                intent.putExtra("result",gson.toJson(itemData));
                setResult(Activity.RESULT_OK,intent);
            }else{
                //TODO 已经完成或者时间已经过去但是重复 需要创建一个新的并且这个的重复设置为false
                uuid = itemData.getUUID();
                itemData.setChecked(backlogSetting.getIsChecked());
                itemData.setBacklog_name(backlogSetting.getBacklogName());
                itemData.setBacklog_time(backlogSetting.getFormat());
                itemData.setRepeat(backlogSetting.getIsRepeat());
                itemData.setUpdateTime(create_time);
                intent.putExtra("result",gson.toJson(itemData));
                setResult(Activity.RESULT_CANCELED,intent);
            }
            backlogSetting.setClock(uuid,backlogSetting.getIsChecked());
            finish();
        }
    }
}