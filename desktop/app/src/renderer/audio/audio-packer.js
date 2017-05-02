/**
 * packet format: [index][content]
 *                ^ 2 byte
 */
export default class AudioPacker {
  static pack(data) {
    const length = data.frame.length + 2;
    const buffer = Buffer.allocUnsafe(length);
    buffer.writeUInt16LE(data.index, 0);
    data.frame.copy(buffer, 2);
    return buffer;
  }
  static unpack(buffer) {
    return { index: buffer.readInt16LE(0), data: buffer.slice(2) };
  }
}
