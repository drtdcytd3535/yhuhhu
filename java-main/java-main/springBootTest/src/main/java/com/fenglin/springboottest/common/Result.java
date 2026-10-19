package com.fenglin.springboottest.common;

/**
 * 统一接口返回体
 */
public class Result {

    private boolean success;
    private String message;
    private Object data;

    public Result() {
    }

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static Result ok(String message) {
        return new Result(true, message);
    }

    public static Result ok(String message, Object data) {
        Result r = new Result(true, message);
        r.data = data;
        return r;
    }

    public static Result fail(String message) {
        return new Result(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
