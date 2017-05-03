package cn.jackq.messenger.network.protocol;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;

public class ServerProtocol {
    private static final String TAG = "ServerProtocol";

    private static final byte[] HEADER = new byte[]{0x4a, 0x51, 0x49, 0x4d};
    private static final int HEADER_LENGTH = HEADER.length;
    private static final byte PACKET_VERSION = (byte) 0x81;

    public boolean isPartialPacket(byte[] readBuffer, int length) {
        if (length < 0 || length > readBuffer.length) return false;

        // Check header
        if (length < HEADER_LENGTH)
            return true;
        else if (!isHeaderValid(readBuffer))
            return false;

        // Check version
        if (length < VERSION_OFFSET + VERSION_LENGTH)
            return true;
        else if (getPacketVersion(readBuffer) != VERSION_LENGTH)
            return false;

        // Check type
        if (length < PACKET_TYPE_OFFSET + PACKET_TYPE_LENGTH)
            return true;
        else if (getPacketType(readBuffer) == null)
            return false;

        // Check length
        if (length < SIZE_OFFSET + SIZE_LENGTH)
            return true;
        else if (getPacketSize(readBuffer) < 0)
            return false;

        return true;
    }

    public enum PacketType {
        SERVER_CHECK(0x01),
        SERVER_STATUS(0x02),
        USER_ADD_REQ(0x03),
        USER_ADD_RESP(0x04),
        USER_LOGIN_REQ(0x05),
        USER_LOGIN_RESP(0x06),
        INFO_QUERY(0x07),
        INFO_RESP(0x08),
        MSG_SEND(0x09),
        MSG_RECV(0x0a),
        CALL_REQ(0x11),
        CALL_INIT(0x12),
        CALL_ADDR(0x13),
        CALL_PREP(0x14),
        CALL_ANS(0x15),
        CALL_CONN(0x16),
        CALL_TERM(0x17),
        CALL_END(0x18),;
        private final byte type;

        PacketType(int type) {
            this.type = (byte) type;
        }

        public byte getValue() {
            return this.type;
        }
    }

    public enum InfoType {
        BUDDY_LIST("buddy-list");
        private String typeValue;

        InfoType(String typeValue) {
            this.typeValue = typeValue;
        }

        public String getTypeValue() {
            return typeValue;
        }
    }

    private static final int VERSION_OFFSET = HEADER_LENGTH;
    private static final int VERSION_LENGTH = 1;
    private static final int PACKET_TYPE_OFFSET = VERSION_OFFSET + VERSION_LENGTH;
    private static final int PACKET_TYPE_LENGTH = 1;
    private static final int SIZE_OFFSET = PACKET_TYPE_OFFSET + PACKET_TYPE_LENGTH;
    private static final int SIZE_LENGTH = 2;
    private static final int PAYLOAD_OFFSET = SIZE_OFFSET + SIZE_LENGTH;


    public boolean isFullPacket(byte[] readBuffer) {
        return isFullPacket(readBuffer, readBuffer.length);
    }

    public boolean isFullPacket(byte[] readBuffer, int length) {
        // validate length
        if (length < readBuffer.length)
            return false;

        // minimal size
        if (length < PAYLOAD_OFFSET) return false;

        // header magic number
        if (!isHeaderValid(readBuffer)) return false;

        // version
        if (getPacketVersion(readBuffer) != PACKET_VERSION) return false;

        // type
        if (getPacketType(readBuffer) == null)
            return false;

        int size = getPacketSize(readBuffer);
        return length == size;
    }

    private boolean isHeaderValid(byte[] readBuffer) {
        for (int i = 0; i < HEADER.length; i++)
            if (readBuffer[i] != HEADER[i]) return false;
        return true;
    }

    private byte[] pack(PacketType type, byte[] payload) {
        short size = (short) (PAYLOAD_OFFSET + payload.length);
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.put(HEADER);
        buffer.put(PACKET_VERSION);
        buffer.put(type.getValue());
        buffer.putShort(size);
        buffer.put(payload);
        return buffer.array();
    }

    private byte[] pack(PacketType type, String payload) {
        return pack(type, payload.getBytes());
    }

    public String unpackString(byte[] readBuffer) {
        return new String(readBuffer, PAYLOAD_OFFSET, getPacketSize(readBuffer));
    }

    private int getPacketSize(byte[] readBuffer) {
        return ByteBuffer.wrap(readBuffer).getShort(SIZE_OFFSET);
    }

    private int getPacketVersion(byte[] readBuffer) {
        return ByteBuffer.wrap(readBuffer).get(VERSION_OFFSET);
    }

    private PacketType getPacketType(byte[] readBuffer) {
        byte typeValue = ByteBuffer.wrap(readBuffer).get(PACKET_TYPE_OFFSET);
        for (PacketType p : PacketType.values()) {
            if (p.getValue() == typeValue)
                return p;
        }
        return null;
    }

    //region Public Packet Enclosure
    public byte[] packServerTestPacket() {
        return pack(PacketType.SERVER_CHECK, "PING");
    }

    public byte[] packUserAddReqPacket(String name, String token) {
        JSONObject object = new JSONObject();
        try {
            object.put("n", name);
            object.put("t", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this.pack(PacketType.USER_ADD_REQ, object.toString());
    }

    public byte[] packLoginReqPacket(String name, String token) {
        JSONObject object = new JSONObject();
        try {
            object.put("n", name);
            object.put("t", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this.pack(PacketType.USER_LOGIN_REQ, object.toString());
    }

    public byte[] packInfoQueryPacket(InfoType type, String param) {
        JSONObject object = new JSONObject();
        try {
            object.put("q", type.getTypeValue());
            object.put("p", param);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this.pack(PacketType.INFO_QUERY, object.toString());
    }

    public byte[] packBuddyListQueryPacket() {
        return this.packInfoQueryPacket(InfoType.BUDDY_LIST, "");
    }

    public byte[] packMsgSendPacket(User user, String message) {
        JSONObject object = new JSONObject();
        try {
            object.put("u", user.getName());
            object.put("c", user.getConnectId());
            object.put("m", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this.pack(PacketType.MSG_SEND, object.toString());
    }

    public byte[] packCallReqPacket(User user, String connectioId) {
        JSONObject object = new JSONObject();
        try {
            object.put("u", user.getName());
            object.put("c", user.getConnectId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this.pack(PacketType.CALL_REQ, object.toString());
    }

    public byte[] packCallPrepPacket(String sessionId) {
        JSONObject object = new JSONObject();
        try {
            object.put("i", sessionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this.pack(PacketType.CALL_PREP, object.toString());
    }

    public byte[] packCallAnsPacket(String sessionId) {
        JSONObject object = new JSONObject();
        try {
            object.put("i", sessionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this.pack(PacketType.CALL_PREP, object.toString());
    }

    public byte[] packCallTremPacket(String sessionId) {
        JSONObject object = new JSONObject();
        try {
            object.put("i", sessionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return this.pack(PacketType.CALL_PREP, object.toString());
    }
    //endregion
}
