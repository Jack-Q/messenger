import net from 'net';
import dgram from 'dgram';
import shortid from 'shortid';

import * as protocol from './protocol';
import { checkUser, createUser } from './user-manager';

import ClientConnection from './client-connection';
import SessionManager from './session-manager';

const packConnection = conn => ({
  id: conn.id,
  pending: true,
  sessionId: null,
  user: {
    name: 'anonymous'
  },
  connection: conn,
});

export default class Server {
  constructor(host, port) {
    this.host = host;
    this.port = port;
    this.server = net.createServer();
    this.connections = [];
    this.sessionManager = new SessionManager(host);
  }

  // start server
  initServer() {
    this.server.addListener('connection', sock => this.onCreateSock(sock));
    this.server.addListener('error', err => console.log(err));

    this.server.listen(this.port, this.host, () => {
      console.log("Server listening ", this.server.address());
    });

    this.sessionManager.init();
  }

  onCreateSock(sock) {
    const id = shortid();
    const connection = new ClientConnection(id, sock);
    connection.listenData(this.handleClientAction.bind(this));
    connection.listenClose(this.handleClientClose.bind(this));
    this.addNewConnection(connection);
  }

  handleClientAction(connection, type, payload) {
    const srcConn = this.connections.find(c => c.id == connection.id);
    let conn = null;
    switch (type.type) {
      case protocol.packetType.USER_ADD_REQ.type:
        connection.send(protocol.packetType.USER_ADD_RESP,
          createUser(payload.name, payload.token));
        break;
      case protocol.packetType.USER_LOGIN_REQ.type:
        if (this.connections.some(c => c.user.name === payload.name)) {
          connection.send(protocol.packetType.USER_LOGIN_RESP, { status: false, message: 'requested login name is in use, please pick another one', sessionKey: '' });
          return;
        }
        if (!checkUser(payload.name, payload.token)) {
          connection.send(protocol.packetType.USER_LOGIN_RESP, { status: false, message: 'invalid login name or token, please check again', sessionKey: '' });
          return;
        }
        connection.send(protocol.packetType.USER_LOGIN_RESP, { status: true, message: 'ok', sessionKey: connection.id });
        this.loginConnection(connection, payload.name);
        break;
      case protocol.packetType.MSG_SEND.type:
        console.log("redirect message from", payload.user, payload.message);
        conn = this.connections.find(c => c.id == payload.connectId);
        if (conn && srcConn) {
          conn.connection.send(protocol.packetType.MSG_RECV, {
            status: true,
            user: srcConn.user.name,
            connectId: srcConn.id,
            message: payload.message,
          });
        }
        break;
      case protocol.packetType.CALL_REQ.type:
        conn = this.connections.find(c => c.id == payload.connectId);
        if (!srcConn) { 
          console.log('invalid request');
          return;
        }
        if( srcConn.pending) {
          // caller unsatisfied state
          this.sessionManager.rejectSession(srcConn, "telephony requires an identity");
          return;
        }
        if (srcConn.sessionId) {
          this.sessionManager.rejectSession(srcConn, "only single simultaneous session is supported");
        }
        if (!conn || conn.pending || conn.sessionId) {
          // handle callee unsatisfied state
          if (conn.sessionId) {
            // the callee is busy, response to caller about this state
            this.sessionManager.rejectSession(srcConn, `sorry, ${conn.user.name} is busy...`);
          } else {
            // unknown reason
            this.sessionManager.rejectSession(srcConn, "requested callee not found");
          }
          return;
        }
        // create the session, and the following process are handled 
        // by the SessionManager
        console.log('create session');
        this.sessionManager.createSession(srcConn, conn);
        break;
      case protocol.packetType.CALL_PREP.type:
        this.sessionManager.prepareCall(payload.sessionId, srcConn.id);
        break;
      case protocol.packetType.CALL_ANS.type:
        this.sessionManager.answerCall(payload.sessionId, srcConn.id);
        break;
      case protocol.packetType.CALL_TERM.type:
        this.sessionManager.terminateCall(payload.sessionId, srcConn.id);
        break;
    }
  }

  addNewConnection(rawConn) {
    this.connections.push(packConnection(rawConn));
  }

  loginConnection(rawConn, name) { 
    const conn = this.connections.find(conn => conn.id === rawConn.id);
    conn.pending = false;
    conn.user.name = name;
    this.sendBuddyListToAll();
    console.log(this.connections.map(conn => ({
      ...conn, connection: {
        ip: conn.connection.remoteAddress,
        port: conn.connection.remotePort,
      }
    })));
  }

  removeConnection(rawConn) {
    const connIndex = this.connections.findIndex(conn => conn.connection === rawConn)
    const conn = this.connections[connIndex];
    this.connections.splice(connIndex, 1);
    
    if (conn.sessionId) {
      this.sessionManager.terminateCall(conn.sessionId, conn.id);
    }

    this.sendBuddyListToAll();
  }
  
  sendBuddyList(conn) {
    conn.connection.send(protocol.packetType.INFO_RESP.type, {
      status: true,
      message: 'ok',
      type: protocol.infoType.BUDDY_LIST,
      payload: this.connections
        .filter(c => c != conn && !c.pending)
        .map(c => ({
          id: c.id,
          name: c.user.name,
          ip: c.connection.remoteAddress,
        })),
    })
  }

  sendBuddyListToAll() {
    this.connections.filter(conn => !conn.pending).map(conn => this.sendBuddyList(conn));
  }

  handleClientClose(rawConn) {
    this.removeConnection(rawConn);
    console.log(rawConn.id, 'closed');
  }
}