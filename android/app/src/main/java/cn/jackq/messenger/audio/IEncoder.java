package cn.jackq.messenger.audio;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * IEncoder provides an interface for native audio encoders to buffer and serve encoded audio
 * data.
 */
public interface IEncoder {
    /**
     * Encodes the provided input and returns the number of bytes encoded.
     * @param input The short PCM data to encode.
     * @param inputSize The number of samples to encode.
     * @return The number of bytes encoded.
     * @throws NativeAudioException if there was an error encoding.
     */
    int encode(short[] input, int inputSize) throws NativeAudioException;

    /**
     * @return the number of audio frames buffered.
     */
    int getBufferedFrames();

    /**
     * @return true if enough buffered audio has been encoded to send to the server.
     */
    boolean isReady();

    /**
     * Writes the currently encoded audio data into the provided {@link ByteBuffer}.
     * Use {@link #isReady()} to determine whether or not this should be called.
     * @throws BufferUnderflowException if insufficient audio data has been buffered.
     * @param packetBuffer
     */
    int getEncodedData(byte[] packetBuffer) throws BufferUnderflowException;

    /**
     * Informs the encoder that there are no more audio packets to be queued. Often, this will
     * trigger an encode operation, changing the result of {@link #isReady()}.
     */
    void terminate() throws NativeAudioException;

    /**
     * Destroys the encoder, cleaning up natively allocated resources.
     */
    void destroy();
}
