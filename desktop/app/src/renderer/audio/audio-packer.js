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
    console.log(`pack ${data.index}`);
    return buffer;
  }
  static unpack(buffer) {
    console.log(`pack ${buffer.readUInt16LE(0)}`);
    return { index: buffer.readUInt16LE(0), frame: buffer.slice(2) };
  }
}
