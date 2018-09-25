package com.dragsun.core.websocket.message;

import lombok.Data;

import java.util.Map;

/**
 * @param
 * @Author: zhuangjiesen
 * @Description:
 * @Date: Created in 2018/9/25
 */
@Data
public class WSMessage<T> {

    private Map<String , String> headers;

    private T body;


}
