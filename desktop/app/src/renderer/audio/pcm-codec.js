const scaler = 2 ** 15;
const amplifier = 1;
const saturate = v => Math.min(1, Math.max(-1, v));

export default class PcmCodec {
  static encode(float32Array) {
    return Int16Array.from(float32Array.map(f => saturate(f * amplifier) * scaler));
  }

  static decode(int16Array) {
    return Float32Array.from(int16Array).map(f => saturate(f / amplifier));
  }
}
