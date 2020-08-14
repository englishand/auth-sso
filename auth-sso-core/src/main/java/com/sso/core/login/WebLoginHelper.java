package com.sso.core.login;

import com.sso.core.entity.Conf;
import com.sso.core.entity.SsoUser;
import com.sso.core.store.SsoLoginStore;
import com.sso.core.store.SsoSessionIdHelper;
import com.sso.core.util.CookieUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WebLoginHelper {

    /**
     * login check
     * @param request
     * @param response
     * @return
     */
    public static SsoUser loginCheck(HttpServletRequest request, HttpServletResponse response){
        String sessionId = CookieUtil.getCookieValue(request, Conf.SSO_SESSIONID);
        SsoUser ssoUser = LoginHelper.loginCheck(sessionId);
        if (ssoUser!=null){
            return ssoUser;
        }

        //remove old cookie，因为setCookie的时候设置了maxAge,会存在遗留session
        removeSessionByCookie(request,response);
        //set new cookie
        String paramSessionId = request.getParameter(Conf.SSO_SESSIONID);
        ssoUser = LoginHelper.loginCheck(paramSessionId);
        if (ssoUser!=null){
            CookieUtil.set(response,Conf.SSO_SESSIONID,paramSessionId,false);
            return ssoUser;
        }
        return null;
    }

    /**
     * 登录成功，cookie,redis存储
     * @param sessionId
     * @param response
     * @param ifRem
     * @param ssoUser
     */
    public static void login(String sessionId,HttpServletResponse response,boolean ifRem,SsoUser ssoUser){
        CookieUtil.set(response,Conf.SSO_SESSIONID,sessionId,ifRem);

        String storeKey = SsoSessionIdHelper.parseStoreKey(sessionId);
        if (storeKey!=null){
            SsoLoginStore.put(storeKey,ssoUser);
        }else {
            throw new RuntimeException("parseStoreKey fail,sessionId:"+ sessionId);
        }
    }

    /**
     * 登出，cookie,redis删除存储信息
     * @param request
     * @param response
     */
    public static void logout(HttpServletRequest request,HttpServletResponse response){
        String sessionId = CookieUtil.getCookieValue(request,Conf.SSO_SESSIONID);
        if (sessionId==null){
            return;
        }
        CookieUtil.remove(request,response,Conf.SSO_SESSIONID);

        String storeKey = SsoSessionIdHelper.parseStoreKey(sessionId);
        if (storeKey!=null){
            SsoLoginStore.remove(storeKey);
        }
    }

    /**
     * 删除cookie
     * @param request
     * @param response
     */
    public static void removeSessionByCookie(HttpServletRequest request,HttpServletResponse response){
        CookieUtil.remove(request,response,Conf.SSO_SESSIONID);
    }
}
