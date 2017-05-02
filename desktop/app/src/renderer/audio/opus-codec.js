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
    return this.codec.decode(buffer);
  }
}
