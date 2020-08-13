package com.sso.server.config;

import com.sso.core.store.SsoLoginStore;
import com.sso.core.util.JedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 用来初始化redis获取Jedis实例
 * InitializingBean为bean提供了初始化的方法，只包括afterProperiesSet方法，凡是继承该接口的类，在初始化bean时会调用该方法。
 * 实现DisposableBean的bean允许在容器销毁该bean的时候获得一次回调，执行destroy方法。（与常用的析构方法作用一样。）
 */
@Slf4j
@Configuration
public class SsoServerConfig implements InitializingBean, DisposableBean {

    @Value("${sso.redis.address}")
    private String ssoRedisAddress;
    @Value("${sso.redis.expire.minite}")
    private int ssoRedisExpireMinite;

    public void destroy() throws Exception {
        JedisUtil.close();
    }

    public void afterPropertiesSet() throws Exception {
        SsoLoginStore.setRedisExpireMinite(ssoRedisExpireMinite);
        JedisUtil.init(ssoRedisAddress);
    }
}
