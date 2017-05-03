import opus from 'node-opus';

/**
 * As the document fo opus states that for the frame size:
 * This must be one of 8000, 12000, 16000, 24000, or 48000.
 */
export default class OpusCodec {
  constructor(sampleRate = 48000) {
    this.directMode = true;
    this.sampleRate = sampleRate;
    this.codec = new opus.OpusEncoder(sampleRate, 1);
  }

  encode(buffer) {
    if (this.directMode) {
      return OpusCodec.directEncode(buffer);
    }
    return this.codec.encode(buffer, 2480);
  }
  decode(buffer) {
    if (this.directMode) {
      return OpusCodec.directDecode(buffer);
    }
    const nodeBuffer = this.codec.decode(buffer);
    // convert node buffer to Int16Array
    return new Int16Array(nodeBuffer.buffer, nodeBuffer.byteOffset, nodeBuffer.byteLength / 2);
  }

  static directEncode(buffer) {
    this.sampleRate++;
    return new Buffer(buffer.buffer)
      .slice(buffer.byteOffset, buffer.byteOffset + buffer.byteLength);
  }

  static directDecode(buffer) {
    console.log(buffer.length);
    if (buffer.byteOffset % 2 !== 0) {
      buffer = new Buffer(buffer.buffer)
        .slice(buffer.byteOffset - 1, buffer.byteOffset + buffer.byteLength);
      buffer.copy(buffer, 0, 1);
      buffer = buffer.slice(0, buffer.length - 1);
    }
    return new Int16Array(buffer.buffer, buffer.byteOffset, buffer.byteLength / 2);
  }
}
