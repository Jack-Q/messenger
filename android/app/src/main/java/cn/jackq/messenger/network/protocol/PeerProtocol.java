package cn.jackq.messenger.network.protocol;

import android.support.annotation.NonNull;

import java.nio.ByteBuffer;

/**
 * Created on: 4/24/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */

public class PeerProtocol {

    public enum PackageType {
        U_SRV_ADDR(0xa1),
        U_SYN(0x01),
        U_ACK(0x02),
        U_DAT(0x03),
        U_END(0x04);

        public final byte value;

        PackageType(int value) {
            this.value = (byte) value;
        }
    }

    public enum DataType {
        AUDIO("audio");

        public final String value;

        DataType(String value) {
            this.value = value;
        }
    }

    public ByteBuffer packServerAddr(String sessionId, String connectId) {
        String header = sessionId + ":" + connectId;
        return getTypeStringBuffer(PackageType.U_SRV_ADDR, header);
    }

    public ByteBuffer packPeerData(String sessionId, DataType dataType, ByteBuffer data) {
        String header = sessionId + ':' + dataType.value + ':';
        ByteBuffer buffer = ByteBuffer.allocate(1 + header.length() + data.limit());
        buffer.put(PackageType.U_DAT.value);
        byte[] headerBytes = header.getBytes();
        buffer.put(headerBytes, 1, headerBytes.length);
        buffer.put(data);
        return buffer;
    }

    public ByteBuffer packPeerSync(String sessionId) {
        return getTypeStringBuffer(PackageType.U_SYN, sessionId);
    }

    public ByteBuffer packPeerAck(String sessionId) {
        return getTypeStringBuffer(PackageType.U_ACK, sessionId);
    }

    public ByteBuffer packPeerEnd(String sessionId) {
        return getTypeStringBuffer(PackageType.U_END, sessionId);
    }

    @NonNull
    private ByteBuffer getTypeStringBuffer(PackageType type, String sessionId) {
        ByteBuffer buffer = ByteBuffer.allocate(sessionId.length() + 1);
        buffer.put(type.value);
        buffer.put(sessionId.getBytes());
        return buffer;
    }

}
