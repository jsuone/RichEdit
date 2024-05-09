package com.example.sample.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @className: DateTime
 * @description: TODO 类描述
 * @date: 2024/4/120:40
 **/
public class DateTime {


    public static String getCurrentTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return sdf.format(date);
    }
}
