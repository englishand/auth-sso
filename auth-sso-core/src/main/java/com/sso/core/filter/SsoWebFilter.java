package com.sso.core.filter;


import com.sso.core.entity.Conf;
import com.sso.core.entity.SsoUser;
import com.sso.core.login.WebLoginHelper;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SsoWebFilter extends HttpServlet implements Filter {

    private String ssoServerPath;
    private String ssoLogoutPath;
    private String ssoExcludedPath;

    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ssoServerPath = filterConfig.getInitParameter(Conf.SSO_SERVER);
        ssoLogoutPath = filterConfig.getInitParameter(Conf.SSO_LOGOUT);
        ssoExcludedPath = filterConfig.getInitParameter(Conf.SSO_EXCLUDED);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String contextPath = req.getContextPath();
        //get servlet path
        String servletPath = req.getServletPath();

        //check excluded path
        if (ssoExcludedPath!=null && ssoExcludedPath.trim().length()>0){
            String[] excludedPaths = ssoExcludedPath.split(",");
            for (String path:excludedPaths){
                if (antPathMatcher.match(servletPath,path)){
                    chain.doFilter(request,response);
                    return;
                }
            }
        }

        //check logout path
        if (ssoLogoutPath!=null && ssoLogoutPath.trim().length()>0){
            if (servletPath.equals(ssoLogoutPath)){
                String logoutPageUrl = ssoServerPath.concat(ssoLogoutPath);
                res.sendRedirect(logoutPageUrl);
                return;
            }
        }

        //valid user
        SsoUser ssoUser = WebLoginHelper.loginCheck(req,res);
        if (ssoUser==null){
            String header = req.getHeader("content-type");
            boolean isJson = header!=null && header.contains("json");
            if (isJson){
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().println("{\"code\":"+Conf.SSO_LOGIN_FAIL_RESULT.getCode()+",\"msg\":"+Conf.SSO_LOGIN_FAIL_RESULT.getMsg()+"}");
                return;
            }else {
                String link = req.getRequestURL().toString();
                String loginPageUrl = ssoServerPath.concat(Conf.SSO_LOGIN)+"?"+Conf.REDIRECT_URL+"="+link;
                res.sendRedirect(loginPageUrl);
                return;
            }
        }

        //save sso user
        request.setAttribute(Conf.SSO_USER,ssoUser);

        //already login ,allow
        chain.doFilter(request,response);
        return;
    }


}
