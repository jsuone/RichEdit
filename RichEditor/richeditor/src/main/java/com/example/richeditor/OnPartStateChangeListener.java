package com.example.richeditor;

import java.util.List;
//编辑器各样式状态改变的监听事件
public interface OnPartStateChangeListener {
    void onPartStateChange(List<EditorOpType> types);
}
