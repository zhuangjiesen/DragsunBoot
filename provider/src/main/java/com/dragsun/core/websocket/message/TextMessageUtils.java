package com.dragsun.core.websocket.message;

/**
 * @param
 * @Author: zhuangjiesen
 * @Description:
 * @Date: Created in 2018/9/25
 */
public class TextMessageUtils {


    public static String getTextHeader(MessagePath path,String requestId) {
        StringBuilder audioReqHeader = new StringBuilder();
        audioReqHeader.append(MessageConstants.PATH);
        audioReqHeader.append(":");
        audioReqHeader.append(path.getPath());
        audioReqHeader.append(MessageConstants.EMTYP_LINE);
        audioReqHeader.append("x-requestid: ");
        audioReqHeader.append(requestId);
        audioReqHeader.append(MessageConstants.EMTYP_LINE);
        audioReqHeader.append(MessageConstants.EMTYP_LINE);
        return audioReqHeader.toString();
    }



}
