package com.example.sample.service;

import static com.example.sample.util.SyncMoudle.IN_SYNC_PROGRESS;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.icu.text.CaseMap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.alarmclock.Group;
import com.example.sample.activity.MainContentActivity;
import com.example.sample.communication.DefaultWebSocketListener;
import com.example.sample.model.NoteModel;
import com.example.sample.model.TitleModel;
import com.example.sample.model.User;
import com.example.sample.util.ACache;
import com.example.sample.util.ByteArrayMapConverter;
import com.example.sample.util.Constant;
import com.example.sample.util.DataManager;
import com.example.sample.util.DateTime;
import com.example.sample.util.GlideApp;
import com.example.sample.util.ImageUtils;
import com.example.sample.util.MyDatabaseHelper;
import com.example.sample.util.SyncMoudle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.ByteString;

public class BackgroundUpdateService extends Service {
    private static final String TAG = "notedata";
    WebSocket webSocket;
    ACache aCache ;
    private final IBinder binder =new BackgroundUpdateBinder();
    private MyDatabaseHelper databaseHelper;
    private Gson gson = new Gson();
    private String path = "http://192.168.43.90:8081/res/images/";
    private List<String> redis_image = new ArrayList<>();
    private Map<String,Bitmap> redis_imageMap = new HashMap<>();
    private Context context;
    class UpdateThingsHandler extends Handler{
        //同步 服务返回数据的处理
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 109:
                    //同步初始阶段服务器返回的用户信息
                    Bundle bundle1 = msg.getData();
                    User user = gson.fromJson(bundle1.getString("user"),User.class);
                    aCache.put("redis_user", user);
                    break;
                case 110://case语句中只能写枚举类定义的变量名称
                    //同步进行中 模式发生了变化
                    if(Constant.MODEL==1){
                        //模式为客户端传数据到服务端
                        //反正本地最新 直接放入本地数据库 如果同步失败也是本地最新之后再同步
                        //找出ssn大于等于上次同步序列号的数据就是需要同步的 因为最新同步序列号是大于数据中最大同步序列号一，
                        if(Constant.TRANSPROT_TYPE==SyncMoudle.CHARACTER_TRANSMISSION.code){
                            List<TitleModel> titleModelList = databaseHelper.queryAllNeedSyncTitle((int)aCache.getAsObject("lastUpdateSSN"));
                            InsyncProgressInitData(titleModelList,"title");
                            List<NoteModel> noteModelList = databaseHelper.queryAllNeedSyncNote((int)aCache.getAsObject("lastUpdateSSN"));
                            InsyncProgressInitData(noteModelList,"note");
                            List<Group.Item> itemList = databaseHelper.queryAllNeedSyncItem((int)aCache.getAsObject("lastUpdateSSN"));
                            InsyncProgressInitData(itemList,"clock");
                            //传输图片名字目录
                            Map<String,List<String>> fileNames = ImageUtils.queryImageFileByNoteGUID(getBaseContext(),noteModelList);
                            InsyncProgressInitImageData(fileNames,"image");
                            Constant.TRANSPROT_TYPE = SyncMoudle.PICTURE_TRANSMISSION.code;//进入图片传输
                        }else if(Constant.TRANSPROT_TYPE == SyncMoudle.PICTURE_TRANSMISSION.code){
                            Bundle bundle = msg.getData();
                            String json = bundle.getString("images");
                            //服务器返回的需要上传的图片
                            List<String> images = gson.fromJson(json,new TypeToken<List<String>>() {}.getType());
                            InsyncProgressInitImage(images);
                            Constant.END = 1;//本地数据最新同步 则本地发起结束同步标志 进入同步结束阶段
                            Log.d(TAG, "InsyncProgressInitData: 发送"+"sync_flag_"+Constant.START+"_"+Constant.END+"_"+Constant.MODEL);
                            webSocket.send("sync_flag_"+Constant.START+"_"+Constant.END+"_"+Constant.MODEL);
                        }

                        //一局笔记的uid得到一份笔记的所有图片 如果是更新 先上传所有需要同步的图片名称，
                        // 服务器进行比较 进行删除服务器图片，并返回需要上传的图片名称 安卓端再传输对应图片 设置一个文字图片传输开启的标志位0 是文字json传输
                        // 1是进入图片传输  同步笔记有图片开启 图片传输完成进入结束阶段
                        //也就不立马进入同步完成阶段

                        //TODO 图片上传

                    }
                    break;
                case 111:
                    //服务端发起同步结束 表示图片等数据传输完毕 进行插入数据库 再通过观察者提醒控件更新
                    //更新用户
                   // Bundle bundle3 = msg.getData();
                    Constant.START = 0;
                    Constant.END = 0;
                    Constant.MODEL = 0;
                    Constant.TRANSPROT_TYPE = SyncMoudle.CHARACTER_TRANSMISSION.code;

