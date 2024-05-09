package com.example.android_note.mapper;

import com.example.android_note.entity.NoteModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @className: NoteMapper
 * @description: TODO 类描述
 * @date: 2024/4/422:55
 **/
@Mapper
public interface NoteMapper {
    Integer insertNote(NoteModel noteModel);
    Integer deleteNoteByGUID(String guid);
    Integer updateNote(NoteModel noteModel);
    Integer insertNoteOfTitle(NoteModel noteModel);
    Integer updateNoteOfTitle(NoteModel noteModel);
    List<NoteModel> selectNoteListByUserName(String UserName);
}
