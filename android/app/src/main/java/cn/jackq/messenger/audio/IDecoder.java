package cn.jackq.messenger.audio;

import java.nio.ByteBuffer;

public interface IDecoder {
    /**
     * Decodes the encoded data provided into float PCM data.
     * @param input A byte array of encoded data of size inputSize.
     * @param inputSize The size of the encoded data.
     * @param output An initialized output array at least frameSize for float PCM data.
     * @param frameSize The maximum frame size possible.
     * @return The number of decoded samples.
     * @throws NativeAudioException if encoding failed.
     */
    int decodeFloat(ByteBuffer input, int inputSize, float[] output, int frameSize) throws NativeAudioException;

    /**
     * Decodes the encoded data provided into short PCM data.
     * @param input A byte array of encoded data of size inputSize.
     * @param inputSize The size of the encoded data.
     * @param output An initialized output array at least frameSize for short PCM data.
     * @param frameSize The maximum frame size possible.
     * @return The number of decoded samples.
     * @throws NativeAudioException if encoding failed.
     */
    int decodeShort(ByteBuffer input, int inputSize, short[] output, int frameSize) throws NativeAudioException;

    /**
     * De-allocates native resources. The decoder must no longer be called after this.
     */
    void destroy();
}
