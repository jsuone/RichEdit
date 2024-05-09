package com.example.android_note.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Data
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


}
