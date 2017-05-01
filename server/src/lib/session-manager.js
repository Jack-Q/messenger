import dgram from 'dgram';
import shortid from 'shortid';

import * as udpProtocol from './udp-protocol';
import * as protocol from './protocol';

class Session{
  constructor(sessionId, caller, callee) {
    this.sessionId = sessionId;
    this.caller = caller;
    this.callerInfo = {
      address: '',
      port: '',
      prepared: false,
    };
    this.callee = callee;
    this.calleeInfo = {
      address: '',
      port: '',
      prepared: false,
    };
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

    // send init messages to peers of current session
    const initPacket = {
      status: true,
      message: 'ok',
      sessionId: sessionId,
      address: this.serverAddress.address,
      port: this.serverAddress.port,
    };
    caller.connection.send(protocol.packetType.CALL_INIT, initPacket);
    callee.connection.send(protocol.packetType.CALL_INIT, initPacket);

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
        // update the new address to registry  
        session.updateAddress(connectId, address, port);
        // send the new data to the peer
        if (session.caller.id === connectId) {
          // update caller, send to callee
          session.callee.connection.send(protocol.packetType.CALL_ADDR, {
            status: true,
            user: caller.id,
            sessionId: sessionId,
            address: address,
            port: port,
          });
        } else if(session.caller.id === connectId) {
          session.caller.connection.send(protocol.packetType.CALL_ADDR, {
            status: true,
            user: callee.id,
            sessionId: sessionId,
            address: address,
            port: port,
          });
        }
        break;
      default:
        console.log("unknown message type");  
    }
  }

  onError(err) {
    console.log('Session socket error:', err);
  }

  prepareCall(sessionId, userId) {
    const session = this.sessionList[sessionId];
    if (!session) {
      console.log(`Error: unknown session id received: ${sessionId}`);
      return;
    }

    if (session.caller.id === userId) {
      session.callerInfo.prepared = true;
    } else if (session.callee.id === userId) {
      session.callee.prepared = true;
    }
  }

  answerCall(sessionId, userId) {
    const session = this.sessionList[sessionId];
    if (!session) {
      console.log(`Error: unknown session id received: ${sessionId}`);
      return;
    }
    
    // only callee can answer the call
    if (session.callee.id !== userId) {
      console.log('the user who try to answer the call is not the callee');
      return;
    }


    const connPacket = {
      status: true,
      message: 'ok',
      sessionId: sessionId,
    };
    session.caller.connection.send(protocol.packetType.CALL_CONN, connPacket);
    session.callee.connection.send(protocol.packetType.CALL_CONN, connPacket);
  }

  terminateCall(sessionId, userId) {
    const session = this.sessionList[sessionId];
    if (!session) {
      console.log(`Error: unknown session id received: ${sessionId}`);
      return;
    }
    
    if (session.caller.id === userId) {
      session.callerInfo.prepared = true;
    } else if (session.callee.id === userId) {
      session.callee.prepared = true;
    }

    const endPacket = {
      status: true,
      message: 'ok',
      sessionId: sessionId,
    };
    session.caller.connection.send(protocol.packetType.CALL_END, endPacket);
    session.callee.connection.send(protocol.packetType.CALL_END, endPacket);

    // erase the session from server after 500ms
    setTimeout(() => {
      delete this.sessionList[sessionId];
      session.callee.sessionId = null;
      session.caller.sessionId = null;
    }, 500);
  }
}