import { BUFFER_SIZE, SERVER_NAME } from '../server-config';
import * as protocol from './protocol';
import { checkUser, createUser } from './user-manager';

const createBuffer = () => ({bufferLow: 0, bufferHigh: 0, readBuffer: Buffer.allocUnsafe(BUFFER_SIZE), writeBuffer: Buffer.allocUnsafe(BUFFER_SIZE)});

export class ClientConnection {
  constructor(sock) {
    this.sock = sock;
    this.remoteAddress = sock.remoteAddress;
    this.remotePort = sock.remotePort;
    this.buffer = createBuffer();
    // bind event handler
    sock.on('close', () => this.onClose());
    sock.on('timeout', () => this.onTimeout());
    sock.on('data', data => this.onData(data))
  }

  // Send Message
  send(type, data) {
    const len = protocol.makePacketToBuffer(this.buffer.writeBuffer, type, data);
    this.sock.write(this.buffer.writeBuffer.slice(0, len));
  }

  // Client send data
  onData(data) {
    console.log('data from ' + this.remoteAddress + ':' + this.remotePort);
    const buf = this.buffer;

    // Copy buffer
    if (buf.readBuffer.length - buf.bufferHigh < data.length) {
      if (buf.readBuffer.length > buf.bufferHigh - buf.bufferLow + data.length) {
        buf.readBuffer.copy(buf.readBuffer, 0, buf.bufferLow, buf.bufferHigh);
        buf.bufferHigh -= buf.bufferLow;
        buf.bufferLow = 0;
      } else {
        console.error("large package size");
        // TODO: close this connection since the state of this connection is unstable
        return;
      }
    }
    data.copy(buf.readBuffer, buf.bufferHigh, 0, data.length);
    buf.bufferHigh += data.length;

    const {valid, complete} = protocol.checkPacket(buf.readBuffer, buf.bufferLow, buf.bufferHigh);
    if (!valid) {
      console.error("invalid package received, reset package");
      // TODO: close or ignore this invalid packet
      return;
    }

    if (!complete) {
      console.warn("incomplete package received, waiting for later package")
      return;
    }

    const { data: { type, payload }, length } = protocol.readPacket(buf.readBuffer, buf.bufferLow, buf.bufferHigh);

    console.log("receive data:", type.type, ":", payload);

    switch (type.type) {
      case protocol.packetType.USER_ADD_REQ.type:
        createUser(payload.name, payload.token);
        this.send(protocol.packetType.USER_ADD_RESP, { status: true, message: 'ok' });
        break;
      case protocol.packetType.USER_LOGIN_REQ.type:
        if (checkUser(payload.name, payload.token))
          this.send(protocol.packetType.USER_LOGIN_RESP, { status: true, message: 'ok', sessionKey: ')AS(0' });
        else
          this.send(protocol.packetType.USER_LOGIN_RESP, { status: false, message: 'login failed', sessionKey: '' });
    }

    buf.bufferLow += length;
    if (buf.bufferLow == buf.bufferHigh) 
      buf.bufferLow = buf.bufferHigh = 0;
    
    this.send(protocol.packetType.SERVER_STATUS, SERVER_NAME);

  }

  // Client disconnected from server
  onClose() {
    console.log(this.remoteAddress + ':' + this.remotePort + ' closed');
  }

  onTimeout() {
    console.log(this.remoteAddress + ':' + this.remotePort + ' closed');
  }
}