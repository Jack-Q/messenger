package cn.jackq.messager.network;

import java.nio.ByteBuffer;

public class Protocol {
    private static final String TAG = "Protocol";

    private static final byte[] HEADER = new byte[]{0x4a, 0x51, 0x49, 0x4d};
    private static final int HEADER_LENGTH = HEADER.length;
    private static final byte PACKAGE_VERSION = (byte) 0x81;

    public boolean isPartialPackage(byte[] readBuffer, int length) {
        if (length < 0 || length > readBuffer.length) return false;

        // Check header
        if (length < HEADER_LENGTH)
            return true;
        else if (!isHeaderValid(readBuffer))
            return false;

        // Check version
        if (length < VERSION_OFFSET + VERSION_LENGTH)
            return true;
        else if (getPackageVersion(readBuffer) != VERSION_LENGTH)
            return false;

        // Check type
        if (length < PACKAGE_TYPE_OFFSET + PACKAGE_TYPE_LENGTH)
            return true;
        else if (getPackageType(readBuffer) == null)
            return false;

        // Check length
        if (length < SIZE_OFFSET + SIZE_LENGTH)
            return true;
        else if (getPackageSize(readBuffer) < 0)
            return false;

        return true;
    }

    private enum PackageType {
        SERVER_CHECK(0x01);
        private final byte type;

        PackageType(int type) {
            this.type = (byte) type;
        }

        public byte getValue() {
            return this.type;
        }
    }

    private static final int VERSION_OFFSET = HEADER_LENGTH;
    private static final int VERSION_LENGTH = 1;
    private static final int PACKAGE_TYPE_OFFSET = VERSION_OFFSET + VERSION_LENGTH;
    private static final int PACKAGE_TYPE_LENGTH = 1;
    private static final int SIZE_OFFSET = PACKAGE_TYPE_OFFSET + PACKAGE_TYPE_LENGTH;
    private static final int SIZE_LENGTH = 2;
    private static final int PAYLOAD_OFFSET = SIZE_OFFSET + SIZE_LENGTH;


    public boolean isFullPackage(byte[] readBuffer) {
        return isFullPackage(readBuffer, readBuffer.length);
    }

    public boolean isFullPackage(byte[] readBuffer, int length) {
        // validate length
        if (length < readBuffer.length)
            return false;

        // minimal size
        if (length < PAYLOAD_OFFSET) return false;

        // header magic number
        if (!isHeaderValid(readBuffer)) return false;

        // version
        if (getPackageVersion(readBuffer) != PACKAGE_VERSION) return false;

        // type
        if (getPackageType(readBuffer) == null)
            return false;

        int size = getPackageSize(readBuffer);
        return length == size;
    }

    private boolean isHeaderValid(byte[] readBuffer) {
        for (int i = 0; i < HEADER.length; i++)
            if (readBuffer[i] != HEADER[i]) return false;
        return true;
    }

    private byte[] pack(PackageType type, byte[] payload) {
        short size = (short) (PAYLOAD_OFFSET + payload.length);
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.put(HEADER);
        buffer.put(PACKAGE_VERSION);
        buffer.put(type.getValue());
        buffer.putShort(size);
        buffer.put(payload);
        return buffer.array();
    }

    private byte[] pack(PackageType type, String payload) {
        return pack(type, payload.getBytes());
    }

    public String unpackString(byte[] readBuffer) {
        return new String(readBuffer, PAYLOAD_OFFSET, getPackageSize(readBuffer));
    }

    private int getPackageSize(byte[] readBuffer) {
        return ByteBuffer.wrap(readBuffer).getShort(SIZE_OFFSET);
    }

    private int getPackageVersion(byte[] readBuffer) {
        return ByteBuffer.wrap(readBuffer).get(VERSION_OFFSET);
    }

    private PackageType getPackageType(byte[] readBuffer) {
        byte typeValue = ByteBuffer.wrap(readBuffer).get(PACKAGE_TYPE_OFFSET);
        for (PackageType p : PackageType.values()) {
            if (p.getValue() == typeValue)
                return p;
        }
        return null;
    }

    public byte[] packServerTestPackage() {
        return pack(PackageType.SERVER_CHECK, "PING");
    }

}
