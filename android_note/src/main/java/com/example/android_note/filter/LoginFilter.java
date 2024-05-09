package com.example.android_note.filter;


import com.example.android_note.model.HttpResult;
import com.example.android_note.util.JWTUtil;

import com.google.gson.Gson;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * @className: LoginFilter
 * @description: TODO 类描述
 * @date: 2023/5/120:43
 **/
@Order(Ordered.LOWEST_PRECEDENCE)
@WebFilter(urlPatterns = "/*")
public class LoginFilter implements Filter {

    Gson gson = new Gson();
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
        System.out.println("登录过滤器创建");
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
        System.out.println("登录过滤器摧毁");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("登录过滤器执行");
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String uri = httpRequest.getRequestURI();
        System.out.println("目前请求的url"+uri);
        //uri = uri.substring(uri.lastIndexOf("/")+1);
        String[] url = uri.split("/");
        //登录链接直接放行
        if("user".equals(url[1])||"res".equals(url[1])){
            chain.doFilter(request,response);
            return;
        }
        //进行token验证
        String token = httpRequest.getHeader("Authorization");
        System.out.println("token "+token);
        //token为空
        if(token==null||token.isEmpty()){
            httpResponse.getWriter().write (gson.toJson(new HttpResult<>(404,"not login now","")));
            return;
        }
        //token内容验证
        String token_jwt = token.substring(7);
            try {
                String userName = JWTUtil.parseJWT(token_jwt, "userName");
                if (StringUtils.hasText(userName)) {
                    chain.doFilter(request,response);
                }else {
                    httpResponse.getWriter().write ("{\"code\":404,\"msg\":\"not login now\",\"data\":\"\"}");
                }
            }catch (Exception e){
                httpResponse.getWriter().write ("{\"code\":404,\"msg\":\"token invalid\",\"data\":\"\"}");
            }
    }
}
