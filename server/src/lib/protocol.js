export const PROTOCOL_VERSION = 0x81;
export const MAXIMUM_PACKET_SIZE = 2048;
  
export const packetType = {
  // Server check
  SERVER_CHECK: { type: 'SERVER_CHECK', value: 0x01 },
  SERVER_STATUS: { type: 'SERVER_STATUS', value: 0x02 },
  // User Create
  USER_ADD_REQ: { type: 'USER_ADD_REQ', value: 0x03 },
  USER_ADD_RESP: { type: 'USER_ADD_RESP', value: 0x04 },
  // User Login
  USER_LOGIN_REQ: { type: 'USER_LOGIN_REQ', value: 0x05 },
  USER_LOGIN_RESP: { type: 'USER_LOGIN_RESP', value: 0x06 },
  // Information Query: query online users, current IPs, etc
  INFO_QUERY: { type: 'INFO_QUERY', value: 0x07 },
  INFO_RESP: { type: 'INFO_RESP', value: 0x08 },
  // Plain Text Conversation
  MSG_SEND: { type: 'MSG_SEND', value: 0x09 },
  MSG_RECV: { type: 'MSG_RECV', value: 0x0a },
  // Call Management
  CALL_INIT: { type: 'CALL_INIT', value: 0x11 },
  CALL_WAIT: { type: 'CALL_WAIT', value: 0x12 },
  CALL_CONN: {type: 'CALL_CONN', value: 0x13},
  CALL_END: {type: 'CALL_END', value: 0x14},
}
export const checkPacket = (buf, low, high) => {
  let valid, complete, i = low, len = high - low;
  const inRange = i => { if (i < low || i - low > len) throw 'OUT_OF_BOUND'; }
  const eq = (i, p) => { inRange(i); if (!p(i)) throw 'INVALID';}
  const eqInt = (i, b) => eq(i, v => buf.readUInt8(v) === b);
  try {
    // header
    eqInt(i++, 0x4a);
    eqInt(i++, 0x51);
    eqInt(i++, 0x49);
    eqInt(i++, 0x4d);
    // protocol version
    eqInt(i++, PROTOCOL_VERSION);
    // package type
    eq(i++, v => {
      const val = buf.readUInt8(v);
      return Object.keys(packetType).some(k => packetType[k].value == val);
    })
    // length
    inRange(i + 1);
    const len = buf.readUInt16BE(i);
    if (i < 0 || i > MAXIMUM_PACKET_SIZE) throw "INVALID";
    // package length
    inRange(low + len);
    valid = true, complete = true;
  } catch (e) {
    console.log(e)
    if (e === 'INVALID') 
      valid = false, complete = false;
    else 
      valid = true, complete = false;
    }
  return {valid, complete};
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