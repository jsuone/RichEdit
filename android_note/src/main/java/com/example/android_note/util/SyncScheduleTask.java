package com.example.android_note.util;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @className: SyncScheduleTask
 * @description: TODO 类描述
 * @date: 2024/4/915:08
 **/
@Component
public class SyncScheduleTask {
public static ConcurrentHashMap<String,String> connectionStatus = new ConcurrentHashMap<>();
public static ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(10);
public static ConcurrentHashMap <String, ScheduledFuture> taskMap = new ConcurrentHashMap<>();
}
