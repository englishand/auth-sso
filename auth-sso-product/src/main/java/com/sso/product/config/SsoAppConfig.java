package com.sso.product.config;

import com.sso.core.entity.Conf;
import com.sso.core.filter.SsoAppFilter;
import com.sso.core.util.JedisUtil;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SsoAppConfig implements DisposableBean {

    @Value("${sso.redis.address}")
    private String ssoRedisAddress;
    @Value("${sso.server.path}")
    private String ssoServerPath;
    @Value("${sso.logout.path}")
    private String ssoLogoutPath;
    @Value("${sso.excluded.paths}")
    private String ssoExcludedPaths;

    @Bean
    public FilterRegistrationBean SsoAppFilterRegistrationBean(){
        //sso redis init;
        JedisUtil.init(ssoRedisAddress);

        //filter init
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setOrder(1);//设置优先级，顶级
        registrationBean.setFilter(new SsoAppFilter());
        registrationBean.setName("SsoAppFilter");
        registrationBean.addUrlPatterns("/*");//过滤所有路径

        /**
         * 初始化自定义的过滤器的参数，添加后可以在filter中获取：filterConfig.getInitParameter("paramName");
         */
        registrationBean.addInitParameter(Conf.SSO_SERVER,ssoServerPath);
        registrationBean.addInitParameter(Conf.SSO_EXCLUDED,ssoExcludedPaths);
        registrationBean.addInitParameter(Conf.SSO_LOGOUT,ssoLogoutPath);

        return registrationBean;
    }

    @Override
    public void destroy() throws Exception {
        JedisUtil.close();
    }
}
