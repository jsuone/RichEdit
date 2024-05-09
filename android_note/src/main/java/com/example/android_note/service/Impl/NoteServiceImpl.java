package com.example.android_note.service.Impl;

import com.example.android_note.entity.NoteModel;
import com.example.android_note.mapper.NoteMapper;
import com.example.android_note.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @className: NoteServiceImpl
 * @description: TODO 类描述
 * @date: 2024/4/423:06
 **/
@Service
public class NoteServiceImpl implements NoteService {
    @Autowired
    NoteMapper noteMapper;
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    @Override
    public int insertNote(NoteModel noteModel) {
        noteMapper.insertNote(noteModel);
        noteMapper.insertNoteOfTitle(noteModel);
        return 0;
    }

    @Override
    public int deleteNoteByGUID(String guid) {
        return noteMapper.deleteNoteByGUID(guid);
    }
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    @Override
    public int updateNoteByGUID(NoteModel noteModel) {
        int i = 0,j = 0;
        i =  noteMapper.updateNote(noteModel);
        j =  noteMapper.updateNoteOfTitle(noteModel);
        return i+j;
    }

    @Override
    public List<NoteModel> selectNoteListByUserName(String userName) {
        return noteMapper.selectNoteListByUserName(userName);
    }
}
