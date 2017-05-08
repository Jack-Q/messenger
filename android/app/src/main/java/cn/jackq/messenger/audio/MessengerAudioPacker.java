package cn.jackq.messenger.audio;

import java.nio.ByteBuffer;

/**
 * Created on: 5/8/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */

public class MessengerAudioPacker {
    static ByteBuffer packAudioFrame(int index, ByteBuffer buffer){
        int size = 2 + buffer.limit() - buffer.position();
        byte[] buf = new byte[size];
        ByteBuffer.wrap(buf).putShort((short) index);
        System.arraycopy(buffer.array(), buffer.position(), buf, 2, size - 2);
        return ByteBuffer.wrap(buf, 0, size);
    }

    static int unpackAudioFrame(ByteBuffer buffer){
        return buffer.getShort();
    }
}
