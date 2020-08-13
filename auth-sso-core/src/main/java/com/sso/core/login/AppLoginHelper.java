package com.sso.core.login;

import com.sso.core.entity.Conf;
import com.sso.core.entity.SsoUser;
import com.sso.core.store.SsoLoginStore;
import com.sso.core.store.SsoSessionIdHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AppLoginHelper {

    public static SsoUser loginCheck(HttpServletRequest request, HttpServletResponse response){
        String sessionId = request.getHeader(Conf.SSO_SESSIONID);
        if (sessionId!=null){
            SsoUser ssoUser = LoginHelper.loginCheck(sessionId);
            if (ssoUser!=null){
                return ssoUser;
            }
        }
        return null;
    }

    public static void logout(String sessoinId){
        String storeKey = SsoSessionIdHelper.parseStoreKey(sessoinId);
        if (storeKey==null){
            return;
        }
        SsoLoginStore.remove(storeKey);
    }

    public static void login(String sessionId,SsoUser ssoUser){
        String storeKey = SsoSessionIdHelper.parseStoreKey(sessionId);
        if (storeKey==null){
            throw new RuntimeException("parseStoreKey fail,sessionId:"+sessionId);
        }
        SsoLoginStore.put(storeKey,ssoUser);
    }
}
