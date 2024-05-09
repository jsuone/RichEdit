package com.example.android_note.mapper;

import com.example.android_note.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @className: UserMapper
 * @description: TODO 类描述
 * @date: 2024/4/19:44
 **/
@Mapper
public interface UserMapper {
    Integer insertUser(User user);
    Integer updateUser(User user);
    User getUserByUsername(String username);
    List<User> queryUserList();
    void updatePassword(User user);
    void updateQuestion(User user);
}
