/*
 * Copyright (C) 2014 Andrew Comminos
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cn.jackq.messenger.audio;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * IEncoder provides an interface for native audio encoders to buffer and serve encoded audio
 * data.
 * Created by andrew on 07/03/14.
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
