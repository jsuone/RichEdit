package com.example.sample.util;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ActivityCollector {
    static String TAG = "notedata";
    public static List<Activity> activities = new ArrayList<>();
    public static void addActivity(Activity activity){
        activities.add(activity);
    }
    public static void removeActivity(Activity activity){
        activities.remove(activity);
    }
    public static void destroyActivity(Activity activity){
        activities.remove(activity);
        activity.finish();
    }
    public static void finishAll(){
        for (Activity activity : activities) {
            if(!activity.isFinishing()){
                Log.d(TAG, "finishAll: "+activity.getClass().getSimpleName());
                activity.finish();
            }
        }
        activities.clear();
    }
}
