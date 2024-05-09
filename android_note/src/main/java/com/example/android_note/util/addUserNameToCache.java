package com.example.android_note.util;

import com.example.android_note.entity.User;
import com.example.android_note.service.UserService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @className: addUserNameToCache
 * @description: TODO 类描述
 * @date: 2024/4/115:16
 **/
public class addUserNameToCache implements InitializingBean {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private UserService userService;
    //查询前50条用户数据作为备用缓存
    @Override
    public void afterPropertiesSet() throws Exception {
        List<User> userList = userService.queryUserNameList();
        userList.forEach(user -> {
            redisUtil.set(user.getUsername(),user);
        });
    }
}
