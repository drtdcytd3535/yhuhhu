package com.fenglin.springboottest.common;

/**
 * 登录态相关的 session key 常量。
 * 前台与后台各用一套，互不干涉。
 */
public final class AuthKeys {

    /** 前台登录态：值为道号 */
    public static final String LOGIN_USER = "LOGIN_USER";

    /** 后台（执事堂）登录态：值为道号 */
    public static final String ADMIN_USER = "ADMIN_USER";

    private AuthKeys() {
    }
}
