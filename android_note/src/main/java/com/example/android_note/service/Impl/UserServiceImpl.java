package com.example.android_note.service.Impl;

import com.example.android_note.entity.User;
import com.example.android_note.mapper.UserMapper;
import com.example.android_note.service.UserService;
import com.example.android_note.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @className: UserServiceImpl
 * @description: TODO 类描述
 * @date: 2024/4/110:37
 **/
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    RedisUtil redisUtil;
    @Override
    public User queryUserByName(String username) {
        User user =  userMapper.getUserByUsername(username);
        return user;
    }

    @Override
    public boolean registerUser(User user) {
        int i = 0;
        if(userMapper.getUserByUsername(user.getUsername())==null){
            i = userMapper.insertUser(user);
        }
        return i==0?false:true;

    }

    @Override
    public List<User> queryUserNameList() {
        return userMapper.queryUserList();
    }

    @Override
    public boolean isUserHasInCache(String username) {
        //用户在缓存中是否存在 提高查询用户的速率
        if(redisUtil.get(username)!=null){
            return true;
        }else return false;

    }

    @Override
    public void updateUserInfo(User user) {
        userMapper.updateUser(user);
    }

    @Override
    public void updateUserPassword(User user) {
        userMapper.updatePassword(user);
    }

    @Override
    public void updateQuestion(User user) {
        userMapper.updateQuestion(user);
    }
}
