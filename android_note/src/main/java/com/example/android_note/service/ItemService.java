package com.example.android_note.service;

import com.example.android_note.entity.Item;

import java.util.List;

/**
 * @className: ItemService
 * @description: TODO 类描述
 * @date: 2024/4/50:04
 **/
public interface ItemService {
    int insertItem(Item item);
    int deleteItemByGUID(String guid);
    int updateItem(Item item);
    List<Item> selectClockListByUserName(String userName);
}
