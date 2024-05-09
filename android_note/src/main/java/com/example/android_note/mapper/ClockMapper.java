package com.example.android_note.mapper;

import com.example.android_note.entity.Item;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @className: ClockMapper
 * @description: TODO 类描述
 * @date: 2024/4/423:33
 **/
@Mapper
public interface ClockMapper {
    int deleteClockByGUID(String guid);
    int updateClock(Item item);
    int insertClock(Item item);
    List<Item> selectClockListByUserName(String userName);
}
