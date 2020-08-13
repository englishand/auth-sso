package com.sso.core.login;

import com.sso.core.entity.SsoUser;
import com.sso.core.store.SsoLoginStore;
import com.sso.core.store.SsoSessionIdHelper;

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
}
