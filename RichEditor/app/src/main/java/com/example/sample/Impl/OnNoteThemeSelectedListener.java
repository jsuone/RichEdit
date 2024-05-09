package com.example.sample.Impl;

import java.util.List;

public interface OnNoteThemeSelectedListener {
    void onNoteThemeSelected(List<String> titleGUID);
    void onNoteThemeDeleted(List<String> titleGUID);
    void updateThemeList(List<String> titleGUID);
    void deleteNoteByNoteID(List<String> noteGUID);
    void setTopTile(String uuid);
}
