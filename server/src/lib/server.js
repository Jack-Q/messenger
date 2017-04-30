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
  }

  // start server  
  initServer() {
    this.server.addListener('connection', sock => this.onCreateSock(sock));
    this.server.addListener('error', err => console.log(err));

    this.server.listen(this.port, this.host, () => {
      console.log("Server listening ", this.server.address());
    });
  }

  onCreateSock(sock) {
    const id = shortid();
    const connection = new ClientConnection(id, sock);
    connection.listenData(this.handleClientAction.bind(this));
    connection.listenClose(this.handleClientClose.bind(this));
    this.addNewConnection(connection);
  }

  handleClientAction(connection, type, payload) {
    switch (type.type) {
      case protocol.packetType.USER_ADD_REQ.type:
        createUser(payload.name, payload.token);
        connection.send(protocol.packetType.USER_ADD_RESP, { status: true, message: 'ok' });
        break;
      case protocol.packetType.USER_LOGIN_REQ.type:
        if (checkUser(payload.name, payload.token)) {
          connection.send(protocol.packetType.USER_LOGIN_RESP, { status: true, message: 'ok', sessionKey: connection.id });
          this.loginConnection(connection, payload.name);
        } 
        else {
          connection.send(protocol.packetType.USER_LOGIN_RESP, { status: false, message: 'login failed', sessionKey: '' });
        }
      case protocol.packetType.MSG_SEND.type:
        console.log("redirect message from", payload.user, payload.message);
        const conn = this.connections.find(c => c.id == payload.connectId);
        const srcConn = this.connections.find(c => c.id == connection.id);
        if (conn && srcConn) {
          conn.connection.send(protocol.packetType.MSG_RECV, {
            status: true,
            user: srcConn.user.name,
            connectId: srcConn.id,
            message: payload.message,
          });
        }
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
    this.connections.splice(this.connections.findIndex(conn => conn.connection === rawConn), 1);
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