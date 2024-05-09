package com.example.richeditor;

public interface OnInsertImageListener {
    /*插入图片的动作监听处理，需要将返回的图片以Base64编码返回到webview*/
    void openGallery();
}
