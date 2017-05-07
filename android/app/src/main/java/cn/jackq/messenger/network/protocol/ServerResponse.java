package cn.jackq.messenger.network.protocol;

import java.util.ArrayList;

public class ServerResponse {
    public static final ServerResponse PARSE_ERROR = new ServerResponse(false, "invalid JSON response");

    private boolean status = false;
    private String message = "";
    private String connectId = "";
    private String sessionId = "";
    private String user = "";
    private String address = "";
    private int port = 0;
    private Object jsonPayload = "";
    private ServerProtocol.InfoType infoType = null;
    private ArrayList<User> buddyList = null;

    public ServerResponse() {
    }

    public ServerResponse(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConnectId() {
        return connectId;
    }

    public void setConnectId(String connectId) {
        this.connectId = connectId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Object getJsonPayload() {
        return jsonPayload;
    }

    public void setJsonPayload(Object jsonPayload) {
        this.jsonPayload = jsonPayload;
    }

    public ServerProtocol.InfoType getInfoType() {
        return infoType;
    }

    public void setInfoType(ServerProtocol.InfoType infoType) {
        this.infoType = infoType;
    }

    public void setBuddyList(ArrayList<User> buddyList) {
        this.buddyList = buddyList;
    }

    public ArrayList<User> getBuddyList() {
        return buddyList;
    }
}
