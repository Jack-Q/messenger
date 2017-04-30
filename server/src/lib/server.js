import net from 'net';
import dgram from 'dgram';
import shortid from 'shortid';

import * as protocol from './protocol';
import { checkUser, createUser } from './user-manager';

import ClientConnection from './client-connection';
import SessionManager from './session-manager';

export default class Server {
  constructor(host, port) {
    this.host = host;
    this.port = port;
    this.server = net.createServer();
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
  }

  handleClientAction(connection, type, payload) {
    switch (type.type) {
      case protocol.packetType.USER_ADD_REQ.type:
        createUser(payload.name, payload.token);
        connection.send(protocol.packetType.USER_ADD_RESP, { status: true, message: 'ok' });
        break;
      case protocol.packetType.USER_LOGIN_REQ.type:
        if (checkUser(payload.name, payload.token))
          connection.send(protocol.packetType.USER_LOGIN_RESP, { status: true, message: 'ok', sessionKey: ')AS(0' });
        else
          connection.send(protocol.packetType.USER_LOGIN_RESP, { status: false, message: 'login failed', sessionKey: '' });
    }
  }

  handleClientClose(connection) {
    console.log(connection, 'closed');
  }
}