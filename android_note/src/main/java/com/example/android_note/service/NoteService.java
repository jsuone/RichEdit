package com.example.android_note.service;

import com.example.android_note.entity.NoteModel;

import java.util.List;

/**
 * @className: NoteService
 * @description: TODO 类描述
 * @date: 2024/4/423:05
 **/
public interface NoteService {
    int insertNote(NoteModel noteModel);
    int deleteNoteByGUID(String guid);
    int updateNoteByGUID(NoteModel noteModel);
    List<NoteModel> selectNoteListByUserName(String userName);
}
