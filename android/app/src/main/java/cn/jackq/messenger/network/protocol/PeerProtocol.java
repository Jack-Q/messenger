package cn.jackq.messenger.network.protocol;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.nio.ByteBuffer;

public class PeerProtocol {

    public enum PacketType {
        U_SRV_ADDR(0xa1),
        U_SYN(0x01),
        U_ACK(0x02),
        U_DAT(0x03),
        U_END(0x04);

        public final byte value;

        PacketType(int value) {
            this.value = (byte) value;
        }

        @Nullable
        public static PacketType fromByte(byte b) {
            for (PacketType p : PacketType.values()) {
                if (p.value == b) return p;
            }
            return null;
        }

    }

    public static ByteBuffer packServerAddr(String sessionId, String connectId) {
        String header = sessionId + ":" + connectId;
        return getTypeStringBuffer(PacketType.U_SRV_ADDR, header);
    }

    public static ByteBuffer packPeerData(PeerData data) {


        String header = data.getSessionId() + ':' + data.getType().value + ':';
        ByteBuffer buffer = ByteBuffer.allocate(1 + header.length() + data.getBuffer().limit());
        buffer.put(PacketType.U_DAT.value);
        byte[] headerBytes = header.getBytes();
        buffer.put(headerBytes);
        buffer.put(data.getBuffer());
        buffer.position(0);
        return buffer;
    }

    public static ByteBuffer packPeerSync(String sessionId) {
        return getTypeStringBuffer(PacketType.U_SYN, sessionId);
    }

    public static ByteBuffer packPeerAck(String sessionId) {
        return getTypeStringBuffer(PacketType.U_ACK, sessionId);
    }

    public static ByteBuffer packPeerEnd(String sessionId) {
        return getTypeStringBuffer(PacketType.U_END, sessionId);
    }

    @NonNull
    private static ByteBuffer getTypeStringBuffer(PacketType type, String sessionId) {

        ByteBuffer buffer = ByteBuffer.allocate(sessionId.length() + 1);
        buffer.put(type.value);
        buffer.put(sessionId.getBytes());
        return buffer;
    }


    public static String unpackSessionId(ByteBuffer buffer) {
        return new String(buffer.array(), buffer.arrayOffset() + 1, buffer.limit() - 1);
    }

    public static PacketType unpackPacketType(ByteBuffer buffer) {
        return PacketType.fromByte(buffer.get(0));
    }

    public static PeerData unpackPeerData(ByteBuffer buffer) {

        ByteBuffer buf = buffer.duplicate();
        int[] sep = new int[2];
        int cnt, pos;
        for (pos = 0, cnt = 0; cnt < 2 && pos < 20 && pos < buffer.limit(); pos++) {
            if (buf.get(pos) == ':') {
                sep[cnt++] = pos;
            }
        }

        if (cnt != 2) {
            return null;
        }

        String sessionId = new String(buffer.array(), buffer.arrayOffset(), sep[0] - 1);
        String typeString = new String(buffer.array(),
                buffer.arrayOffset() + sep[0] + 1, sep[1] - sep[0]);
        ByteBuffer data = ByteBuffer.wrap(buffer.array(), buffer.arrayOffset() + sep[1] + 1,
                buffer.limit() - sep[1]);

        return new PeerData(sessionId, PeerData.DataType.fromString(typeString), data);
    }
}
