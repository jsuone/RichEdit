package com.example.sample.Impl;

import java.util.List;

public interface OnNoteListChangeListener {
     void finishLoadMore();
     void finishLoadMoreNoData();
     void setNoMoreData();
}
