package com.alvin.boot.netty.pojo;

import lombok.Data;

/**
 * @Description:
 * @Author: alvin
 * @Date: 2020/6/9 17:01
 */
@Data
public class SendMessage {

    private Long id;
    private MsgType msgType;
    private String rout;
    private String from;
    private String to;
    private String data;

    public enum MsgType {
        /**
         * 发送给单个用户
         */
        TO_USER
    }
}
