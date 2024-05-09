package com.example.alarmclock;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String name;
    private ArrayList<Item> items;
 
    public Group(String name, ArrayList<Item> items) {
        this.name = name;
        this.items = items;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    public String getName() {
        return name;
    }
 
    public ArrayList<Item> getItems() {
        return items;
    }

    public static class Item {
        private String UUID;
        private String backlog_name;
        private String backlog_time;
        private Boolean isChecked;
        private Boolean isRepeat;
        private Integer ssn;
        private Boolean isDel;
        private String createTime;
        private String updateTime;
        private String userGuid;

        public Item(String UUID, String backlog_name, String backlog_time, Boolean isChecked, Boolean isRepeat, Boolean isDel, String createTime, String updateTime) {
            this.UUID = UUID;
            this.backlog_name = backlog_name;
            this.backlog_time = backlog_time;
            this.isChecked = isChecked;
            this.isRepeat = isRepeat;
            this.isDel = isDel;
            this.createTime = createTime;
            this.updateTime = updateTime;
        }

        public Item(String UUID, String backlog_name, String backlog_time, Boolean isChecked, Boolean isRepeat, Integer ssn, Boolean isDel, String createTime, String updateTime, String userGuid) {
            this.UUID = UUID;
            this.backlog_name = backlog_name;
            this.backlog_time = backlog_time;
            this.isChecked = isChecked;
            this.isRepeat = isRepeat;
            this.ssn = ssn;
            this.isDel = isDel;
            this.createTime = createTime;
            this.updateTime = updateTime;
            this.userGuid = userGuid;
        }
        // 拷贝构造函数
        public Item(Item other) {
            this.UUID = other.UUID;
            this.backlog_name = other.backlog_name;
            this.backlog_time = other.backlog_time;
            this.isChecked = other.isChecked;
            this.isRepeat = other.isRepeat;
            this.ssn = other.ssn;
            this.isDel = other.isDel;
            this.createTime = other.createTime;
            this.updateTime = other.updateTime;
            this.userGuid = other.userGuid;
        }

        public void setBacklog_name(String backlog_name) {
            this.backlog_name = backlog_name;
        }

        public void setBacklog_time(String backlog_time) {
            this.backlog_time = backlog_time;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }

        public String getBacklog_name() {
            return backlog_name;
        }

        public String getBacklog_time() {
            return backlog_time;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public String getUUID() {
            return UUID;
        }

        public void setUUID(String UUID) {
            this.UUID = UUID;
        }

        public Boolean getRepeat() {
            return isRepeat;
        }

        public void setRepeat(Boolean repeat) {
            isRepeat = repeat;
        }

        public Integer getSsn() {
            return ssn;
        }

        public void setSsn(Integer ssn) {
            this.ssn = ssn;
        }

        public Boolean getDel() {
            return isDel;
        }

        public void setDel(Boolean del) {
            isDel = del;
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

        public String getUserGuid() {
            return userGuid;
        }

        public void setUserGuid(String userGuid) {
            this.userGuid = userGuid;
        }
    }


}