package com.sso.server.controller;

import com.sso.core.entity.Conf;
import com.sso.core.entity.SsoUser;
import com.sso.core.login.LoginHelper;
import com.sso.core.login.WebLoginHelper;
import com.sso.core.result.ResultT;
import com.sso.core.store.SsoSessionIdHelper;
import com.sso.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Controller
public class WebLoginController {

    @Autowired
    private UserService userService;

    @RequestMapping("/")
    public String index(Model model,HttpServletRequest request, HttpServletResponse response){
        SsoUser ssoUser = WebLoginHelper.loginCheck(request,response);
        if (ssoUser!=null){
            model.addAttribute("ssoUser",ssoUser);
            return "index";
        }
        return "redirect:/login";
    }

    @RequestMapping("/login")
    public String login(Model model,HttpServletRequest request,HttpServletResponse response){
        SsoUser ssoUser = WebLoginHelper.loginCheck(request,response);
        if (ssoUser!=null){
            String redirectUrl = request.getParameter(Conf.REDIRECT_URL);
            if (redirectUrl!=null && redirectUrl.trim().length()>0){
                String sessionid = LoginHelper.getSessoinIdByCookie(request);
                redirectUrl = redirectUrl+"?"+Conf.SSO_SESSIONID+"="+sessionid;
                return "redirect:"+redirectUrl;
            }else {
                return "redirect:/";
            }
        }
        model.addAttribute("errorMsg",request.getParameter("errorMsg"));
        model.addAttribute(Conf.REDIRECT_URL,request.getParameter(Conf.REDIRECT_URL));
        return "login";
    }

    @RequestMapping("dologin")
    public String doLogin(String username, String password, String ifRemember, RedirectAttributes redirectAttributes,
                          HttpServletRequest request,HttpServletResponse response){

        boolean ifRem = (ifRemember!=null && "on".equals(ifRemember))?true:false;

        ResultT<SsoUser> resultT = userService.findUser(username,password);
        if (resultT.getCode()==ResultT.SUCCESS_CODE){

            //make sso user
            SsoUser ssoUser = resultT.getData();
            //make sessionId
            String sessionId = SsoSessionIdHelper.getSessionId(ssoUser);
            log.info("WebLoginController--sessionid:"+sessionId);
            //login store
            WebLoginHelper.login(sessionId,response,ifRem,ssoUser);

            String redirectUrl = request.getParameter(Conf.REDIRECT_URL);
            if (redirectUrl!=null && redirectUrl.trim().length()>0){
                redirectUrl = redirectUrl+"?"+Conf.SSO_SESSIONID+"="+sessionId;
                return "redirect:"+redirectUrl;
            }else {
                return "redirect:/";
            }
        }else {
            redirectAttributes.addAttribute("errorMsg",resultT.getMsg());
            redirectAttributes.addAttribute(Conf.REDIRECT_URL,request.getParameter(Conf.REDIRECT_URL));
            return "redirect:/login";
        }
    }

    @RequestMapping("/logout")
    public String logout(HttpServletRequest request ,HttpServletResponse response,RedirectAttributes redirectAttributes){
        WebLoginHelper.logout(request,response);
        redirectAttributes.addAttribute(Conf.REDIRECT_URL,request.getParameter(Conf.REDIRECT_URL));
        return "redirect:/login";
    }
}