                    User user2 = (User) aCache.getAsObject("redis_user");
                    String noteJSON = aCache.getAsString("redis_note");
                    String titleJSON = aCache.getAsString("redis_title");
                    String clockJSON = aCache.getAsString("redis_clock");
                    //String imageJSON = bundle3.getString("redis_image");
                    aCache.put("user_ssn",user2.getSsn());
                    aCache.put("lastUpdateSSN",user2.getLastUpdateSSN());
                    aCache.put("lastSyncTime",user2.getLastSyncTime());
                    List<NoteModel> noteModelList = gson.fromJson(noteJSON,new TypeToken<List<NoteModel>>(){}.getType());
                    List<TitleModel> titleModelList = gson.fromJson(titleJSON,new TypeToken<List<TitleModel>>(){}.getType());
                    List<Group.Item> itemList = gson.fromJson(clockJSON,new TypeToken<List<Group.Item>>(){}.getType());
                    //List<String> imageList = mapGson.fromJson(imageJSON,new TypeToken<List<String>>(){}.getType());
                    databaseHelper.beginTransaction();
                    try {
                        //可能免密登录但是服务器数据最新，此时本地还有数据进行删除再更新
                        databaseHelper.deleteAllData();
                        ImageUtils.deleteAllImageFile(getBaseContext());
                        databaseHelper.insertUser(user2);
                        databaseHelper.insertTitleList(titleModelList);
                        databaseHelper.insertNoteList(noteModelList);
                        databaseHelper.insertClockList(itemList);
                        databaseHelper.setTransactionSuccessful();
                    } finally {
                        databaseHelper.endTransaction();
                    }
                    //保存图片
                    ImageUtils.saveImagesFromMap(getBaseContext(),redis_image,redis_imageMap);
                    //提醒各控件更新
                    DataManager.getInstance().notifyObservers(Constant.Note_ALL_KEY,noteModelList);
                    DataManager.getInstance().notifyObservers(Constant.Title_KEY,titleModelList);
                    DataManager.getInstance().notifyObservers(Constant.Clock_ALL_KEY,itemList);//这个控件写的时候刷新直接从数据库查，就不传参了

                    aCache.remove("redis_user");
                    aCache.remove("redis_note");
                    aCache.remove("redis_title");
                    aCache.remove("redis_clock");
                    Intent intent = new Intent();
                    intent.setAction(Constant.Service_END_ACTION);

                    redis_image.clear();
                    //发送同步完成广播
                    sendBroadcast(intent);
                    break;
                case 112:
                    //到了同步结束阶段，客户端发起结束，服务端的回应
                    Constant.START = 0;
                    Constant.END = 0;
                    Constant.MODEL = 0;
                    Constant.TRANSPROT_TYPE = SyncMoudle.CHARACTER_TRANSMISSION.code;
                    User user1 = (User) aCache.getAsObject("redis_user");
                    user1.setLastUpdateSSN((int)aCache.getAsObject("user_ssn"));
                    user1.setSsn((int)aCache.getAsObject("user_ssn"));
                    Log.d(TAG,"同步结束用户信息"+user1.getUsername()+user1.getLastUpdateSSN()+user1.getLastSyncTime());
                    //TODO 删除数据库中已经同步过的删除数据
                    int ssn = (int)user1.getLastUpdateSSN();
                    databaseHelper.deleteNoteExpireData(ssn);
                    databaseHelper.deleteTitleExpireData(ssn);
                    databaseHelper.deleteClockExipreData(ssn);
                    aCache.put("lastUpdateSSN",user1.getLastUpdateSSN());
                    aCache.put("lastSyncTime",user1.getLastSyncTime());
                    databaseHelper.updateUser(user1);
                    Intent intent1 = new Intent();
                    intent1.setAction(Constant.Service_END_ACTION);


                    //发送同步完成广播
                    sendBroadcast(intent1);

