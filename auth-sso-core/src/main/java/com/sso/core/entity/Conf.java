package com.sso.core.entity;

import com.sso.core.result.ResultT;

public class Conf {

    public static final String SSO_SESSIONID="sso_sessionid";

    public static final String REDIRECT_URL = "redirect_url";

    public static final String SSO_SERVER = "sso_server";
    public static final String SSO_LOGOUT = "sso_logout";
    public static final String SSO_EXCLUDED = "sso_excluded";

    public static final String SSO_USER = "sso_user";

    public static final String SSO_LOGIN = "/login";

    public static final ResultT<String> SSO_LOGIN_FAIL_RESULT = new ResultT<String>(501,"sso not login");
}
