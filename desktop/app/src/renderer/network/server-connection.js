import net from 'net';

import * as protocol from '../../../../../server/src/lib/protocol';
import CallbackHub from '../../../../../server/src/lib/callback-hub';

const BUFFER_SIZE = 10240;

const createBuffer = () => ({
  bufferLow: 0,
  bufferHigh: 0,
  readBuffer: Buffer.allocUnsafe(BUFFER_SIZE),
  writeBuffer: Buffer.allocUnsafe(BUFFER_SIZE),
});

export default class ServerConnection {
  static createSock(host, port) {
    return new Promise((res, rej) => {
      const sock = net.createConnection({ port, host }, (err) => {
        if (err) {
          console.log('failed to connect to server', err);
          rej(err);
          return;
        }

        console.log('connected to server');
        res(new ServerConnection(host, port, sock));
      });
      sock.once('error', (e) => {
        rej(e);
      });
    });
  }

  constructor(host, port, sock) {
    this.callbackHub = new CallbackHub();
    this.host = host;
    this.sock = sock;
    this.port = port;
    this.buffer = createBuffer();
    this.timer = 0;

    this.sock.on('close', () => this.onClose());
    this.sock.on('data', data => this.onData(data));
    this.sock.on('error', err => this.onError(err));
    this.sock.on('timeout', () => this.onError('timeout'));

    this.bindPacketHandler();

    this.startPing();
  }

  register(user, pass) {
    return new Promise((res, rej) => {
      this.sock.write(protocol.makePacket(protocol.packetType.USER_ADD_REQ, {
        name: user,
        token: pass,
      }));
      this.sock.once('error', e => rej(e));
      this.callbackHub.listenOnce(protocol.packetType.USER_ADD_RESP.type, (data) => {
        console.log(data);
        res(data);
      });
    });
  }

  login(user, pass) {
    return new Promise((res, rej) => {
      this.sock.write(protocol.makePacket(protocol.packetType.USER_LOGIN_REQ, {
        name: user,
        token: pass,
      }));
      this.sock.once('error', e => rej(e));
      this.callbackHub.listenOnce(protocol.packetType.USER_LOGIN_RESP.type, (data) => {
        console.log('hub-resp', data);
        res(data);
      });
    });
  }

  sendMessage(user, connectId, message) {
    this.send(protocol.packetType.MSG_SEND, { user, connectId, message });
  }

  send(type, data) {
    this.sock.write(protocol.makePacket(type, data));
  }

  requestCall(user, connectId) {
    this.send(protocol.packetType.CALL_REQ, { user, connectId });
  }

  prepareCall(sessionId) {
    this.send(protocol.packetType.CALL_PREP, { sessionId });
  }

  answerCall(sessionId) {
    this.send(protocol.packetType.CALL_ANS, { sessionId });
  }

  terminateCall(sessionId) {
    this.send(protocol.packetType.CALL_TERM, { sessionId });
  }

  onError(err) {
    console.log(err.name, err.message, err.stack);
    this.stopPing();
  }

  onClose() {
    console.log('connection closed');
    this.callbackHub.pub('connection-close');
    this.stopPing();
  }

  onData(buffer) {
    // Copy buffer
    if (this.buffer.readBuffer.length - this.buffer.bufferHigh < buffer.length) {
      if (this.buffer.readBuffer.length >
        this.buffer.bufferHigh - this.buffer.bufferLow + buffer.length) {
        this.buffer.readBuffer.copy(
          this.buffer.readBuffer, 0, this.buffer.bufferLow, this.buffer.bufferHigh);
        this.buffer.bufferHigh -= this.buffer.bufferLow;
        this.buffer.bufferLow = 0;
      } else {
        console.error('large package size');
        // TODO: close this connection since the state of this connection is unstable
        return;
      }
    }
    buffer.copy(this.buffer.readBuffer, this.buffer.bufferHigh, 0, buffer.length);
    this.buffer.bufferHigh += buffer.length;

    // loop to abstract all the incomplete packets
    for (; ;) {
      const status = protocol.checkPacket(
        this.buffer.readBuffer, this.buffer.bufferLow, this.buffer.bufferHigh);

      if (!status.valid) {
        // clean the buffer
        this.callbackHub.pub('data-error', { message: 'invalid buffer' });
        this.buffer.bufferLow = this.buffer.bufferHigh = 0;
        break;
      }

      if (!status.complete) { break; }

      const { data: { type, payload }, length } = protocol.readPacket(
        this.buffer.readBuffer, this.buffer.bufferLow, this.buffer.bufferHigh);

      this.buffer.bufferLow += length;
      if (this.buffer.bufferLow === this.buffer.bufferHigh) {
        this.buffer.bufferLow = this.buffer.bufferHigh = 0;
      }

      this.callbackHub.pub(type.type || type, payload);
    }
  }

  startPing() {
    this.stopPing();
    this.timer = setInterval(() =>
      this.sock.write(protocol.makePacket(protocol.packetType.SERVER_CHECK)), 20000);
  }

  stopPing() {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = 0;
    }
  }

  bindPacketHandler() {
    this.callbackHub.listen(protocol.packetType.MSG_RECV.type, (msg) => {
      this.callbackHub.pub('data-message', { user: msg.user, connectId: msg.connectId, message: msg.message });
    });
    this.callbackHub.listen(protocol.packetType.INFO_RESP.type, (info) => {
      console.log(info);
      switch (info.type) {
        case protocol.infoType.BUDDY_LIST:
          this.callbackHub.pub('data-buddy-list', info.payload);
          break;
        default:
          console.log('unknown info received', info.type, info.payload);
      }
    });
    this.callbackHub.listen(protocol.packetType.CALL_INIT.type, (msg) => {
      this.callbackHub.pub('call-init', msg);
    });
    this.callbackHub.listen(protocol.packetType.CALL_ADDR.type, (msg) => {
      this.callbackHub.pub('call-addr', msg);
    });
    this.callbackHub.listen(protocol.packetType.CALL_CONN.type, (msg) => {
      this.callbackHub.pub('call-conn', msg);
    });
    this.callbackHub.listen(protocol.packetType.CALL_END.type, (msg) => {
      this.callbackHub.pub('call-end', msg);
    });
  }

  on(event, callback) {
    this.callbackHub.listen(event, callback);
  }

  getServerName() {
    return `${this.host}:${this.port}`;
  }
}
