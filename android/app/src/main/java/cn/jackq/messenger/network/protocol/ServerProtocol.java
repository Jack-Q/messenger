package cn.jackq.messenger.network.protocol;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ServerProtocol {

    private static final String TAG = "ServerProtocol";
    private static final byte[] HEADER = new byte[]{0x4a, 0x51, 0x49, 0x4d};
    private static final int HEADER_LENGTH = HEADER.length;

    private static final byte PACKET_VERSION = (byte) 0x81;

    public static boolean isPartialPacket(byte[] readBuffer, int offset, int length) {
        if (length < 0 || length > readBuffer.length) return false;

        // Check header
        if (length < HEADER_LENGTH)
            return true;
        else if (!isHeaderValid(readBuffer, offset))
            return false;

        // Check version
        if (length < VERSION_OFFSET + VERSION_LENGTH)
            return true;
        else if (getPacketVersion(readBuffer, offset) != PACKET_VERSION)
            return false;

        // Check type
        if (length < PACKET_TYPE_OFFSET + PACKET_TYPE_LENGTH)
            return true;
        else if (getPacketType(readBuffer, offset) == null)
            return false;

        // Check length
        if (length < SIZE_OFFSET + SIZE_LENGTH)
            return true;
        else if (getPacketSize(readBuffer, offset) < 0)
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
        CALL_END(0x18);
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

        @Nullable
        static InfoType fromString(String value) {
            for (InfoType i : InfoType.values()) {
                if (i.typeValue.equals(value))
                    return i;
            }
            return null;
        }
    }

    private static final int VERSION_OFFSET = HEADER_LENGTH;

    private static final int VERSION_LENGTH = 1;
    private static final int PACKET_TYPE_OFFSET = VERSION_OFFSET + VERSION_LENGTH;
    private static final int PACKET_TYPE_LENGTH = 1;
    private static final int SIZE_OFFSET = PACKET_TYPE_OFFSET + PACKET_TYPE_LENGTH;
    private static final int SIZE_LENGTH = 2;
    private static final int PAYLOAD_OFFSET = SIZE_OFFSET + SIZE_LENGTH;

    public static boolean isFullPacket(byte[] readBuffer) {
        return isFullPacket(readBuffer, 0, readBuffer.length);
    }


    public static boolean isFullPacket(byte[] readBuffer, int offset, int length) {
        // validate length
        if (length > readBuffer.length - offset)
            return false;

        // minimal size
        if (length < PAYLOAD_OFFSET) return false;

        // header magic number
        if (!isHeaderValid(readBuffer, offset)) return false;

        // version
        if (getPacketVersion(readBuffer, offset) != PACKET_VERSION) return false;

        // type
        if (getPacketType(readBuffer, offset) == null)
            return false;

        int size = getPacketSize(readBuffer, offset);
        return length == size;
    }

    private static boolean isHeaderValid(byte[] readBuffer, int offset) {
        for (int i = 0; i < HEADER.length; i++)
            if (readBuffer[i + offset] != HEADER[i]) return false;
        return true;
    }

    @NonNull
    private static byte[] pack(PacketType type, byte[] payload) {
        short size = (short) (PAYLOAD_OFFSET + payload.length);
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.put(HEADER);
        buffer.put(PACKET_VERSION);
        buffer.put(type.getValue());
        buffer.putShort(size);
        buffer.put(payload);
        return buffer.array();
    }

    @NonNull
    private static byte[] pack(PacketType type, String payload) {
        return pack(type, payload.getBytes());
    }

    @NonNull
    public static String unpackString(byte[] readBuffer) {
        return unpackString(readBuffer, 0);
    }

    public static String unpackString(byte[] readBuffer, int posLow) {
        return new String(readBuffer, posLow + PAYLOAD_OFFSET, getPacketSize(readBuffer, posLow) - PAYLOAD_OFFSET);
    }

    public static ServerResponse unpackJsonResponse(byte[] readBuffer, int offset) {
        String payload = unpackString(readBuffer, offset);
        try {
            JSONObject object = new JSONObject(payload);
            ServerResponse resp = new ServerResponse();

            // Status
            if (object.has("s")) {
                resp.setStatus(object.getBoolean("s"));
            }

            // Message
            if (object.has("m")) {
                resp.setMessage(object.getString("m"));
            }

            // connectId (k)
            if (object.has("k")) {
                resp.setConnectId(object.getString("k"));
            }
            if (object.has("c")) {
                resp.setConnectId(object.getString("c"));
            }

            // Info Response Type
            if (object.has("t")) {
                resp.setInfoType(InfoType.fromString(object.getString("t")));
            }

            // Info Response Content
            if (resp.getInfoType() != null && object.has("p")) {
                resp.setJsonPayload(object.get("p"));
                if (resp.getInfoType() == InfoType.BUDDY_LIST) {
                    JSONArray p = object.getJSONArray("p");
                    ArrayList<User> users = new ArrayList<>();
                    for(int i = 0; i < p.length(); i++){
                        JSONObject o = p.getJSONObject(i);
                        users.add(new User(o.getString("name"), o.getString("id"), o.getString("ip")));
                    }
                    resp.setBuddyList(users);
                }
            }

            // User (Peer[u], Current User[c])
            if(object.has("u")){
                resp.setUser(object.getString("u"));
            }
            if(object.has("f")){
                resp.setUser(object.getString("f"));
            }

            // Session ID (for call session)
            if(object.has("i")){
                resp.setSessionId(object.getString("i"));
            }

            // Address
            if(object.has("a")){
                resp.setAddress(object.getString("a"));
            }

            // Port
            if(resp.getAddress().length() > 0 && object.has("p")){
                resp.setPort(object.getInt("p"));
            }

            return resp;
        } catch (JSONException e) {
            e.printStackTrace();
            return ServerResponse.PARSE_ERROR;
        }
    }


    public static int getPacketSize(byte[] readBuffer) {
        return getPacketSize(readBuffer, 0);
    }

    public static int getPacketSize(byte[] readBuffer, int offset) {
        return ByteBuffer.wrap(readBuffer).getShort(offset + SIZE_OFFSET);
    }

    public static int getPacketVersion(byte[] readBuffer, int offset) {
        return ByteBuffer.wrap(readBuffer).get(offset + VERSION_OFFSET);
    }

    public static PacketType getPacketType(byte[] readBuffer, int offset) {
        byte typeValue = ByteBuffer.wrap(readBuffer).get(PACKET_TYPE_OFFSET + offset);
        for (PacketType p : PacketType.values()) {
            if (p.getValue() == typeValue)
                return p;
        }
        return null;
    }

    //region Public Packet Enclosure
    @NonNull
    public static byte[] packServerTestPacket() {
        return pack(PacketType.SERVER_CHECK, "PING");
    }

    @NonNull
    public static byte[] packUserAddReqPacket(String name, String token) {
        JSONObject object = new JSONObject();
        try {
            object.put("n", name);
            object.put("t", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return pack(PacketType.USER_ADD_REQ, object.toString());
    }

    @NonNull
    public static byte[] packLoginReqPacket(String name, String token) {
        JSONObject object = new JSONObject();
        try {
            object.put("n", name);
            object.put("t", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return pack(PacketType.USER_LOGIN_REQ, object.toString());
    }

    @NonNull
    public static byte[] packInfoQueryPacket(InfoType type, String param) {
        JSONObject object = new JSONObject();
        try {
            object.put("q", type.getTypeValue());
            object.put("p", param);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return pack(PacketType.INFO_QUERY, object.toString());
    }

    @NonNull
    public static byte[] packBuddyListQueryPacket() {
        return packInfoQueryPacket(InfoType.BUDDY_LIST, "");
    }

    @NonNull
    public static byte[] packMsgSendPacket(User user, String message) {
        JSONObject object = new JSONObject();
        try {
            object.put("u", user.getName());
            object.put("c", user.getConnectId());
            object.put("m", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return pack(PacketType.MSG_SEND, object.toString());
    }

    @NonNull
    public static byte[] packCallReqPacket(User user, String connectId) {
        JSONObject object = new JSONObject();
        try {
            object.put("u", user.getName());
            object.put("c", connectId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return pack(PacketType.CALL_REQ, object.toString());
    }

    @NonNull
    public static byte[] packCallPrepPacket(String sessionId) {
        JSONObject object = new JSONObject();
        try {
            object.put("i", sessionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return pack(PacketType.CALL_PREP, object.toString());
    }

    @NonNull
    public static byte[] packCallAnsPacket(String sessionId) {
        JSONObject object = new JSONObject();
        try {
            object.put("i", sessionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return pack(PacketType.CALL_ANS, object.toString());
    }

    @NonNull
    public static byte[] packCallTermPacket(String sessionId) {
        JSONObject object = new JSONObject();
        try {
            object.put("i", sessionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return pack(PacketType.CALL_TERM, object.toString());
    }
    //endregion
}
