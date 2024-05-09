package com.example.android_note.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NoteModel {//点击标题对应笔记显示 采用map集合存储 一个标题guid对应list笔记
    private String guid;
    private String theme;
    private String note_context;
    private String title_guid;
    private Boolean isDel;//0是保留 1是删除标记
    private Integer ssn;//同步序列号
    private String createTime;
    private String updateTime;
    private String username;

}
