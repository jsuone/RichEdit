package com.example.android_note.service.Impl;

import com.example.android_note.entity.Item;
import com.example.android_note.mapper.ClockMapper;
import com.example.android_note.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @className: ItemServiceImpl
 * @description: TODO 类描述
 * @date: 2024/4/50:04
 **/
@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    ClockMapper clockMapper;
    @Override
    public int insertItem(Item item) {
        return clockMapper.insertClock(item);

    }

    @Override
    public int deleteItemByGUID(String guid) {
        return clockMapper.deleteClockByGUID(guid);
    }

    @Override
    public int updateItem(Item item) {
        return clockMapper.updateClock(item);
    }

    @Override
    public List<Item> selectClockListByUserName(String userName) {
        return clockMapper.selectClockListByUserName(userName);
    }
}
