package com.example.android_note.mapper;

import com.example.android_note.entity.TitleModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @className: TitleMapper
 * @description: TODO 类描述
 * @date: 2024/4/316:33
 **/
@Mapper
public interface TitleMapper {
    Integer insertTitle(TitleModel titleModel);
    int deleteTitleByGUID(String guid);
    int updateTitle(TitleModel titleModel);
    List<TitleModel> selectTitleListByUserName(String userName);
}
