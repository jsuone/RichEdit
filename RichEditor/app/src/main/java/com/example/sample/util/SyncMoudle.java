package com.example.sample.util;

public enum SyncMoudle {
    STRAT_SYNC_PROGRESS(109),
    IN_SYNC_PROGRESS(110),
    END_SYNC_PROGRESS_STRAT_WEB(111),
    END_SYNC_PROGRESS_STRAT_ANDROID(112),
    IN_SYNC_ANDROID_UPDATE_PROGRESS(113),
    END_SYNC_PROGRESS_THE_SAME(114),
    CHARACTER_TRANSMISSION(0),
    PICTURE_TRANSMISSION(1);
    public int code;

    SyncMoudle(int code) {
        this.code = code;
    }
}