                    break;
                case 113:
                    //如果服务器最新数据 接收服务器传递过来的信息
                    Bundle bundle = msg.getData();
                    String type = bundle.getString("dataType");
                    if("note".equals(type)){
                        String json = bundle.getString("content");
                        if(json!=null){
                            aCache.put("redis_note",json);
                        }
                    }else if("title".equals(type)){
                        String json = bundle.getString("content");
                        if(json!=null){
                            aCache.put("redis_title",json);
                        }

                    }else if("clock".equals(type)){
                        String json = bundle.getString("content");
                        if(json!=null){
                            aCache.put("redis_clock",json);
                        }

                    }else if("imageName".equals(type)){//下载图片
                        String json = bundle.getString("content");
                        if(json!=null){
                            redis_image = gson.fromJson(json,new TypeToken<List<String>>() {}.getType());
                            //下载图片
                            if(redis_image.size()==0){
                                Constant.END = 1;
                                webSocket.send("sync_flag_"+Constant.START+"_"+Constant.END+"_"+Constant.MODEL);
                                return;
                            }

                            for (int i = 0; i < redis_image.size(); i++) {
                                String imagePath = path+aCache.getAsString("user_name")+"/"+redis_image.get(i);
                                int temp = i;
                                GlideApp.with(context).asBitmap().load(imagePath).into(new CustomTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                Log.d(TAG, "onResourceReady: 图片处理开始");
                                                redis_imageMap.put(redis_image.get(temp),resource);

                                                if(redis_imageMap.size()== redis_image.size()){
                                                    //下载了最后一张图片 发送下一步的标志
                                                    Log.d(TAG, "onResourceReady: 图片处理完成");
                                                    Constant.END = 1;
                                                    webSocket.send("sync_flag_"+Constant.START+"_"+Constant.END+"_"+Constant.MODEL);
                                                }
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                                Log.d(TAG, "onLoadCleared: 下载图片失败");
                                                return;
                                            }
                                        });
                            }
                        }
                    }/*else if("image".equals(type)){
                        String json = bundle.getString("content");
                        if(json!=null){
                            byte[] bytes = Base64.getDecoder().decode(json);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                            aCache.put(imagename,bitmap);
                            redis_image.add(imagename);
                            imagename = null;
                        }
                    }*/
                    break;
                case 114:
                    //没有修改 服务器结束同步
                    Intent intent2 = new Intent();
                    intent2.setAction(Constant.Service_END_ACTION);
                    Constant.START = 0;
                    Constant.END = 0;
                    Constant.MODEL = 0;
                    Constant.TRANSPROT_TYPE = SyncMoudle.CHARACTER_TRANSMISSION.code;
                    //发送同步完成广播
                    sendBroadcast(intent2);
                    break;
                case 10086:
                    syncDataStart();
                    break;
                default:
                    break;
            }
        }
    }
    private UpdateThingsHandler handler = new UpdateThingsHandler();
    public BackgroundUpdateService() {


    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
      return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: 服务创建"+this.hashCode());
        aCache = ACache.get(this);
        databaseHelper = MyDatabaseHelper.getInstance(this);

        super.onCreate();
    }
    public void setContext(Context context){
        this.context = context;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: 服务启动");
        new Thread(new Runnable() {
            @Override
            public void run() {
                initWebSocketClient();
            }
        }).start();


        return super.onStartCommand(intent, flags, startId);
    }
