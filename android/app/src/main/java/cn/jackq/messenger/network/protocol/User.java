package cn.jackq.messenger.network.protocol;

/**
 * Created on: 5/4/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */

public class User {
    private String name;
    private String connectId;
    private String ip;

    public User(String name, String connectId, String ip) {
        this.name = name;
        this.connectId = connectId;
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConnectId() {
        return connectId;
    }

    public void setConnectId(String connectId) {
        this.connectId = connectId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
