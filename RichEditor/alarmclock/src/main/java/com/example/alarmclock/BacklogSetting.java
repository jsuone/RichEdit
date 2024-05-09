package com.example.alarmclock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TimeUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.loper7.date_time_picker.DateTimePicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.FunctionImpl;

public class BacklogSetting extends LinearLayout {
    private DateTimePicker dateTimePicker;
    private CheckBox checkBox;
    private CheckBox repeat;
    private EditText editText;
    AlarmManager alarmManager;
    Context context;
    Date triggerAtMillis;//选择时间的毫秒
    String format;//字符串形式的选择时间
    public static String TAG = "notedate";
    public BacklogSetting(Context context) {
        this(context,null);
    }

    public BacklogSetting(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BacklogSetting(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }
    void initView(){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.module_backlog_setting,null);

        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        dateTimePicker = view.findViewById(R.id.picker);
        checkBox = view.findViewById(R.id.backlog_checked);
        repeat = view.findViewById(R.id.backlog_repeat);
        editText = view.findViewById(R.id.backlog_edit);
        dateTimePicker.setLayout(R.layout.layout_date_picker_segmentation);

        dateTimePicker.setOnDateTimeChangedListener(new Function1<Long, Unit>() {
            @Override
            public Unit invoke(Long aLong) {//kotin 中unit对应java中void
                triggerAtMillis  = new Date(aLong);
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                format = fmt.format(triggerAtMillis);

                return null;
            }
        }) ;

        addView(view);
    }
    void setCheckBox(boolean isChecked){
        checkBox.setChecked(isChecked);
    }
    void setRepeat(Boolean isRepeat){ repeat.setChecked(isRepeat);}
    void setEditText(String text){
        editText.setText(text);
    }
    void setDateTimePicker(String time){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date date = format.parse(time);
            dateTimePicker.setDefaultMillisecond(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    void setEnableCheckBox(Boolean enableCheckBox){ checkBox.setEnabled(enableCheckBox);}
    Boolean getIsRepeat(){
        return repeat.isChecked();
    }
    Boolean getIsChecked(){
        return checkBox.isChecked();
    }
    String getFormat(){
        return format;
    }
    String getBacklogName(){
        return editText.getText().toString();
    }
    void setClock(String uuid,boolean isChecked){
        if(isChecked){
            //已经完成
            return;
        }
        Date date = new Date();//现在的时间
        if(date.after(triggerAtMillis)){
            Toast.makeText(context,"当前设定提醒已过期",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("time",format);
        intent.putExtra("backlog_name",getBacklogName());
        intent.putExtra("guid",uuid);
        intent.setAction("com.example.package.clock");
        PendingIntent pendingIntent =
        PendingIntent.getBroadcast(getContext(),uuid.hashCode(),intent,PendingIntent.FLAG_CANCEL_CURRENT);
        //PendingIntent.getService(getContext(),uuid.hashCode(),intent,PendingIntent.FLAG_CANCEL_CURRENT);
        //第二个参数是每次requestcode不同,就能产生多个Pendingintent.
/*
        1.FLAG_CANCEL_CURRENT:如果AlarmManager管理的PendingIntent已经存在,那么将会取消当前的PendingIntent，从而创建一个新的PendingIntent.

        2.FLAG_UPDATE_CURRENT:如果AlarmManager管理的PendingIntent已经存在,让新的Intent更新之前Intent对象数据,
                例如更新Intent中的Extras,另外,我们也可以在PendingIntent的原进程中调用PendingIntent的cancel ()把其从系统中移除掉

        3.FLAG_NO_CREATE:如果AlarmManager管理的PendingIntent已经存在,那么将不进行任何操作,若描述的Intent不存直接返回NULL(空）.

        4.FLAG_ONE_SHOT:该PendingIntent只作用一次.在该PendingIntent对象通过send()方法触发过后,PendingIntent将自动调用cancel()进行销毁,那么如果你再调用send()方法的话将会失败,系统将会返回一个SendIntentException.
*/

        alarmManager.set(AlarmManager.RTC_WAKEUP,triggerAtMillis.getTime(),pendingIntent);//如果是过去会立刻触发

    }
    public static void setClock(Group.Item item,Context context){
        if(item.isChecked()){
            //已经完成
            return;
        }
        AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
      // AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Date date = new Date();//现在的时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date triggerAtMillis = null;
        try {
            triggerAtMillis = format.parse(item.getBacklog_time());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(date.after(triggerAtMillis)){
            Toast.makeText(context,"当前设定提醒已过期",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("backlog_name",item.getBacklog_name());
        intent.putExtra("guid",item.getUUID());
        intent.setAction("com.example.package.clock");
        Toast.makeText(context,"setClock: 当前intent的标识码"+item.getUUID().hashCode(),Toast.LENGTH_SHORT).show();
        Log.d(TAG, "setClock: 当前intent的标识码"+item.getUUID().hashCode()+context.toString());
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(context,item.getUUID().hashCode(),intent,PendingIntent.FLAG_CANCEL_CURRENT);
        //PendingIntent.getService(getContext(),uuid.hashCode(),intent,PendingIntent.FLAG_CANCEL_CURRENT);
        //第二个参数是每次requestcode不同,就能产生多个Pendingintent.
/*
        1.FLAG_CANCEL_CURRENT:如果AlarmManager管理的PendingIntent已经存在,那么将会取消当前的PendingIntent，从而创建一个新的PendingIntent.

        2.FLAG_UPDATE_CURRENT:如果AlarmManager管理的PendingIntent已经存在,让新的Intent更新之前Intent对象数据,
                例如更新Intent中的Extras,另外,我们也可以在PendingIntent的原进程中调用PendingIntent的cancel ()把其从系统中移除掉

        3.FLAG_NO_CREATE:如果AlarmManager管理的PendingIntent已经存在,那么将不进行任何操作,若描述的Intent不存直接返回NULL(空）.

        4.FLAG_ONE_SHOT:该PendingIntent只作用一次.在该PendingIntent对象通过send()方法触发过后,PendingIntent将自动调用cancel()进行销毁,那么如果你再调用send()方法的话将会失败,系统将会返回一个SendIntentException.
*/

        alarmManager.setExact(AlarmManager.RTC_WAKEUP,triggerAtMillis.getTime(),pendingIntent);//如果是过去会立刻触发
    }
   public static  void removePendingIntent(String guid,Context context){
        //context.getApplicationContext().getSystemService() 因为定时无法取消怀疑是上下文不一致
       AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        //AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
       Toast.makeText(context,"removePendingIntent: 当前intent的标志码"+guid.hashCode(),Toast.LENGTH_SHORT).show();
       Log.d(TAG, "removePendingIntent: 当前intent的标志码"+guid.hashCode()+context.toString());
       Intent intent = new Intent();
       intent.setAction("com.example.package.clock");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,guid.hashCode(),intent,PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
   }
}
