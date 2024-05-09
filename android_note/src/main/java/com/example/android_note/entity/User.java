package com.example.android_note.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @className: User
 * @description: TODO 类描述
 * @date: 2024/4/19:08
 **/
@Data
public class User implements Serializable {
    private Integer id;
    private String username;
    private String password;
    private Integer ssn;
    private Integer lastUpdateSSN;
    private String lastSyncTime;
    private String security_question;
    private String security_answer;
    private String password_change_time;
}
