package cn.jackq.messager.network;

/**
 * Created by jackq on 4/18/17.
 */
public class NetworkOperationStatus {
    private String message;
    private boolean ok;

    public static final NetworkOperationStatus OK = new NetworkOperationStatus("ok", true);

    public NetworkOperationStatus(String message, boolean ok) {
        this.message = message;
        this.ok = ok;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }
}
