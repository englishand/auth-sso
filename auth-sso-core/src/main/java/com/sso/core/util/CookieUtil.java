package com.sso.core.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieUtil {

    //保存路径
    private static final String COOKIE_PATH="/";
    //默认缓存时间，单位/秒，2H
    private static final int COOKIE_MAX_AGE = 2*60*60;

    public static String getCookieValue(HttpServletRequest request,String key){
        Cookie cookie = getCookie(request,key);
        if (cookie!=null){
            return cookie.getValue();
        }else {
            return null;
        }
    }

    public static Cookie getCookie(HttpServletRequest request,String key){
        Cookie[] cookies = request.getCookies();
        if (cookies!=null && cookies.length>0){
            for (Cookie cookie:cookies){
                if (cookie.getName().equals(key)){
                    return cookie;
                }
            }
        }
        return null;
    }

    /**
     * 设置cookie的准备
     * @param response
     * @param key
     * @param value
     * @param ifRem
     */
    public static void set(HttpServletResponse response,String key,String value,boolean ifRem){
        int age = ifRem?COOKIE_MAX_AGE:-1;
        setCookie(response,key,value,COOKIE_PATH,null,age,true);
    }

    public static void setCookie(HttpServletResponse response,String key,String value,String path,String domain,int age,boolean isHttpOnly){
        Cookie cookie = new Cookie(key,value);
        if (domain!=null) {
            cookie.setDomain(domain);
        }
        cookie.setMaxAge(age);
        cookie.setPath(path);

        cookie.setHttpOnly(isHttpOnly);//若此属性为True，则只有在http请求头中会有此cookie信息，而不能通过document.cookie来访问此cookie。
        response.addCookie(cookie);
    }

    public static void remove(HttpServletRequest request,HttpServletResponse response,String key){
        Cookie cookie = getCookie(request,key);
        if (cookie!=null){
            setCookie(response,key,"",COOKIE_PATH,null,0,true);
        }
    }
}
