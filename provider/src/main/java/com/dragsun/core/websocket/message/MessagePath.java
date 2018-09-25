package com.dragsun.core.websocket.message;

/**
 * @param
 * @Author: zhuangjiesen
 * @Description:
 *
 * 消息路径枚举
 * @Date: Created in 2018/9/25
 */
public enum MessagePath {

    SUBSCRIBE("subscribe"),
    UNSUBSCRIBE("unsubscribe"),

    ;
    private String path;

    MessagePath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
