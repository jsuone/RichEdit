package com.example.sample.model;

public class HttpResult<T> {
    public Integer code;
    public String msg;
    public T data;

    @Override
    public String toString() {
        return "HttpResult{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
