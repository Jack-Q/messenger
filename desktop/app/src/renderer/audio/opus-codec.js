import opus from 'node-opus';


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
