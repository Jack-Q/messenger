const HEADER_LIMIT = 30;
const HEADER_SEPARATOR = ':'.charCodeAt(0);

export const dataType = {
  AUDIO: 'audio',
};

export const packetType = {
  // client/server
  U_SRV_ADDR: { type: 'U_SRV_ADDR', value: 0xa1 },
  // peer to peer
  U_SYN: { type: 'U_SYN', value: 0x01 },
  U_ACK: { type: 'U_SYN', value: 0x02 },
  U_DAT: { type: 'U_SYN', value: 0x03 },
  U_END: { type: 'U_SYN', value: 0x04 },
};

const makePacketOnlyToken = (type, sessionId) => {
  const buffer = Buffer.allocUnsafe(sessionId.length + 1);
  buffer.writeUInt8(type, 0);
  buffer.write(sessionId, 1);
  return buffer;
};
const makePacketByType = {
  U_SRV_ADDR: data => {
    const header = `${data.sessionId}:${data.connectId}`;
    const buffer = Buffer.allocUnsafe(header.length + 1);
    buffer.writeUInt8(packetType.U_SRV_ADDR.value, 0);
    buffer.write(header, 1);
    return buffer;
  },
  U_SYN: data => makePacketOnlyToken(packetType.U_SYN.value, data.sessionId),
  U_ACK: data => makePacketOnlyToken(packetType.U_ACK.value, data.sessionId),
  U_DAT: data => {
    const header = data.sessionId + ':' + data.type + ':';
    const buffer = Buffer.allocUnsafe(1 + header.length + data.buffer.length);
    let pos = 0;
    pos = buffer.write(packetType.U_DAT.value, pos);
    pos = buffer.write(header, pos);
    data.buffer.copy(buffer, pos);
    return buffer;
  },
  U_END: data => makePacketOnlyToken(packetType.U_END.value, data.sessionId),
};

const parsePacketOnlyToken = msg => ({ sessionId: msg.toString('utf8', 1, HEADER_LIMIT) });
const parsePacketByType = {
  U_SRV_ADDR: msg => {
    const arr = msg.toString('utf8', 1, HEADER_LIMIT).split(':');
    return {sessionId: arr[0], connectId: arr[1]};
  },
  U_SYN: parsePacketOnlyToken,
  U_ACK: parsePacketOnlyToken,
  U_DAT: msg => {
    const sep = [];
    
    // Scan message packet for message content
    // skip type indicator (pos 0)
    for (let pos = 1, cnt = 0; cnt < 2 && pos < HEADER_LIMIT && pos < msg.length; i++) {
      if (buf.readUInt8(i) === separator) {
        buf[cnt++] = pos;
      }
    }
    
    return {
      sessionId: msg.toString('utf8', 1, sep[0]),
      type: msg.toString('utf8', sep[0] + 1, sep[1]),
      buffer: msg.slice('utf8', sep[1] + 1),
    };
  },
  U_END: parsePacketOnlyToken,
};

export const makePacket = (type, data) => makePacketByType[type.type || type](data);

export const parsePacket = (msg) => {
  try {
    const type = Object.keys(packetType).map(k => packetType[k])
      .find(k => k.value === buffer[0]);
    const payload = parsePacketByType[type.type](msg);
    return { valid: true, type, payload };
  } catch (e) {
    return { valid: false };
  }
}