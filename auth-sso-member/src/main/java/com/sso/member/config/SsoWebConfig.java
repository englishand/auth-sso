package com.sso.member.config;

import com.sso.core.entity.Conf;
import com.sso.core.filter.SsoWebFilter;
import com.sso.core.util.JedisUtil;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SsoWebConfig implements DisposableBean {

    @Value("${sso.redis.address}")
    private String ssoRedisAddress;
    @Value("${sso.logout.path}")
    private String ssoLogoutPath;
    @Value("${sso.server.path}")
    private String ssoServerPath;
    @Value("${sso.excluded.paths}")
    private String ssoExcludedPaths;

    @Bean
    public FilterRegistrationBean SsoWebFilterRegistrationBean(){
        //sso redis init
        JedisUtil.init(ssoRedisAddress);

        //filter init
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setName("SsoWebFilter");
        registrationBean.setFilter(new SsoWebFilter());
        registrationBean.setOrder(1);//设置优先级
        registrationBean.addUrlPatterns("/*");//过滤所有路径

        /**
         * 初始化自定义过滤器的参数，后边可以在过滤器中获取，filterConfig.getInitParameter("paramName");
         */
        registrationBean.addInitParameter(Conf.SSO_SERVER,ssoServerPath);
        registrationBean.addInitParameter(Conf.SSO_LOGOUT,ssoLogoutPath);
        registrationBean.addInitParameter(Conf.SSO_EXCLUDED,ssoExcludedPaths);

        return registrationBean;
    }

    public void destroy() throws Exception {
        JedisUtil.close();
    }
}
