package com.example.sample.Impl;

import java.util.List;

// 观察者接口
public interface Observer<T> {
    void onDataChanged(List<T> newData);
}