package com.example.android_note.service;

import com.example.android_note.entity.TitleModel;

import java.util.List;

/**
 * @className: TitleService
 * @description: TODO 类描述
 * @date: 2024/4/316:39
 **/
public interface TitleService {
    void insertTitle(TitleModel titleModel);
    int deleteTitleByGUID(String guid);
    int updateTitleByGUID(TitleModel titleModel);
    List<TitleModel> selectTitleListByUserName(String userName);
}
