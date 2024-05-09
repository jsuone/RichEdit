package com.example.android_note.webSocket;

import com.example.android_note.model.HttpResult;
import com.example.android_note.model.ResponseConstant;
import com.example.android_note.util.JWTUtil;
import lombok.NonNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class WebSocketInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) request;
            // 模拟用户（通常利用JWT令牌解析用户信息）
            String token = servletServerHttpRequest.getServletRequest().getHeader("Authorization");
            //token为空 禁止连接
            if(token==null||token.isEmpty()){
                throw new RuntimeException("token不能为空");
            }


            //token内容验证 token是否在有效期是否正确 将token传过去 不验证用户是否已经登录 因为抢占登录需要给用户提醒返回消息，所以这里不验证
            //String token_jwt = token.substring(7);
            try {
                String userName = JWTUtil.parseJWT(token, "userName");
                if (StringUtils.hasText(userName)) {
                    attributes.put("token",token);
                    return true;
                }else {
                   return false;
                }
            }catch (Exception e){
               throw new RuntimeException("token已经过期或者非法");
            }

        }
        return false;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response, @NonNull WebSocketHandler wsHandler, Exception exception) {

    }
}
