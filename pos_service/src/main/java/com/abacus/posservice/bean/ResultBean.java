package com.abacus.posservice.bean;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.netty.handler.codec.http.HttpResponseStatus;


public class ResultBean<T> {
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ResultBean(int code,
                      T data,
                      String msg) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ResultBean() {
    }

    public ResultBean(T data) {
        this.code = HttpResponseStatus.OK.code();
        this.msg = HttpResponseStatus.OK.toString();
        this.data = data;
    }

    int code;
    String msg;
    T data;

    @Override
    public String toString() {
        return "ResultBean{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
