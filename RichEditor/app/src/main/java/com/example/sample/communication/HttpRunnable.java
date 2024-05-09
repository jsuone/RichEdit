package com.example.sample.communication;

import com.example.sample.model.HttpResult;
import com.google.gson.Gson;

import java.lang.reflect.Type;

public class HttpRunnable implements Runnable{
    String mUrl;
    HttpResult httpResult;
    String json;
    Gson gson;
    Type type;
    public <T>void setInfo(String mUrl,HttpResult<T> httpResult,String json,Type type,Gson gson){
        this.mUrl = mUrl;
        this.httpResult = httpResult;
        this.json = json;
        this.type = type;
        this.gson = gson;
    }
    @Override
    public void run() {

    }
}
