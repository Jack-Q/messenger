package cn.jackq.messenger.audio;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created on: 5/8/17.
 * Creator: Jack Q <qiaobo@outlook.com>
 */

public class MessengerAudioPacker {
    private static final String TAG = "MessengerAudioPacker";
    static ByteBuffer packAudioFrame(int index, ByteBuffer buffer){
        int size = 2 + buffer.limit() - buffer.position();
        byte[] buf = new byte[size];

        ByteBuffer byteBuffer = ByteBuffer.wrap(buf);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putShort((short) index);
        System.arraycopy(buffer.array(), buffer.position(), buf, 2, size - 2);
//        Log.d(TAG, "packAudioFrame: send packet " + index);
        return ByteBuffer.wrap(buf, 0, size);
    }

    static int unpackAudioFrame(ByteBuffer buffer){
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        short i = buffer.getShort();
        Log.d(TAG, "unpackAudioFrame: receive packet " + i);
        return i;
    }
}
