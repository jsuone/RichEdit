package com.example.sample.communication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sample.model.User;
import com.example.sample.util.ACache;
import com.example.sample.util.ByteArrayMapConverter;
import com.example.sample.util.Constant;
import com.example.sample.util.SyncMoudle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class DefaultWebSocketListener extends WebSocketListener {
    private static final String TAG = "notedata";
    private Gson gson = new Gson();
    private Gson mapGson = ByteArrayMapConverter.createGson();
    private ACache aCache;
    private Context context;
    private Handler handler;
    private List<String> imageCache = new ArrayList<>();
    private String imageName = null;
    public DefaultWebSocketListener(Handler handler,Context context) {
        super();
        this.handler = handler;
        this.aCache = ACache.get(context);
        this.context = context;
    }

    @Override
    public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        super.onClosed(webSocket, code, reason);
        Constant.isSuccessful = 0;
        Log.d(TAG, "onClosed: 已经关闭长连接");
        Intent intent = new Intent();
        intent.setAction(Constant.Service_FAILED_END_ACTION);
        context.sendBroadcast(intent);
    }

    @Override
    public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        super.onClosing(webSocket, code, reason);
    }

    @Override
    public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        t.printStackTrace();
        //webSocket.cancel();
        Intent intent = new Intent();
        intent.setAction(Constant.Service_FAILED_END_ACTION);
        if((Constant.START==1&&Constant.END==0)||(Constant.START==1&&Constant.END==1&&Constant.MODEL==1)){//如果同步未进入结束阶段或者模式一客户端发起结束
            aCache.remove("redis_user");
            aCache.remove("redis_note");
            aCache.remove("redis_title");
            aCache.remove("redis_clock");

        }

        Constant.START = 0;
        Constant.END = 0;
        Constant.MODEL = 0;
        Constant.TRANSPROT_TYPE = SyncMoudle.CHARACTER_TRANSMISSION.code;
        Constant.isSuccessful = 0;


        //发送同步完成广播
        context.sendBroadcast(intent);
        Log.d(TAG, "onFailure: 长连接失败"+t);
    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
        super.onMessage(webSocket, text);
        Log.d(TAG,"message "+text);
        if(text.equals("500")){
            //被挤下线

            Intent intent = new Intent();
            intent.setAction(Constant.Login_Grab_ACTION);
            context.sendBroadcast(intent);
            return;
        }
        if(text.startsWith("response_content_")){
            String content = text.substring("response_content_".length());
            if(content!=null||!content.equals("")){
                parseSyncContent(content);
            }

        }
        if(text.startsWith("response_flag_")){
            String[] messageArr = text.split("_");
            updateSyncFlag(messageArr[2],messageArr[3],messageArr[4]);
        }


    }

    @Override
    public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
        super.onMessage(webSocket, bytes);
        Log.d(TAG, "onMessage: 接受到字节消息");
        if(Constant.START==1&&Constant.END!=1){
            if(imageName==null){
                imageName = bytes.utf8();
            }else{
                imageCache.add(imageName);
                aCache.put(imageName,bytes);
                imageName = null;
                //一组图片数据存储完毕
            }

            //String workingDirectory = System.getProperty("user.dir")+"\\src\\main\\resources\\static";
        }

    }

    @Override
    public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
        Log.d(TAG, "onOpen: 开始长连接");
        super.onOpen(webSocket, response);
        Message message = handler.obtainMessage();
        message.what = Constant.Service_START_ACTION;
        handler.sendMessage(message);
        Constant.isSuccessful = 1;
        Log.d(TAG, "DefaultWebSocketListener: 发送长连接完成消息");

    }
    private void parseSyncContent(String content){
        if(Constant.START==1&&Constant.END==0){
            //同步进行中
            if(Constant.MODEL==0){
                //正在判断模式阶段
                    String[] strings = content.split("_");
                    //Integer model = Integer.valueOf(strings[0]);

                   /* if(model==2){
                        //这表示服务器数据最新 将传输过来的用户信息保存到缓存中

                    }*/
                    Message message = handler.obtainMessage();
                    message.what = SyncMoudle.STRAT_SYNC_PROGRESS.code;
                    String json = content.substring("1_".length());//服务器返回的用户数据 包含同步的时间
                    Bundle bundle = new Bundle();
                    bundle.putString("user",json);
                    message.setData(bundle);
                    handler.sendMessage(message);
                    return;
            }else if(Constant.MODEL==1){
                Log.d(TAG, "parseSyncContent: 客户端上传数据中 服务端的回应"+content);
                //List<String> fileNames = gson.fromJson(content, new TypeToken<List<String>>() {}.getType());
                Message message = handler.obtainMessage();
                message.what = SyncMoudle.IN_SYNC_PROGRESS.code;
                Bundle bundle = new Bundle();
                bundle.putString("images",content);
                message.setData(bundle);
                handler.sendMessage(message);
                return;
            }else if(Constant.MODEL==2){
                //服务端数据最新
                Log.d(TAG, "parseSyncContent: 服务端上传数据中 "+content);
                Message message = handler.obtainMessage();
                message.what = SyncMoudle.IN_SYNC_ANDROID_UPDATE_PROGRESS.code;
                String[] ss = content.split("_");
                Bundle bundle = new Bundle();
                String json = content.substring(ss[0].length()+1);//因为后续还有一个”_“
                bundle.putString("dataType",ss[0]);
                bundle.putString("content",json);
                message.setData(bundle);
                handler.sendMessage(message);

            }
        }else if(Constant.END==1){
            if(Constant.MODEL==1) {
                //到了同步结束阶段，客户端发起结束，服务端的回应 客户端开始插入数据到数据库
                Message message = handler.obtainMessage();
                message.what = SyncMoudle.END_SYNC_PROGRESS_STRAT_ANDROID.code;
                handler.sendMessage(message);
            }else if(Constant.MODEL==2){
                //服务器最新
                Message message = handler.obtainMessage();
                message.what = SyncMoudle.END_SYNC_PROGRESS_STRAT_WEB.code;
                handler.sendMessage(message);
            }
        }
    }
    private void updateSyncFlag(String start,String end,String model){
        if(Constant.END!=Integer.valueOf(end)){
          //由服务端开启的 结束阶段 说明是服务端数据最新 到了同步结束阶段 或者没有修改 服务器结束同步
            if(Integer.valueOf(model)==3){
                Message message = handler.obtainMessage();
                message.what = SyncMoudle.END_SYNC_PROGRESS_THE_SAME.code;
                handler.sendMessage(message);
                return;
            }
            Constant.END = Integer.valueOf(end);

            //Bundle bundle = new Bundle();
            //bundle.putString("redis_image", gson.toJson(imageCache));
            //message.setData(bundle);

        }
        else {
            if(Constant.MODEL!=Integer.valueOf(model)){
                //模式发生变化 发送下一模式的数据
                Constant.MODEL = Integer.valueOf(model);
                Message message = handler.obtainMessage();
                message.what = SyncMoudle.IN_SYNC_PROGRESS.code;
                handler.sendMessage(message);
            }
        }

    }

}
