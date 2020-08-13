package com.sso.core.result;

import java.io.Serializable;

public class ResultT<T> implements Serializable {

    private static final long serialVersionUID=42L;

    public static int SUCCESS_CODE = 200;
    public static int FAIL_CODE = 500;

    private int code;
    private T data;
    private String msg;

    public ResultT(T data){
        this.code = SUCCESS_CODE;
        this.data = data;
    }
    public ResultT(int code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public static ResultT<String> SUCCESS = new ResultT<String>(null);
    public static ResultT<String> FAIL  = new ResultT<String>(FAIL_CODE,null);

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
