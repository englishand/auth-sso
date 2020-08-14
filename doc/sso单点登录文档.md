
一．简介

1．什么是单点登录？

    单点登录（Single Sign On），简称为 SSO，是比较流行的企业业务整合的解决方案之一。
    SSO的定义是在多个应用系统中，用户只需要登录一次就可以访问所有相互信任的应用系统。

2．实现方法

    server端
    以server群如何生成、验证ID的方式大致分为两种：
    “共享Cookie”这个就是上面提到的共享session的方式，我倒觉得叫“共享session”来得好一点，
    本质上cookie只是存储session-id的介质，session-id也可以放在每一次请求的url里。
    据说这种方式不安全。其实也是，毕竟session这项机制一开始就是一个server一个session的，把session拿出来让所有server共享。

    浏览器端
    单点登录还有非常关键的一步，这一步跟server端验证token的方式无关，
    用最早的“共享session”的方式还是现在的“token”方式，
    身份标识到了浏览器端都要面临这样的一个问题：用户登录成功拿到token(或者是session-id)后怎么让浏览器存储和分享到其它域名下？
    同域名很简单，把token存在cookie里，把cookie的路径设置成顶级域名下，这样所有子域都能读取cookie中的token。
    这就是共享cookie的方式（这才叫共享Cookie嘛，上面那个应该叫共享session）。

3．优缺点

    优点：
        1）提高用户的效率。
        用户不再被多次登录困扰，也不需要记住多个 ID 和密码。另外，用户忘记密码并求助于支持人员的情况也会减少。
        2）提高开发人员的效率。
        SSO 为开发人员提供了一个通用的身份验证框架。实际上，如果 SSO 机制是独立的，那么开发人员就完全不需要为身份验证操心。
        他们可以假设，只要对应用程序的请求附带一个用户名，身份验证就已经完成了。
        3）简化管理。
        如果应用程序加入了单点登录协议，管理用户帐号的负担就会减轻。简化的程度取决于应用程序，因为 SSO 只处理身份验证。
        所以，应用程序可能仍然需要设置用户的属性（比如访问特权）。
    缺点：
        1）不利于重构
        因为涉及到的系统很多，要重构必须要兼容所有的系统，可能很耗时。
        2） 无人看守桌面
        因为只需要登录一次，所有的授权的应用系统都可以访问，可能导致一些很重要的信息泄露。
4．本系统特性

    1.简洁：api直观，可快速上手。
    2.轻量级：以来环境小，部署与接入成本较低。
    3.单点登录：只需要登录一次就可以访问所有相互信任的应用系统。
    4.分布式：暂无
    5.HA:暂无
    6.跨域：支持跨域应用接入sso认证中心
    7.Cookie+token均支持，支持基于Cookie和基于Token两种接入方式。
    8.Web+App:支持web和app接入
    9.实时性：系统登录、注销状态，server和client端实时共享。
    10.CS结构：基于CS结构，包括Server“认证中心”与Client"受保护的应用“
    11.记住密码：未记住密码时，关闭浏览器则登录态失效；记住密码时，支持登录态自动延期，在自定义延期时间的基础上，原则上可以无限延期
    12.路径排除：支持自定义多个排除路径，支持Ant模式，用户排除SSO客户端不需要过滤的路径。
二．环境

    Jdk: 1.8+
    Reids: 3.2+
三．项目源码

1．流程分析

    1)用户于Client端应用访问受限资源时，将会自动 redirect 到 SSO Server 进入统一登录界面
    2)用户登录成功之后将会为用户分配 SSO SessionId 并 redirect 返回来源Client端应用
    3)在Client端的SSO Filter里验证 SSO SessionId 无误，将 SSO SessionId 写入到用户浏览器Client端域名下 cookie 中
    4)SSO Filter验证 SSO SessionId 通过，受限资源请求放行

2．项目结构

    （1）auth2-sso-server:中央认证服务，支持集群。
    （2）auth-sso-core: Client端依赖。
    （3）auth-sso-member:基于Cookie接入方式，供用户浏览器访问，springboot版本。
    （4）auth-sso-product:基于Token接入方式，
3．项目源码

（1）Oauth2-sso-auth-server

    1）application.yml
        datasource、mvc、freemarker、redis
        Redis:
        sso.redis.address: redis://127.0.0.1:6379  #配置地址
        sso.redis.expire.minite: 1440   #配置过期时间
    2）定义spring配置类。
        该配置类实现InitializingBean和DisposableBea。
        重写afterPropertiesSet()方法，该方法主要获取ShardedJedis实例。
        重写destory()方法，该方法用于关闭ShardedJedisPool。
    3）WebController 登录、登出接口
        index()方法:根据获取系统用户是否存在检查登录状态，进行页面跳转 redirect:/login 或 index页面。
        login()方法：主要用于客户端系统登录接口用。根据获取用户是否存在检查登录状态，若存在进行重定向，不存在返回登录页。
        doLogin()方法：登录页面提交的接口。逻辑：1.检查登录用户是否存在，并做相应处理。
            2.根据验证成功的用户做Cookie保存和redis存储。3.登录成功后的成定向。
        logout()方法：删除Cookie,清理redis,成定向。
（2）Oauth2-sso-auth-core

    1）定义过滤器(ZhySsoWebFilter)，基于cookie
        a.检查排除路径，支持ant表达式
        b.是否退出
        c.获取系统用户，若为null，做重定向到中央端登录，并做好登录后的跳转参数。
        d.若成功获取系统用户，request封装用户。准备登录，chain.doFilter()。
    2）定义过滤器（ZhySsoTokenFilter）,基于token
        a.检查排除路径，支持ant表达式
        b.是否退出
        c.获取系统用户，验证登录信息。
        d.若成功获取系统用户，request封装用户。继续后续业务，chain.doFilter()。
    3）其他类
        a.删除session、logincheck验证登录、获取session。
        b.根据userid从redis获取系统用户、生成rediskey、redis存储用户信息、redis删除用户信息。
        c.通过sessionid获取用户信息、根据用户信息生成sessionid。
（3）Oauth2-sso-auth-member

    1）自定义spring配置类，实现DisposableBean。
        定义内部spring配置类FilterRegistrationBean:实例化ShardedJedis、注册自定义过滤器(ZhySsoWebFilter)。
        FilterRegistrationBean作用：注册自定义的过滤器、设置优先级、过滤请求路径、设置过滤器名称、
            初始化自定义过滤器的参数（参数获取方法：filterconfig.getInitParameter(“paramName”)）。
（4）Oauth2-sso-auth-order

    1）自定义spring 配置类，实现DisposableBean。
    定义内部spring配置类FilterRegistrationBean:实例化ShardedJedis、注册自定义过滤器（ZhySsoTokenFilter）。
四．项目地址

    https://github.com/englishand/auth-sso
六．特别鸣谢

    xuxueli