package com.sso.core.login;

import com.sso.core.entity.Conf;
import com.sso.core.entity.SsoUser;
import com.sso.core.store.SsoLoginStore;
import com.sso.core.store.SsoSessionIdHelper;
import com.sso.core.util.CookieUtil;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

public class LoginHelper {

    /**
     * login check
     * @param sessionId
     * @return
     */
    public static SsoUser loginCheck(String sessionId){
        String storeKey = SsoSessionIdHelper.parseStoreKey(sessionId);
        if (storeKey!=null){
            SsoUser ssoUser = SsoLoginStore.getSsoUser(storeKey) ;
            if (ssoUser!=null &&ssoUser.getVersion().equals(SsoSessionIdHelper.parseVersion(sessionId))){
                /**
                 * 业务上的逻辑
                 * if the user expireMinite has half passed,fresh
                 */
                if ((System.currentTimeMillis()-ssoUser.getExpireFreshMinite())>ssoUser.getExpireMinite()/2){
                    ssoUser.setExpireFreshMinite(System.currentTimeMillis());
                    SsoLoginStore.put(storeKey,ssoUser);
                }
                return ssoUser;
            }
        }
        return null;
    }

    /**
     * 根据cookie获取sessionid
     * @param request
     * @return
     */
    public static String getSessoinIdByCookie(HttpServletRequest request){
        String sessionid = CookieUtil.getCookieValue(request, Conf.SSO_SESSIONID);
        return sessionid;
    }
}
