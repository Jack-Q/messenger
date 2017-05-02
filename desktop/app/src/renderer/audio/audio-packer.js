export default class AudioPacker {
  static pack(data) {
    return Buffer.from(data);
  }
  static unpack(buffer) {
    return { data: buffer };
  }
}
