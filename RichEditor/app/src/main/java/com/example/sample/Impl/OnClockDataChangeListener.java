package com.example.sample.Impl;

public interface OnClockDataChangeListener {
    void notificaitonDataChangeListener(String uuid,Boolean change,Boolean extraTime);
    void itemDataChangeListener(String uuid,Boolean change);
    void deleteItemDataLinstener(String uuid);
}
