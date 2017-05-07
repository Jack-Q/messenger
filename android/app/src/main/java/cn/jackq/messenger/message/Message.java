package cn.jackq.messenger.message;

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Created on: 5/7/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */

public class Message{
    public enum MessageType {
        SEND, RECEIVE, SYSTEM
    }
    private String id;
    private Date date;
    private String content;
    private MessageType type;

    private Message(String content, MessageType type) {
        this.date = new Date();
        this.id = String.valueOf(date.getTime());
        this.content = content;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }

    public MessageType getType() {
        return type;
    }

    @NonNull
    public static Message createSend(String content){
        return new Message(content, MessageType.SEND);
    }
    @NonNull
    public static Message createReceive(String content){
        return new Message(content, MessageType.RECEIVE);
    }
    @NonNull
    public static Message createSystem(String content){
        return new Message(content, MessageType.SYSTEM);
    }
}
