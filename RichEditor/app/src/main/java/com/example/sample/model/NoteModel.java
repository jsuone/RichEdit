package com.example.sample.model;

import java.io.Serializable;
import java.util.List;

public class NoteModel implements Serializable {//点击标题对应笔记显示 采用map集合存储 一个标题guid对应list笔记
    private String guid;
    private String theme;
    private String note_context;
    private String title_guid;
    private Boolean isDel;//0是保留 1是删除标记
    private Integer ssn;//同步序列号
    private String createTime;
    private String updateTime;
    private String username;

    public NoteModel(String guid, String theme, String title_guid, Boolean isDel, Integer ssn, String createTime, String updateTime, String username) {
        this.guid = guid;
        this.theme = theme;
        this.title_guid = title_guid;
        this.isDel = isDel;
        this.ssn = ssn;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.username = username;
    }

    public NoteModel(String guid, String theme, String note_context, String title_guid, Boolean isDel, Integer ssn, String createTime, String updateTime) {
        this.guid = guid;
        this.theme = theme;
        this.note_context = note_context;
        this.title_guid = title_guid;
        this.isDel = isDel;
        this.ssn = ssn;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public NoteModel(String guid, String theme, String note_context, String title_guid, Boolean isDel, Integer ssn, String createTime, String updateTime, String username) {
        this.guid = guid;
        this.theme = theme;
        this.note_context = note_context;
        this.title_guid = title_guid;
        this.isDel = isDel;
        this.ssn = ssn;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.username = username;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getNote_context() {
        return note_context;
    }

    public void setNote_context(String note_context) {
        this.note_context = note_context;
    }

    public String getTitle_guid() {
        return title_guid;
    }

    public void setTitle_guid(String title_guid) {
        this.title_guid = title_guid;
    }

    public Boolean getDel() {
        return isDel;
    }

    public void setDel(Boolean del) {
        isDel = del;
    }

    public Integer getSsn() {
        return ssn;
    }

    public void setSsn(Integer ssn) {
        this.ssn = ssn;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
