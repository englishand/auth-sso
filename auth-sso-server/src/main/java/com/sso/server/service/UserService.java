package com.sso.server.service;

import com.sso.core.entity.SsoUser;
import com.sso.core.result.ResultT;

public interface UserService {

    ResultT<SsoUser> findUser(String username,String password);
}
