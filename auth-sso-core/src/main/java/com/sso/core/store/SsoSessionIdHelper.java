package com.sso.core.store;

import com.sso.core.entity.SsoUser;

public class SsoSessionIdHelper {

    /**
     * 根据ssouser生成sessionid
     * @param ssoUser
     * @return
     */
    public static String getSessionId(SsoUser ssoUser){
        if (ssoUser!=null){
            return ssoUser.getUserId().concat("_").concat(ssoUser.getVersion());
        }
        return null;
    }

    /**
     * 解析sessionid,获取用户version
     * @param sessionId
     * @return
     */
    public static String parseVersion(String sessionId){
        String[] sessionIdStr = sessionId.split("_");
        if (sessionIdStr.length==2 && sessionIdStr[1].trim().length()>0){
            return sessionIdStr[1];
        }
        return null;
    }

    /**
     * 解析sessionId,获取用户userId
     * @param sessionId
     * @return
     */
    public static String parseStoreKey(String sessionId){
        String[] sessionIdStr = sessionId.split("_");
        if (sessionIdStr.length==2 && sessionIdStr[0].trim().length()>0){
            return sessionIdStr[0];
        }
        return null;
    }
}
