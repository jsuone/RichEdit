package com.example.sample.Impl;


import android.os.Handler;

public interface SyncManageAction {
    void SyncStart();
    void SyncStop();
    Handler getHandler();
}
