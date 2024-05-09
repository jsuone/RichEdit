package com.example.android_note.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public  class Item {
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


    }