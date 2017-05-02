import opus from 'node-opus';


export default class OpusCodec {
  constructor(sampleRate = 44100) {
    this.sampleRate = sampleRate;
    this.codec = new opus.OpusEncoder(sampleRate);
  }

  encode(buffer) {
    return this.codec.encode(buffer);
  }
  decode(buffer) {
    return this.codec.decode(buffer);
  }
}
