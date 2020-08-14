package com.sso.member.controller;

import com.sso.core.entity.Conf;
import com.sso.core.entity.SsoUser;
import com.sso.core.result.ResultT;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class IndexController {

    @RequestMapping("/")
    public String index(Model model,HttpServletRequest request, HttpServletResponse response){
        SsoUser ssoUser = (SsoUser) request.getAttribute(Conf.SSO_USER);
        model.addAttribute("ssoUser",ssoUser);
        return "index";
    }

    @RequestMapping("/json")
    public ResultT json(HttpServletRequest request){
        SsoUser ssoUser = (SsoUser) request.getAttribute(Conf.SSO_USER);
        return new ResultT(ssoUser);
    }
}
