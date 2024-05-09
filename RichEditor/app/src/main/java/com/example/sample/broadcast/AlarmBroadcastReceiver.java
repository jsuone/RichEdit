package com.example.sample.broadcast;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.example.sample.Impl.OnClockDataChangeListener;
import com.example.sample.R;
import com.example.sample.util.Constant;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    private Context mContext;
    private NotificationManager notificationManager;
    private Notification notification;
    private NotificationChannel notificationChannel;
    private final static String NOTIFICATION_CHANNEL_ID = "CHANNEL_ID";
    private final static String NOTIFICATION_CHANNEL_NAME = "CHANNEL_NAME";


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("AlarmBroadcastReceiverpp",action);
        if (action.equals(Constant.clock_start_action)) {
            mContext = context;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                String textname = intent.getStringExtra("backlog_name");
                long[] pattern = {0, 1000, 1000};
                /*数组第一个参数表示延迟震动时间
第二个参数表示震动持续时间
第三个参数表示震动后的休眠时间
第四个参数又表示震动持续时间
第五个参数也表示正到休眠时间
以此类推*/

                String guid = intent.getStringExtra("guid");
                notificationChannel = new NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        NOTIFICATION_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(pattern);
                notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                notificationManager.createNotificationChannel(notificationChannel);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_ALL | Notification.DEFAULT_SOUND)
                        .setFullScreenIntent(null, true).setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setWhen(System.currentTimeMillis());

                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.my_notification_layout);//远程视图
                remoteViews.setTextViewText(R.id.notification_text, textname);

                Intent cancelIntent = new Intent();
                cancelIntent.setAction(Constant.clock_have_notification);
                cancelIntent.putExtra("guid",guid);
                cancelIntent.putExtra("change",false);
                cancelIntent.putExtra("extraTime",true);
                PendingIntent cancelPI = PendingIntent.getBroadcast(context,guid.hashCode()+1,cancelIntent,PendingIntent.FLAG_CANCEL_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.notification_clock_cancel,cancelPI);

                Intent confirmIntent = new Intent();
                confirmIntent.setAction(Constant.clock_have_notification);
                confirmIntent.putExtra("guid",guid);
                confirmIntent.putExtra("change",true);
                cancelIntent.putExtra("extraTime",false);
                PendingIntent confirmPI = PendingIntent.getBroadcast(context,guid.hashCode()+2,confirmIntent,PendingIntent.FLAG_CANCEL_CURRENT);
                remoteViews.setOnClickPendingIntent(R.id.notification_clock_confirm,confirmPI);
                //一旦触发就将此提醒设置为过期未完成，这是初始状态，如果用户没有动作状态就是对的，用户有动作就会触发广播，状态也会改变，
                OnClockDataChangeListener listener = (OnClockDataChangeListener) context;
                listener.notificaitonDataChangeListener(guid,false,false);

                notificationBuilder.setCustomContentView(remoteViews);
                notification = notificationBuilder.build();
                notification.headsUpContentView = remoteViews;//手机打开横幅通知
                notificationManager.notify(guid.hashCode(), notification);

            }

        }else if(action.equals(Constant.clock_have_notification)){
            //通知已经完成任务
            String guid = intent.getStringExtra("guid");
            Boolean change = intent.getBooleanExtra("change",false);
            Boolean extraTime = intent.getBooleanExtra("extraTime",false);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                int notificationId = guid.hashCode();
                notificationManager.cancel(notificationId);
            }
            OnClockDataChangeListener listener = (OnClockDataChangeListener) context;
            listener.notificaitonDataChangeListener(guid,change,extraTime);
        }
    }

}