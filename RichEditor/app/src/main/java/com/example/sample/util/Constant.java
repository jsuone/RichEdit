package com.example.sample.util;

public class  Constant {
public static final String Title_KEY  = "title_key";
public static final String Title_List_KEY = "title_list_key";
public static final String Note_List_KEY = "note_list_key";//笔记显示列表里的数据
public static final String Note_ALL_KEY = "note_all_key";//笔记显示列表所属的父布局里的所有笔记数据
public static final String Clock_ALL_KEY = "clock_all_key";
public static final int NOTE_ADD_FINISH = 999;
public static final int NOTE_COMPILE_FINISH = 888;
public static final String clock_start_action = "com.example.package.clock";
public static final String clock_have_notification = "com.example.package.clock.have.notification";
public static volatile int START = 0;//1 启用 0关闭 同步的标志位
public static volatile int END = 0;//1 启用 0关闭
public static volatile int MODEL = 0;///1  客户端将已经修改的数据传输到服务端2服务端传数据到客户端 3 没有修改
public static volatile int isSuccessful = 0;//0是同步没成功长连接断开 1是长连接正常
public static final int Service_START_ACTION = 10086;//当前意图的动作
public static final String Service_END_ACTION = "com.example.service.end";//当前意图的动作
public static final String Service_FAILED_END_ACTION = "com.example.service.failed.end";//同步失败
public static final String Login_Grab_ACTION = "com.exanple.login.grab";//被挤下线
//同步结束在设计中有本地最新情况就不需要同步完从数据库里查询再更新 但服务器最新则需要
public static volatile int TRANSPROT_TYPE = SyncMoudle.CHARACTER_TRANSMISSION.code;
public static final String SP_KEY = "lock";//sp保存数据地方的名称
public static final String SP_LOCK_KEY = "lockContent";//手势密码的键
}
