package com.example.alarmclock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyJobService extends JobService {
    private NotificationManager notificationManager;
    private Notification notification;
    private NotificationChannel notificationChannel;
    private final static String NOTIFICATION_CHANNEL_ID = "CHANNEL_ID";
    private final static String NOTIFICATION_CHANNEL_NAME = "CHANNEL_NAME";
    @Override
    public boolean onStartJob(JobParameters params) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("标题：测试文案")
                    .setContentText("内容：你好,点击打开app主页")
                    .build();
        }
        notificationManager.notify(021,notification);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            // 如果当前设备大于 7.0 , 延迟 5 秒 , 再次执行一次
            //startJob(this);
        }
        return false;

    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
    public static void startJob(Context context,String time){
        // 创建 JobScheduler
        JobScheduler jobScheduler =
                (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date clockTime = null;
        try {
             clockTime = format.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date currentTime = new Date();
        Long temp = clockTime.getTime()-currentTime.getTime();
        // 第一个参数指定任务 ID
        // 第二个参数指定任务在哪个组件中执行
        // setPersisted 方法需要 android.permission.RECEIVE_BOOT_COMPLETED 权限
        // setPersisted 方法作用是设备重启后 , 依然执行 JobScheduler 定时任务
        JobInfo.Builder jobInfoBuilder = new JobInfo.Builder(10,
                new ComponentName(context.getPackageName(), MyJobService.class.getName()))
                .setPersisted(true);

        // 7.0 以下的版本, 可以每隔 5000 毫秒执行一次任务
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
            jobInfoBuilder.setPeriodic(5_000);

        }else{
            // 7.0 以上的版本 , 设置延迟 5 秒执行
            // 该时间不能小于 JobInfo.getMinLatencyMillis 方法获取的最小值
            jobInfoBuilder.setMinimumLatency(temp);
        }

        // 开启定时任务
        jobScheduler.schedule(jobInfoBuilder.build());

    }

}
