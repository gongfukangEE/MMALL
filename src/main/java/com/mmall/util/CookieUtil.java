package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Auther gongfukang
 * @Date 7/6 19:07
 */
@Slf4j
public class CookieUtil {

    // 写在一级域名下
    private final static String COOKIE_DOMAIN = ".moocmall.com";
    private final static String COOKIE_NAME = "mmall_login_token";

    /**
     * 写入 cookie
     */
    public static void writeLoginToken(HttpServletResponse response, String token) {
        Cookie ck = new Cookie(COOKIE_NAME, token);
        ck.setDomain(COOKIE_DOMAIN);
        ck.setPath("/");    // 代表设置在根目录
        ck.setHttpOnly(true);
        ck.setMaxAge(60 * 60 * 24 * 365);   // cookie 有效期，单位是 sec。-1 表示永久，如果不设置，cookie 不写入硬盘，而是写入内存，只在当前页面有效
        log.info("write cookieName:{}, cookieValue:{}", ck.getName(), ck.getValue());
        response.addCookie(ck);
    }

    /**
     * 读取 cookie
     */
    public static String readLoginToken(HttpServletRequest request) {
        Cookie[] cks = request.getCookies();
        if (cks != null) {
            for (Cookie ck : cks) {
                log.info("read cookieName:{}, cookieValue:{}", ck.getName(), ck.getValue());
                if (StringUtils.equals(ck.getName(),COOKIE_NAME)) {
                    log.info("return cookieName:{}, cookieValue:{}", ck.getName(), ck.getValue());
                    return ck.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 删除 cookie
     */
    public static void delLoginToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cks = request.getCookies();
        if (cks != null) {
            for (Cookie ck : cks) {
                if (StringUtils.equals(ck.getName(), COOKIE_NAME)) {
                    ck.setDomain(COOKIE_DOMAIN);
                    ck.setMaxAge(0);    // 设置为 0 代表删除此 cookie
                    ck.setPath("/");
                    log.info("del cookieName:{}, cookieValue:{}", ck.getName(), ck.getValue());
                    response.addCookie(ck);
                    return;
                }
            }
        }
    }
}
