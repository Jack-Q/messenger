package cn.jackq.messenger.service;

import java.util.Locale;

import cn.jackq.messenger.network.protocol.User;

/**
 * Created on: 5/5/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */


public class ChatSession {
    enum ChatStatus {
        NULL, PREPARING, WAITING_ADDRESS, WAITING, PEER_CONNECTING, CHATTING, TERMINATING
    }

    private String id;
    private User peer;
    private ChatStatus status;
    private String statusString;
    private int timeLength;
    private boolean canAnswer;
    private boolean canEnd;
    private String peerAddress;
    private int peerPort;

    public void reset() {
        this.id = "";
        this.peer = null;
        this.statusString = "";
        this.timeLength = 0;
        this.canAnswer = false;
        this.canEnd = false;
        this.status = ChatStatus.NULL;
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

    public String getStatusString() {
        return statusString;
    }

    public void setStatusString(String statusString) {
        this.statusString = statusString;
    }

    public String getTimeString() {
        if (timeLength > 0)
            return String.format(Locale.ENGLISH, "%d:%02d", timeLength / 60, timeLength % 60);
        else
            return "";
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

    public ChatStatus getStatus() {
        return status;
    }

    public void setStatus(ChatStatus status) {
        this.status = status;
    }

    public int getTimeLength() {
        return timeLength;
    }

    public void setTimeLength(int timeLength) {
        this.timeLength = timeLength;
    }

    public String getPeerAddress() {
        return peerAddress;
    }

    public void setPeerAddress(String peerAddress) {
        this.peerAddress = peerAddress;
    }

    public int getPeerPort() {
        return peerPort;
    }

    public void setPeerPort(int peerPort) {
        this.peerPort = peerPort;
    }
}
