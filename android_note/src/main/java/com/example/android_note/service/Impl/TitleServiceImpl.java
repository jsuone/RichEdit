package com.example.android_note.service.Impl;

import com.example.android_note.entity.TitleModel;
import com.example.android_note.mapper.TitleMapper;
import com.example.android_note.service.TitleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @className: TitleServiceImple
 * @description: TODO 类描述
 * @date: 2024/4/316:40
 **/
@Service
public class TitleServiceImpl implements TitleService {

    @Autowired
    TitleMapper titleMapper;
    @Override
    public void insertTitle(TitleModel titleModel) {
        titleMapper.insertTitle(titleModel);

    }

    @Override
    public int deleteTitleByGUID(String guid) {
        return titleMapper.deleteTitleByGUID(guid);
    }
    @Override
    public int updateTitleByGUID(TitleModel titleModel) {
        return titleMapper.updateTitle(titleModel);
    }

    @Override
    public List<TitleModel> selectTitleListByUserName(String userName) {
        return titleMapper.selectTitleListByUserName(userName);
    }

}
