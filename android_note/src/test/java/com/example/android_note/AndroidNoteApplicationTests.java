package com.example.android_note;

import com.example.android_note.entity.User;
import com.example.android_note.mapper.UserMapper;
import com.example.android_note.service.UserService;
import com.example.android_note.util.JWTUtil;
import com.example.android_note.util.RedisUtil;
import com.google.gson.Gson;
import icu.xuyijie.base64utils.Base64Utils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.text.ParseException;
import java.util.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AndroidNoteApplicationTests {
    @Autowired
    UserMapper userMapper;
    @Autowired
    RedisUtil redisUtil;

    @Test
    void contextLoads() {
        User user = userMapper.getUserByUsername("admin");
    }
    @Test
    void userTest() {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("123456");
        user.setSsn(0);
        user.setLastUpdateSSN(0);
        user.setLastSyncTime("2021-04-01 00:00:00");
        userMapper.insertUser(user);
    }
    @Test
    void RedisString(){
        boolean flag1 = redisUtil.set("name","li");
        redisUtil.set("name1","liu");
        String value = (String) redisUtil.get("name");
        List<String> keys = new ArrayList<>();
        //keys.add("name");
       // keys.add("name1");
       // boolean flag =  redisUtil.delete(keys);
        System.out.println(redisUtil.get("name")+""+redisUtil.get("name1"));
    }
    @Test
    void RedisTestUser(){
        User user = new User();
        user.setUsername("li");
        user.setPassword("123456");
        user.setSsn(0);
        user.setLastUpdateSSN(0);
        redisUtil.set("li",user);
        User user1 = (User) redisUtil.get("li");
        System.out.println(user1.toString());
/*        user1.setLastUpdateSSN(11);
        User user2 = (User) redisUtil.get(user.getUsername());
        System.out.println(user2.toString());*/

    }
    @Autowired
    UserService userService;
    @Test
    void testuser() throws ParseException {
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzM4NCJ9.eyJ1c2VyTmFtZSI6InF3ZSIsImV4cCI6MTcxMzkzMDExMH0.-S1XNzPrz8IB2pm7csEAA41kNeImKenvXIAjzfbwowJBAwAaiC8HQIQYTxQiPdA3";
        String time = "2024-04-17 11:45:27";
        System.out.println(JWTUtil.isPasswordChangedBeforeToken(token,time));

    }
@Test
    void test(){
    // 创建一个 Map 用于存储笔记id和对应的图片id集合
    Map<String, List<String>> imageMap = new HashMap<>();

    // 假设有一些图片id
    List<String> imageIds = new ArrayList<>();
    imageIds.add("user_1_uuid1");
    imageIds.add("user_1_uuid2");
    imageIds.add("user_2_uuid1");

    // 遍历图片id集合，将其添加到对应的笔记id的集合中
    for (String imageId : imageIds) {
        String[] parts = imageId.split("_");
        if (parts.length >= 2) {
            String noteId = parts[1]; // 笔记id
            // 如果笔记id已经存在于 map 中，则将图片id添加到对应的集合中
            if (imageMap.containsKey(noteId)) {
                imageMap.get(noteId).add(imageId);
            } else {
                // 否则，创建一个新的集合并将图片id添加进去
                List<String> newList = new ArrayList<>();
                newList.add(imageId);
                imageMap.put(noteId, newList);
            }
        }
    }

    // 打印结果
    for (Map.Entry<String, List<String>> entry : imageMap.entrySet()) {
        String noteId = entry.getKey();
        List<String> imageList = entry.getValue();
        System.out.println("笔记id: " + noteId);
        System.out.println("对应的图片id集合: " + imageList);
    }
}
@Test
    void testlist(){
    String imagePath = "src/main/resources/static/images/"+"test2";
    File directory = new File(imagePath);
    File[] files = directory.listFiles();
    for (File file : files) {
        String base64Image = Base64Utils.transferToBase64(file,false);
        System.out.println(base64Image);
    }


}
}
