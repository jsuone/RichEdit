package com.example.sample.model;

import java.util.List;

public class TitleModel {

    private String guid;
    private String title;
    private String parentID;
    private List<TitleModel> child;
    private Integer level;//标题等级，方便查出来的数据排序进行实体化
    private Boolean isOpen;//是否展开
    private Boolean isDel;//0是保留 1是删除标记
    private Integer ssn;//同步序列号
    private String createTime;
    private String updateTime;
    private String username;

    public TitleModel(String guid, String title, String parentID, Integer level, Boolean isOpen, Boolean isDel, Integer ssn, String createTime,String updateTime) {
        this.guid = guid;
        this.title = title;
        this.parentID = parentID;
        this.level = level;
        this.isOpen = isOpen;
        this.isDel = isDel;
        this.ssn = ssn;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public TitleModel(String guid, String title, String parentID, Integer level, Boolean isOpen, Boolean isDel, Integer ssn, String createTime, String updateTime, String username) {
        this.guid = guid;
        this.title = title;
        this.parentID = parentID;
        this.level = level;
        this.isOpen = isOpen;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public List<TitleModel> getChild() {
        return child;
    }

    public void setChild(List<TitleModel> child) {
        this.child = child;
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

    public Boolean isOpen() {
        return isOpen;
    }

    public void setOpen(Boolean open) {
        isOpen = open;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }
}
