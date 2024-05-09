package com.example.sample.communication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.example.sample.model.HttpResult;
import com.example.sample.model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

//http发起登录和注册请求
public class SendHttp {
    static Gson gson = new Gson();
    private static  final String murl = "http://192.168.43.90:8081/";
    private static final String contentType = "Content-Type";
    private static final String content = "";
    public static final String TAG = "notedata";
    //注册
    public void registerUser(User user,Handler handler){
        String url = murl+"user/register";
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"),gson.toJson(user));
        Request request = new Request.Builder().url(url).post(body).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("SendHttp","连接失败");
                Message message = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putInt("registerUser",2);
                message.setData(bundle);
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ResponseBody body = response.body();
                HttpResult<String> result = gson.fromJson(body.string(),new TypeToken<HttpResult<String>>(){}.getType());
                if(result.code==404){
                    //注册失败
                    Message message = handler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putInt("registerUser",0);
                    bundle.putString("msg", result.data);
                    message.setData(bundle);
                    handler.sendMessage(message);
                }else if(result.code == 200){
                    Message message = handler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putInt("registerUser",1);
                    bundle.putString("token",result.data);
                    bundle.putString("userData", gson.toJson(user));
                    message.setData(bundle);
                    handler.sendMessage(message);
                }
            }
        });
    }
    //用户名是否重复 防止需要整个表单提交才知道
    public void isRepeatUserName(String userName, Handler handler){
        String url = murl+"user/isRepeat?username="+userName;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("SendHttp","连接失败");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ResponseBody body = response.body();
                HttpResult<String> result = gson.fromJson(body.string(),new TypeToken<HttpResult<String>>(){}.getType());
                if(result.code==404){
                    //用户名已经存在
                    Message message = handler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putInt("isRepeatUserName",1);
                    message.setData(bundle);
                    handler.sendMessage(message);
                }else if(result.code == 200){
                    //用户米不存在
                    Message message = handler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putInt("isRepeatUserName",0);
                    message.setData(bundle);
                    handler.sendMessage(message);
                }

            }
        });

    }

    //登录
    public void loginByUser(String userName,String passWord,Handler handler){
        String url = murl+"user/login";
        OkHttpClient client = new OkHttpClient();
        User user = new User(userName,passWord);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"),gson.toJson(user));
        Request request = new Request.Builder().url(url).post(body).build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("SendHttp","连接失败");
                offLineLoginHandler(5,handler);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
               loginHandlerThing(response,handler, user);
            }
        });
    }
    public void secretFreeLogin(String userName,String token,Handler handler){
        String url = murl+"user/SecretFreeLogin?userName="+userName;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().addHeader("Authorization",token).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("SendHttp","连接失败");
                offLineLoginHandler(6,handler);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                loginHandlerThing(response,handler,null);
            }
        });
        //免密需要不在登录界面停顿进入 所以要卡在主界面获得信息判断是否进入主界面
        //安卓中不允许在主线程进行网络传输等操作
    }
    public void loginOut(String username,String token){
        String url = murl+"user/login_out?username="+username;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().addHeader("Authorization",token).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d(TAG,"退出登录");
            }
        });
    }
    public void checkSecurityQuestion(String username,String token,Handler handler){
        String url = murl+"user/check_question?username="+username;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).get().addHeader("Authorization",token).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                //网络异常
                Message message = handler.obtainMessage();
                message.what = 13;
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d(TAG,"请求密保问题");
                ResponseBody body = response.body();
                HttpResult<String> result = gson.fromJson(body.string(),new  TypeToken<HttpResult<String>>(){}.getType());
                Message message = handler.obtainMessage();
                if(result.code==200){
                    //有设置密保问题
                    message.what = 11;
                    Bundle bundle = new Bundle();
                    bundle.putString("question",result.data);
                    message.setData(bundle);
                    handler.sendMessage(message);
                }else if(result.code == 404){
                    //没有设置
                    message.what = 12;
                    handler.sendMessage(message);
                }
            }
        });
    }
    public void checkSecurityAnswer(String username,String token,String answer,Handler handler){
        User user = new User();
        user.setUsername(username);
        user.setSecurity_answer(answer);
        String url = murl+"user/check_answer";
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"),gson.toJson(user));
        Request request = new Request.Builder().url(url).post(body).addHeader("Authorization",token).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                //网络异常
                Message message = handler.obtainMessage();
                message.what = 13;
                handler.sendMessage(message);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ResponseBody body = response.body();
                HttpResult<String> result = gson.fromJson(body.string(),new  TypeToken<HttpResult<String>>(){}.getType());
                Message message = handler.obtainMessage();
                if(result.code==200){
                    //密保答案正确
                    message.what = 21;
                    handler.sendMessage(message);
                }else if(result.code == 404){
                    //密保答案错误
                    message.what = 22;
                    handler.sendMessage(message);
                }
            }
        });
    }
    public void checkPassword(String username,String token,String password,Handler handler){
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        String url = murl+"user/check_password";
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"),gson.toJson(user));
        Request request = new Request.Builder().url(url).post(body).addHeader("Authorization",token).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                //网络异常
                Message message = handler.obtainMessage();
                message.what = 13;
                handler.sendMessage(message);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ResponseBody body = response.body();
                HttpResult<String> result = gson.fromJson(body.string(),new  TypeToken<HttpResult<String>>(){}.getType());
                Message message = handler.obtainMessage();
                if(result.code==200){
                    //密码正确
                    message.what = 31;
                    handler.sendMessage(message);
                }else if(result.code == 404){
                    //密码错误
                    message.what = 32;
                    handler.sendMessage(message);
                }
            }
        });
    }
    public void changePassword(String username,String token,String newPassword,Handler handler){
        User user = new User();
        user.setUsername(username);
        user.setPassword(newPassword);
        String url = murl+"user/change_password";
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"),gson.toJson(user));
        Request request = new Request.Builder().url(url).post(body).addHeader("Authorization",token).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ResponseBody body = response.body();
                HttpResult<String> result = gson.fromJson(body.string(),new  TypeToken<HttpResult<String>>(){}.getType());
                Message message = handler.obtainMessage();
                if(result.code==200){
                    //修改完毕
                    message.what = 41;
                    handler.sendMessage(message);
                }
            }
        });
    }
    public void setNewSecurityQuestion(String username,String token,String question,String answer,Handler handler){
        User user = new User();
        user.setUsername(username);
        user.setSecurity_question(question);
        user.setSecurity_answer(answer);
        String url = murl+"user/set_question";
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"),gson.toJson(user));
        Request request = new Request.Builder().url(url).post(body).addHeader("Authorization",token).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ResponseBody body = response.body();
                HttpResult<String> result = gson.fromJson(body.string(),new  TypeToken<HttpResult<String>>(){}.getType());
                Message message = handler.obtainMessage();
                if(result.code==200){
                    //修改完毕
                    message.what = 41;
                    handler.sendMessage(message);
                }
            }
        });
    }
    private void offLineLoginHandler(int modelFlag,Handler handler){
        //5是登录发生网络问题 6是免密登录发生网络问题
        Message message = handler.obtainMessage();
        message.what = modelFlag;
        handler.sendMessage(message);

    }
    private void loginHandlerThing(Response response,Handler handler,User user){
        ResponseBody body = response.body();
        HttpResult<String> result = null;
        try {
            result = gson.fromJson(body.string(),new TypeToken<HttpResult<String>>(){}.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "loginHandlerThing: 免密接受的消息"+result.toString());
        if(result.code==200){
            //成功
            Message message = handler.obtainMessage();
            String token = result.data;
            Bundle bundle = new Bundle();
            bundle.putString("token",token);

            if(user!=null){
                //密码登录
                bundle.putString("user",gson.toJson(user));
                message.what = 1;
            }else{
                //免密登录
                message.what = 2;
            }
            message.setData(bundle);
            handler.sendMessage(message);
        }else if(result.code==404){
            //失败
            Message message = handler.obtainMessage();
            String msg = result.msg;
            Bundle bundle = new Bundle();
            bundle.putString("msg",msg);
            message.setData(bundle);
            message.what = 3;
            handler.sendMessage(message);
        }else if(result.code==500){
            //是否抢占登录
            Message message = handler.obtainMessage();
            message.what = 4;
            String token = result.data;
            Bundle bundle = new Bundle();
            bundle.putString("token",token);
            if(user!=null){
                bundle.putString("user",gson.toJson(user));
            }
            message.setData(bundle);
            handler.sendMessage(message);
        }
    }
}
