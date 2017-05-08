package cn.jackq.messenger.service;

import java.util.Locale;

import cn.jackq.messenger.network.protocol.User;

/**
 * Created on: 5/5/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */


public class ChatSession {
    private String id;
    private User peer;
    private String status;
    private int timeLength;
    private boolean canAnswer;
    private boolean canEnd;

    public void reset() {
        this.id = "";
        this.peer = null;
        this.status = "";
        this.timeLength = 0;
        this.canAnswer = false;
        this.canEnd = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getPeer() {
        return peer;
    }

    public void setPeer(User peer) {
        this.peer = peer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimeString() {
        if (timeLength > 0)
            return String.format(Locale.ENGLISH, "%d:%02d", timeLength / 60, timeLength % 60);
        else
            return "";
    }

    public void setTimeLength(int time) {
        this.timeLength = time;
    }

    public boolean isCanAnswer() {
        return canAnswer;
    }

    public void setCanAnswer(boolean canAnswer) {
        this.canAnswer = canAnswer;
    }

    public boolean isCanEnd() {
        return canEnd;
    }

    public void setCanEnd(boolean canEnd) {
        this.canEnd = canEnd;
    }
}
