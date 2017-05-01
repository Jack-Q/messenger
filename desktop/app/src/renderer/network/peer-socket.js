import dgram from 'dgram';

import CallbackHub from '../../../../../server/src/lib/callback-hub';
import * as udpProtocol from '../../../../../server/src/lib/udp-protocol';

export default class PeerSocket {
  eventType = {
    // Connection message
    CONN_ERROR: 'CONN_ERROR',
    CONN_PREP: 'CONN_PREP',
    CONN_END: 'CONN_END',
    // Data message
    DATA_AUDIO: 'DATA_AUDIO',
  }

  static createSock(sessionId, serverAddress) {
    return new Promise((res, rej) => {
      const dgramSock = dgram.createSocket('udp4');
      dgramSock.bind({ exclusive: true }, () => {
        res(new PeerSocket(dgramSock, sessionId, serverAddress));
      });
      dgramSock.once('error', (error) => {
        dgramSock.close();
        rej(error);
      });
    });
  }

  constructor(sock, sessionId, serverAddress) {
    if (!sock) {
      return;
    }
    this.sock = sock;
    this.sessionId = sessionId;
    this.address = { address: sock.address().address, port: sock.address().port };
    this.peerAddress = { address: '', port: 0 };
    this.serverAddress = { address: serverAddress.address, port: serverAddress.port };
    this.callbackHub = new CallbackHub();

    sock.on('message', (msg, rinfo) => this.onMessage(msg, rinfo));
    sock.on('error', err => this.onError(err));
  }

  onMessage(message, remoteInfo) {
    const { address, port } = remoteInfo;
    console.log('udp message from ', address, port, message.toString());

    const messageData = udpProtocol.parsePacket(message);
    if (!messageData.valid) {
      console.log('invalid UDP packet received');
      return;
    }

    if (messageData.payload.sessionId !== this.sessionId) {
      console.log('invalid SessionId field');
      return;
    }

    switch (messageData.type.type) {
      case udpProtocol.packetType.U_SYN:
        this.updatePeer(address, port);
        this.sendAck();
        console.log(`SYN from ${address}:${port}`);
        this.callbackHub.pub(this.eventType.CONN_PREP);
        break;
      case udpProtocol.packetType.U_ACK:
        this.updatePeer(address, port);
        console.log(`ACK from ${address}:${port}`);
        this.callbackHub.pub(this.eventType.CONN_PREP);
        break;
      case udpProtocol.packetType.U_DAT:
        console.log(`Data package received from ${address}:${port}`);
        switch (messageData.payload.type) {
          case udpProtocol.dataType.AUDIO:
            this.callbackHub.pub(this.eventType.DATA_AUDIO, messageData.payload.buffer);
            break;
          default:
            console.log('unknown data type');
        }
        break;
      case udpProtocol.packetType.U_END:
        console.log(`end of session from ${address}:${port}`);
        this.callbackHub.pub(this.eventType.CONN_END);
        break;
      default:
        console.log(`unknown packet type from ${address}:${port}`);
        // Discard unknown message
    }
  }

  updatePeer(address, port) {
    if (this.peerAddress.address !== address) {
      console.log(`update peer address from ${this.peerAddress.address} to ${address}`);
      this.peerAddress.address = address;
    }
    if (this.peerAddress.port !== port) {
      console.log(`update peer port from ${this.peerAddress.port} to ${port}`);
      this.peerAddress.port = port;
    }
  }

  onError(error) {
    console.log(error);
    this.callbackHub.pub(this.eventType.CONN_ERROR, error);
  }

  send(address, type, data) {
    this.sock.send(udpProtocol.makePacket(type, data), address.port, address.address);
  }

  sendSyn() {
    this.send(this.peerAddress, udpProtocol.packetType.U_SYN, { sessionId: this.sessionId });
  }

  sendAck() {
    this.send(this.peerAddress, udpProtocol.packetType.U_ACK, { sessionId: this.sessionId });
  }

  sendData(buffer) {
    this.send(this.peerAddress, udpProtocol.packetType.U_SYN, {
      buffer,
      type: udpProtocol.dataType.AUDIO,
      sessionId: this.sessionId,
    });
  }

  sendSrvAddr(connectId) {
    this.send(this.serverAddress,
      udpProtocol.packetType.U_SRV_ADDR, {
        sessionId: this.sessionId,
        connectId,
      });
  }

  sendEnd() {
    this.send(this.peerAddress, udpProtocol.packetType.U_END, { sessionId: this.sessionId });
  }

  close() {
    this.sock.close();
  }
}