/*    private void initMockServer() {

        MockWebServer mMockWebServer = new MockWebServer();
        MockResponse response = new MockResponse()
                .withWebSocketUpgrade(new WebSocketListener() {
                    @Override
                    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                        super.onOpen(webSocket, response);
                        //有客户端连接时回调
                        Log.e(TAG, "服务器收到客户端连接成功回调：");
                        mWebSocket = webSocket;
                        mWebSocket.send("我是服务器，你好呀");
                    }

                    @Override
                    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                        super.onMessage(webSocket, text);

                        Log.e(TAG, "服务器收到消息：" + text);
                    }

                    @Override
                    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                        super.onClosed(webSocket, code, reason);
                        Log.e(TAG, "onClosed：");
                    }
                });

        mMockWebServer.enqueue(response);

        //获取连接url，初始化websocket客户端
        String websocketUrl = "ws://" + mMockWebServer.getHostName() + ":" + mMockWebServer.getPort() + "/";
        //WSManager.getInstance().init(websocketUrl);
        initWebSocketClient(websocketUrl);


    }*/

   public class BackgroundUpdateBinder extends Binder{
        public BackgroundUpdateService getService(){
            return BackgroundUpdateService.this;
        }
    }
    public boolean isOffConnection(){
       if(Constant.isSuccessful==0){

           return true;
       }else{
           return false;
       }
    }

    public void initWebSocketClient(){
        String wsURL = "ws://192.168.43.90:8081/ws/message";

        OkHttpClient client = new OkHttpClient.Builder().pingInterval(6, TimeUnit.SECONDS)
                 .build();
        Request request = new Request.Builder().url(wsURL).addHeader("Authorization",aCache.getAsString("token")).build();

         webSocket =  client.newWebSocket(request,new DefaultWebSocketListener(handler,this));
     /*    Intent intent = new Intent();由于本身是在子线程启动长连接，可能服务启动完成发送广播，长连接还未开始
         intent.setAction(Constant.Service_START_ACTION);
         sendBroadcast(intent);//发送服务启动完成的广播
        Log.d(TAG, "initWebSocketClient: 发送服务启动完成广播");*/

    }
    public void syncDataStart(){
       //初始化
        aCache.remove("redis_user");
        aCache.remove("redis_note");
        aCache.remove("redis_title");
        aCache.remove("redis_clock");
       redis_image.clear();
       redis_imageMap.clear();
        Constant.START = 0;
        Constant.END = 0;
        Constant.MODEL = 0;
        Constant.TRANSPROT_TYPE = SyncMoudle.CHARACTER_TRANSMISSION.code;
        Constant.isSuccessful = 0;
        Constant.START = 1;//开始同步的标志
        //从数据库中查出用户数据还是缓存中存呢 还是缓存中吧 毕竟同步次数肯定频繁
        Log.d(TAG, "syncDataStart: 发送"+"sync_flag_"+Constant.START+"_"+Constant.END+"_"+Constant.MODEL);
        webSocket.send("sync_flag_"+Constant.START+"_"+Constant.END+"_"+Constant.MODEL);
        User user = new User(aCache.getAsString("user_name"),(Integer) aCache.getAsObject("user_ssn"),
                (Integer) aCache.getAsObject("lastUpdateSSN"),aCache.getAsString("lastSyncTime"));
        Log.d(TAG, "syncDataStart: 发送 "+"sync_content_"+gson.toJson(user));
        webSocket.send("sync_content_"+gson.toJson(user));
    }
    public void syncDataStop(){
       /* 1000
      1000 indicates a normal closure, meaning that the purpose for
      which the connection was established has been fulfilled.
   1001
      1001 indicates that an endpoint is "going away", such as a server
      going down or a browser having navigated away from a page.
   1002
      1002 indicates that an endpoint is terminating the connection due
      to a protocol error.
   1003
      1003 indicates that an endpoint is terminating the connection
      because it has received a type of data it cannot accept (e.g., an
      endpoint that understands only text data MAY send this if it
      receives a binary message).
      1000 1000表示正常关闭，表示建立连接的目的已经达到。1001 1001 表示端点正在"离开"，例如服务器宕机或浏览器离开页面。
      1002 1002 表示端点由于协议错误而终止连接。1003 1003 表示端点正在终止连接，因为它收到了无法接受的数据类型
      （例如，仅理解文本数据的端点如果收到二进制消息，可以发送此数据）。
      。在发送关闭消息之前，任何已排队的消息都将被传输，但后续对 send 方法的调用将返回 false，并且它们的消息将不会被排队。
      cancel(): 立即释放 WebSocket 所持有的资源，丢弃所有已排队的消息。如果 WebSocket 已经关闭或取消，则此方法不执行任何操作。
      */
       webSocket.close(1000,null);
    }
    private <T> void InsyncProgressInitData(List<T> data, String msg){
        Log.d(TAG, "InsyncProgressInitData: 发送"+"sync_content_"+gson.toJson(data));
       webSocket.send("sync_content_"+msg+"_"+gson.toJson(data));

    }
    private void InsyncProgressInitImageData(Map<String,List<String>> data,String msg ){
        Log.d(TAG, "InsyncProgressInitImageData: 发送"+"sync_content_"+gson.toJson(data));
        webSocket.send("sync_content_"+msg+"_"+gson.toJson(data,new TypeToken<Map<String,List<String>>>() {}.getType()));
    }
    private void InsyncProgressInitImage(List<String> images){
        Map<String,String> filePaths = ImageUtils.getFilePathsByNames(getBaseContext(),images);//相对路径 文件名
        for (Map.Entry<String, String> stringStringEntry : filePaths.entrySet()) {
            String filePath = stringStringEntry.getKey();
            String fileName = stringStringEntry.getValue();

            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // 将 Bitmap 对象压缩为 PNG 格式的字节数组
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
            byte[] byteArray = outputStream.toByteArray();
            // 将字节数组转换为 ByteString
            ByteString byteString = ByteString.of(byteArray);
            webSocket.send(ByteString.of(fileName.getBytes()));//发送图片名字
            webSocket.send(byteString);
        }
    }

    public void send(String s){
        if(webSocket!=null){
            webSocket.send(s);
        }
    }
    public void send(ByteString bytes){
        if(webSocket!=null){
            webSocket.send(bytes);
        }
    }

}