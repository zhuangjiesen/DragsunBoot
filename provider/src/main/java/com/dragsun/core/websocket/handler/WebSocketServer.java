package com.dragsun.core.websocket.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @param
 * @Author: zhuangjiesen
 * @Description:
 * @Date: Created in 2018/9/25
 */

@Slf4j
@ServerEndpoint(value = "/websocket")
@Component
public class WebSocketServer {
    private static ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();


    private String sessionId;
    private Session wsSession;

    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session) {
        this.sessionId = session.getId();
        this.wsSession = session;
        sessionMap.put(this.sessionId , this.wsSession);
        log.debug(String.format("有新连接加入！当前在线人数为 : %d ", sessionMap.size()));
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        sessionMap.remove(this.sessionId);
        log.debug(String.format("有一连接关闭！当前在线人数为 : %d ", sessionMap.size()));
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String message, Session session) {
        log.debug(String.format("来自客户端的消息: %s " , message));


        String retMessage = "服务端返回消息_" + System.currentTimeMillis() + "_" + message;
        try {
            session.getBasicRemote().sendText(retMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

     @OnError
     public void onError(Session session, Throwable error) {
        log.debug("发生错误");
        log.error(error.getMessage() , error);
     }



}
