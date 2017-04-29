import net from 'net';

import * as protocol from '../../../../../server/src/lib/protocol';
import { CallbackHub } from '../../../../../server/src/lib/callback-hub';

const BUFFER_SIZE = 10240;

const createBuffer = () => ({
  bufferLow: 0,
  bufferHigh: 0,
  readBuffer: Buffer.allocUnsafe(BUFFER_SIZE),
  writeBuffer: Buffer.allocUnsafe(BUFFER_SIZE),
});

class ServerConnection {
  constructor(host, port, sock) {
    this.callbackHub = new CallbackHub();
    this.host = host;
    this.sock = sock;
    this.port = port;
    this.buffer = createBuffer();
    this.timer = 0;

    this.sock.on('close', () => this.onClose());
    this.sock.on('timeout', () => this.onError('timeout'));
    this.sock.on('data', data => this.onData(data));

    this.startPing();
  }

  register(info, cb) {
    this.sock.write(protocol.makePacket(protocol.packetType.USER_ADD_REQ, {
      name: info.name,
      token: info.token,
    }));
    this.callbackHub.listenOnce(protocol.packetType.USER_ADD_RESP.type, (data) => {
      console.log(data);
      cb(data);
    });
  }

  login(info, cb) {
    this.sock.write(protocol.makePacket(protocol.packetType.USER_LOGIN_REQ, {
      name: info.name,
      token: info.token,
    }));
    this.callbackHub.listenOnce(protocol.packetType.USER_LOGIN_RESP.type, (data) => {
      console.log(data);
      cb(data);
    });
  }

  onError(err) {
    console.log(err.name, err.message, err.stack);
    this.stopPing();
  }

  onClose() {
    console.log('connection closed');
    this.stopPing();
  }

  onData(buffer) {
    if (protocol.checkPacket(buffer).valid) {
      const { data: { type, payload } /* , length */ } = protocol.readPacket(buffer);
      console.log(type, payload);
      this.callbackHub.pub(type.type || type, [payload]);
    } else {
      console.log(buffer);
    }
  }

  startPing() {
    this.stopPing();
    this.timer = setInterval(() =>
      this.sock.write(protocol.makePacket(protocol.packetType.SERVER_CHECK)), 1200);
  }

  stopPing() {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = 0;
    }
  }

  on(event, callback) {
    this.callbackHub.listen(event, callback);
  }
}

export const createSock = (host, port) => new Promise((res, rej) => {
  const sock = net.createConnection({ port, host }, (err) => {
    if (err) {
      console.log('failed to connect to server', err);
      rej(err);
      return;
    }

    console.log('connected to server');
    res(new ServerConnection(host, port, sock));
  });
  sock.on('error', e => {
    rej(e);
  });
});
