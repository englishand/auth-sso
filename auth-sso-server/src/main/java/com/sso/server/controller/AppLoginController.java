package com.sso.server.controller;

import com.sso.core.entity.SsoUser;
import com.sso.core.login.AppLoginHelper;
import com.sso.core.login.LoginHelper;
import com.sso.core.result.ResultT;
import com.sso.core.store.SsoSessionIdHelper;
import com.sso.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/app")
public class AppLoginController {

    @Autowired
    private UserService userService;

    @RequestMapping("/login")
    @ResponseBody
    public ResultT<String> login(String username,String password){
        //valid login user
        ResultT<SsoUser> resultT = userService.findUser(username,password);
        if (resultT.getCode()==ResultT.SUCCESS_CODE){
            SsoUser ssoUser = resultT.getData();
            //make sessionId
            String sessionId = SsoSessionIdHelper.getSessionId(ssoUser);
            //redis store
            AppLoginHelper.login(sessionId,ssoUser);
            return new ResultT<>(sessionId);
        }
        return new ResultT<>(resultT.getCode(),resultT.getMsg());
    }

    @RequestMapping("/checkSessionId")
    @ResponseBody
    public ResultT<SsoUser> checkSessionId(String sessionId){
        if (sessionId!=null && sessionId.trim().length()>0){
            SsoUser ssoUser = LoginHelper.loginCheck(sessionId);
            if (ssoUser!=null){
                return new ResultT<>(ssoUser);
            }else {
                return new ResultT<>(ResultT.FAIL_CODE,"sso not login");
            }
        }
        return new ResultT<>(ResultT.FAIL_CODE,"sessionId is null");
    }

    @RequestMapping("/logout")
    @ResponseBody
    public ResultT<String> logout(String sessionId){
        if (sessionId!=null){
            AppLoginHelper.logout(sessionId);
            return new ResultT(ResultT.SUCCESS_CODE);
        }
        return new ResultT<>(ResultT.FAIL_CODE,"sessionId is null");
    }
}
