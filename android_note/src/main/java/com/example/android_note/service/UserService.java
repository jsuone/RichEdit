package com.example.android_note.service;

import com.example.android_note.entity.User;

import java.util.List;

/**
 * @className: UserService
 * @description: TODO 类描述
 * @date: 2024/4/19:44
 **/
public interface UserService {
    User queryUserByName(String username);
    boolean registerUser(User user);
    List<User> queryUserNameList();

    boolean isUserHasInCache(String username);
    void updateUserInfo(User user);
    void updateUserPassword(User user);
    void updateQuestion(User user);
}
