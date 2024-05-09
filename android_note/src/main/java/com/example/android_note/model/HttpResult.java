package com.example.android_note.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @className: HttpResult
 * @description: TODO 类描述
 * @date: 2023/5/102:17
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HttpResult<T> {
public int code;
public String msg;
public T data;
}
