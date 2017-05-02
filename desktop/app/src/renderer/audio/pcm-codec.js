export default class PcmCodec {
  static encode(float32Array) {
    return Int16Array.from(float32Array);
  }

  static decode(int16Array) {
    return Float32Array.from(int16Array);
  }
}
