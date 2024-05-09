package com.example.android_note.model;

import lombok.AllArgsConstructor;

/**
 * @className: ResponseConstant
 * @description: TODO 类描述
 * @date: 2023/5/102:19
 **/
@AllArgsConstructor
public enum ResponseConstant {
    SUCESS(200,"响应成功"),LOGIN_FAIL(404,"密码错误"),USER_NAME_REPEAT(404,"用户名已经存在"),NOT_EXIST(404,"账号不存在"),REGISTER_FAIL(404,"注册失败"),
    SECURITY_QUESTION_NOT_EXIST(404,"密保问题不存在"),USER_HAS_LOGIN(500,"已有设备处于登录状态"),TOKEN_EXPIRE(404,"令牌已经过期或者非法"),PASSWORD_HAS_CHANGED(404,"密码已经修改，请重新登录");
    public int code;
    public String msg;

}
