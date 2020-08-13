package com.sso.product.controller;

import com.sso.core.entity.Conf;
import com.sso.core.entity.SsoUser;
import com.sso.core.result.ResultT;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class IndexController {

    @RequestMapping("/")
    @ResponseBody
    public ResultT<SsoUser> index(HttpServletRequest request){
        SsoUser ssoUser = (SsoUser) request.getAttribute(Conf.SSO_USER);
        return new ResultT<SsoUser>(ssoUser);
    }
}
