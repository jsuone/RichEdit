package com.example.sample.model;


import java.io.Serializable;

/**
 * @className: User
 * @description: TODO 类描述
 * @date: 2024/4/19:08
 **/

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
    public User(){}
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(String username, Integer ssn, Integer lastUpdateSSN, String lastSyncTime) {
        this.username = username;
        this.ssn = ssn;
        this.lastUpdateSSN = lastUpdateSSN;
        this.lastSyncTime = lastSyncTime;
    }

    public User(Integer id, String username, String password, Integer ssn, Integer lastUpdateSSN, String lastSyncTime) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.ssn = ssn;
        this.lastUpdateSSN = lastUpdateSSN;
        this.lastSyncTime = lastSyncTime;
    }

    public User(Integer id, String username, Integer ssn, Integer lastUpdateSSN, String lastSyncTime) {
        this.id = id;
        this.username = username;
        this.ssn = ssn;
        this.lastUpdateSSN = lastUpdateSSN;
        this.lastSyncTime = lastSyncTime;
    }

    public User(String username, String password, Integer ssn, Integer lastUpdateSSN, String lastSyncTime,String password_change_time) {
        this.username = username;
        this.password = password;
        this.ssn = ssn;
        this.lastUpdateSSN = lastUpdateSSN;
        this.lastSyncTime = lastSyncTime;
        this.password_change_time = password_change_time;
    }
    public User(String username, String password, Integer ssn, Integer lastUpdateSSN, String lastSyncTime) {
        this.username = username;
        this.password = password;
        this.ssn = ssn;
        this.lastUpdateSSN = lastUpdateSSN;
        this.lastSyncTime = lastSyncTime;

    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getSsn() {
        return ssn;
    }

    public void setSsn(Integer ssn) {
        this.ssn = ssn;
    }

    public Integer getLastUpdateSSN() {
        return lastUpdateSSN;
    }

    public void setLastUpdateSSN(Integer lastUpdateSSN) {
        this.lastUpdateSSN = lastUpdateSSN;
    }

    public String getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(String lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public String getSecurity_question() {
        return security_question;
    }

    public void setSecurity_question(String security_question) {
        this.security_question = security_question;
    }

    public String getSecurity_answer() {
        return security_answer;
    }

    public void setSecurity_answer(String security_answer) {
        this.security_answer = security_answer;
    }

    public String getPassword_change_time() {
        return password_change_time;
    }

    public void setPassword_change_time(String password_change_time) {
        this.password_change_time = password_change_time;
    }

}
