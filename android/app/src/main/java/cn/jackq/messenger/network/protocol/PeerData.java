package cn.jackq.messenger.network.protocol;

import android.support.annotation.Nullable;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Created on: 4/24/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */

public class PeerData {
    public enum DataType {
        AUDIO("audio");

        public final String value;

        DataType(String value) {
            this.value = value;
        }

        @Nullable
        static DataType fromString(String str) {
            for (DataType v : DataType.values()) {
                if (Objects.equals(v.value, str)) {
                    return v;
                }
            }
            return null;
        }
    }

    private String sessionId;
    private DataType type;
    private ByteBuffer buffer;

    public PeerData(String sessionId, DataType type, ByteBuffer buffer) {
        this.sessionId = sessionId;
        this.type = type;
        this.buffer = buffer;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public ByteBuffer getBuffer() {
        return buffer.duplicate();
    }

    public ByteBuffer getOriginalBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer.duplicate();
    }

    public ByteBuffer setOriginalBuffer() {
        return buffer;
    }
}
