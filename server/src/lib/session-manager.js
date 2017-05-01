import dgram from 'dgram';
import shortid from 'shortid';

import * as udpProtocol from './udp-protocol';

class Session{
  constructor(sessionId, caller, callee) {
    this.sessionId = sessionId;
    this.caller = caller;
    this.callerInfo = {};
    this.callee = callee;
    this.calleeInfo = {};
  }

  updateAddress(connectId, address, port) {
    if (this.caller.id === connectId) {
      this.callerInfo.address = address;
      this.callerInfo.port = port;
    } else if (this.callee.id === connectId){
      this.calleeInfo.address = address;
      this.calleeInfo.port = port;
    } else {
      console.log("unknown connect id");
    }
  }
}

export default class SessionManager {
  constructor() {
    this.sessionList = {};
    this.serverAddress = {
      address: '',
      port: 0,
    };
    this.udpSocket = dgram.createSocket('udp4');
  }

  init() {
    this.udpSocket.on('message', this.onMessage.bind(this));
    this.udpSocket.on('error', this.onError.bind(this));
    this.udpSocket.bind({
      exclusive: true
    }, () => {
      this.serverAddress.address = this.udpSocket.address().address;
      this.serverAddress.port = this.udpSocket.address().port;
    });
  }

  createSession(caller, callee) {
    const sessionId = shortid();
    callee.sessionId = sessionId;
    caller.sessionId = sessionId;
    this.sessionList[sessionId] = new Session(sessionId, caller, callee);
    return sessionId;
  }

  onMessage(msg, rinfo) {
    const { address, port } = remoteInfo;
    console.log('udp message from ', address, port, message.toString());

    const messageData = udpProtocol.parsePacket(message);
    if (!messageData.valid) {
      console.log('invalid UDP packet received');
      return;
    }

    const sessionId = messageData.payload.sessionId;
    const connectId = messageData.payload.connectId;
    const session = sessionList[sessionId];

    if (!sessionId || !session) {
      console.log('invalid SessionId field');
      return;
    }

    switch (messageData.type) {
      case udpProtocol.packetType.U_SRV_ADDR:
        session.updateAddress(connectId, address, port);
        break;
      default:
        console.log("unknown message type");  
    }
  }

  onError(err) {
    console.log('Session socket error:', err);
  }

  prepareCall(sessionId, userId) {
    
  }
  answerCall(sessionId, userId) {
    
  }
  terminateCall(sessionId, userId) {
    
  }
}