import opus from 'node-opus';

/**
 * As the document fo opus states that for the frame size:
 * This must be one of 8000, 12000, 16000, 24000, or 48000.
 */
export default class OpusCodec {
  constructor(sampleRate = 48000) {
    this.sampleRate = sampleRate;
    this.codec = new opus.OpusEncoder(sampleRate, 1);
  }

  encode(buffer) {
    return this.codec.encode(buffer, 480);
  }
  decode(buffer) {
    const nodeBuffer = this.codec.decode(buffer);
    // convert node buffer to Int16Array
    return new Int16Array(nodeBuffer.buffer, nodeBuffer.byteOffset, nodeBuffer.byteLength / 2);
  }
}
