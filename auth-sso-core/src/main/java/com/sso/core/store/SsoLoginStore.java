package com.sso.core.store;

import com.sso.core.entity.Conf;
import com.sso.core.entity.SsoUser;
import com.sso.core.util.JedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;

public class SsoLoginStore {

    public static int redisExpireMinite=30;
    public static void setRedisExpireMinite(int expireMinite){
        if (expireMinite<30){
            expireMinite=30;
        }
        SsoLoginStore.redisExpireMinite = expireMinite;
    }

    public static SsoUser getSsoUser(String storeKey){
        String redisKey = getRedisKey(storeKey);
        SsoUser ssoUser = (SsoUser) JedisUtil.getRedisValue(redisKey);
        if (ssoUser!=null){
            return ssoUser;
        }
        return null;
    }

    /**
     * 根据storeKey获取RedisKey
     * @param storeKey
     * @return
     */
    public static String getRedisKey(String storeKey){
        if (storeKey!=null && storeKey.trim().length()>0){
            return Conf.SSO_SESSIONID.concat("#").concat(storeKey);
        }
        return null;
    }

    /**
     * redis存储用户信息
     * @param storeKey
     * @param ssoUser
     * @return
     */
    public static void put(String storeKey, SsoUser ssoUser){
        String redisKey = getRedisKey(storeKey);
        JedisUtil.setValue(redisKey,ssoUser,SsoLoginStore.redisExpireMinite);
    }

    public static void remove(String storeKey){
        String redisKey = getRedisKey(storeKey);
        JedisUtil.del(redisKey);
    }
}
