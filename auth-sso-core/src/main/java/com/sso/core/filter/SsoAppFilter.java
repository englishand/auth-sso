package com.sso.core.filter;

import com.sso.core.entity.Conf;
import com.sso.core.entity.SsoUser;
import com.sso.core.login.AppLoginHelper;
import com.sso.core.result.ResultT;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SsoAppFilter extends HttpServlet implements Filter {

    private String ssoServerPath;
    private String ssoLogoutpath;
    private String ssoExcludedPaths;

    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ssoServerPath = filterConfig.getInitParameter(Conf.SSO_SERVER);
        ssoLogoutpath = filterConfig.getInitParameter(Conf.SSO_LOGOUT);
        ssoExcludedPaths = filterConfig.getInitParameter(Conf.SSO_EXCLUDED);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if (request instanceof HttpServletRequest){
            System.out.println("这是子类对象");
        }
        /**
         * ServletRequest request；这个是将子类对象赋给父类引用，他运行时的类型是子类，编译时的类型是父类。
         * 但是在运行时，父类类型对象调用的方法如果子类里面有，那就执行子类里面的方法，如果编译时的类型也就是父类没有调用的那个方法，则报错。
         * 所以在那里要做一个强制类型转换，否则就会报错。
         */
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        //get servlet path
        String servletPath = req.getServletPath();
        //check excluded paths
        if (ssoExcludedPaths!=null && ssoExcludedPaths.trim().length()>0){
            String[] paths = ssoExcludedPaths.split(",");
            for (String path:paths){
                if (antPathMatcher.match(path,servletPath)){
                    chain.doFilter(request,response);
                    return;
                }
            }
        }

        //check logout
        if (ssoLogoutpath!=null && ssoLogoutpath.trim().length()>0){
            if (servletPath.equals(ssoLogoutpath)){
                res.setStatus(HttpServletResponse.SC_OK);
                res.setContentType("application/json;charset=utf-8");
                res.getWriter().println("{\"code\":"+ ResultT.SUCCESS_CODE +",\"msg\":\"\"}");
                return;
            }
        }

        //check sso user
        SsoUser ssoUser = AppLoginHelper.loginCheck(req,res);
        if (ssoUser==null){
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("application/json;charset=utf-8");
            res.getWriter().println("{\"code\":"+Conf.SSO_LOGIN_FAIL_RESULT.getCode()+",\"msg\":"+Conf.SSO_LOGIN_FAIL_RESULT.getMsg()+"}");
            return;
        }

        //save sso user
        req.setAttribute(Conf.SSO_USER,ssoUser);

        //already login, allow
        chain.doFilter(request,response);
        return;
    }
}
