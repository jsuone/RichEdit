package com.example.android_note.webSocket;

import com.example.android_note.util.DateTime;
import com.example.android_note.util.JWTUtil;
import com.example.android_note.util.RedisUtil;
import com.example.android_note.util.SyncScheduleTask;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import javax.websocket.Session;
import java.io.IOException;
import java.nio.ByteBuffer;
@Slf4j
public class DefaultWebSocketHandler  implements WebSocketHandler {


    @Autowired
    private WebSocket webSocket;

    @Autowired
    private RedisUtil redisUtil;
    /**
     * 建立连接
     *
     * @param session Session
     */
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws IOException {

        //将登陆成功的用户存入redis缓存中 key login+用户名 value token map集合 一个token对应一个链接 key token value session
        // 如果redis中有说明已经登录，对应的token也一样就加入map集合 不一样就是抢占登录 客户端已经确定过了 没有说明未登录 每次链接断开移除对应session
        String token = (String) session.getAttributes().get("token");
        String userName = JWTUtil.parseJWT(token, "userName");//经过前面的验证 token必是合法有效的
        String redisToken = (String) redisUtil.get("login_"+userName);

        if (redisToken != null && redisToken.equals(token)) {
            //用户已经登录
            //用户登录成功，放入在线用户缓存
            webSocket.handleOpen(token, session);
            System.out.println("afterConnectionEstablished"+"恭喜"+userName+"登录成功");
        } else if (redisToken != null && !redisToken.equals(token.toString())) {
            //用户已经登录，但是发生冲突 抢占登录
            redisUtil.set("login_"+userName,token);
            //如果有其他设备正在回话，关闭
            webSocket.sendMessage(redisToken, "500");
            webSocket.handleClose(redisToken);
            webSocket.handleOpen(token, session);
        } else if (redisToken == null) {
            //用户未登录
            webSocket.handleOpen(token,session);
            System.out.println("afterConnectionEstablished"+"恭喜"+userName+"登录成功");
        }
        session.setBinaryMessageSizeLimit(52428800);//不设置的话传输图片会由于数据过大管道破裂   不设置长一点的心跳时间，也会由于处理图片数据时间超时报错
        session.setTextMessageSizeLimit(52428800);

    }

    /**
     * 接收消息
     *
     * @param session Session
     * @param message 消息
     */
    @Override
    public void handleMessage(@NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message) throws IOException, InterruptedException {

        if (message instanceof TextMessage) {
            //Thread.sleep(Thread.currentThread().getId()*3);
            log.info("session的对象"+session.getId());
            TextMessage textMessage = (TextMessage) message;
            webSocket.handleMessage(session, textMessage.getPayload());
        }else if(message instanceof BinaryMessage){
            BinaryMessage binaryMessage = (BinaryMessage) message;
            webSocket.handleMessage(session,binaryMessage.getPayload());
        }else if(message instanceof PongMessage){
            SyncScheduleTask.connectionStatus.put(session.getId(), DateTime.getCurrentTime());
            System.out.println("接受pong消息");
        }
    }


    /**
     * 发生错误
     *
     * @param session   Session
     * @param exception 异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        webSocket.handleError(session, exception);
    }

    /**
     * 关闭连接
     *
     * @param session     Session
     * @param closeStatus 关闭状态
     */
    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) {
        Object token = session.getAttributes().get("token");
        System.out.println("链接关闭后");
        if (token != null) {
            // 用户退出，移除缓存
            webSocket.handleClose(token.toString());
        }
    }

    /**
     * 是否支持发送部分消息
     *
     * @return false
     */
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }


}
