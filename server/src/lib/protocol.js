export const PROTOCOL_VERSION = 0x81;
export const MAXIMUM_PACKET_SIZE = 2048;
  
export const packetType = {
  SERVER_CHECK: {type:'SERVER_CHECK', value: 0x01},
}

export const checkPacket = (buffer, low, high) => {
  let valid, complete;
  return { valid, complete };
}

export const readPacket = (buffer, low, high) => {
  let length = 0, type, payload;
  return { data: { type, payload }, length };
}

const writeBuffer = (buf, type, data) => {
  let i = 0;
  i = buf.writeUInt8(0x4a, i);
  i = buf.writeUInt8(0x51, i);
  i = buf.writeUInt8(0x49, i);
  i = buf.writeUInt8(0x4d, i);

  i = buf.writeUInt8(PROTOCOL_VERSION, i);
  i = buf.writeUInt8(type.value, i);

  let size = 0;  

  if (!(data instanceof Buffer))
    data = Buffer.from(data);
  
  size = 8 + data.length;

  i = buf.writeUInt16BE(size, i);
  data.copy(buf, i, 0, data.length);

  return size;
}

const makePacketByType = {
  SERVER_CHECK: data => "PING",
}

export const makePacketToBuffer = (buf, type, data) => {
  type = typeof type == 'string' ? type : type.type;
  return writeBuffer(buf, packetType[type], makePacketByType[type](data));
}

const protocolWriteBuffer = Buffer.alloc(MAXIMUM_PACKET_SIZE);
export const makePacket = (type, data) => {
  const size = makePacketToBuffer(protocolWriteBuffer, type, data);
  return protocolWriteBuffer.slice(0, size);
}