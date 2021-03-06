package com.sso.server.service.impl;

import com.sso.core.entity.SsoUser;
import com.sso.core.result.ResultT;
import com.sso.core.store.SsoLoginStore;
import com.sso.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    public ResultT<SsoUser> findUser(String username, String password) {
        if (StringUtils.isEmpty(username)){
            return new ResultT<SsoUser>(ResultT.FAIL_CODE,"用户名不能为空");
        }
        if (StringUtils.isEmpty(password)){
            return new ResultT<SsoUser>(ResultT.FAIL_CODE,"密码不能为空");
        }
        if (username.equals("zhy") && password.equals("zhy")){
            SsoUser ssoUser = new SsoUser();
            ssoUser.setExpireFreshMinite(System.currentTimeMillis());
            ssoUser.setExpireMinite(SsoLoginStore.redisExpireMinite);
            ssoUser.setPassword(password);
            ssoUser.setUserId("000000001");
            ssoUser.setUsername(username);
            String version = UUID.randomUUID().toString().replaceAll("-","");
            log.info("UserService---version:"+version);
            ssoUser.setVersion(version);
            return new ResultT<SsoUser>(ssoUser);
        }
        return new ResultT<SsoUser>(ResultT.FAIL_CODE,"用户名或密码错误");
    }
}
