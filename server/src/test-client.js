import net from 'net';

import * as config from './server-config';
import * as protocol from './lib/protocol';

import CallbackHub from './lib/callback-hub';

let timer = 0;

const callbackHub = new CallbackHub();

const register = (sock, info, cb) => {
  sock.write(protocol.makePacket(protocol.packetType.USER_ADD_REQ, { name: info.name, token: info.token }));
  callbackHub.listenOnce(protocol.packetType.USER_ADD_RESP.type, (data) => { 
    console.log(data);
    cb(data);
  });
}


const login = (sock, info, cb) => {
  sock.write(protocol.makePacket(protocol.packetType.USER_LOGIN_REQ, { name: info.name, token: info.token }));
  callbackHub.listenOnce(protocol.packetType.USER_LOGIN_RESP.type, (data) => { 
    console.log(data);
    cb(data);
  });
}



const sock = net.createConnection({
  port: config.port,
  host: config.host
}, () => {
  console.log('connected to server');
  timer = setInterval(() => sock.write(protocol.makePacket(protocol.packetType.SERVER_CHECK)), 1200);

  const identity = {
    name: 'Jack Q',
    token: 'token'
  };  
  register(sock, identity, data => {
    login(sock, identity, data => {
      console.log('auth success, session key: ', data.sessionKey);
    })
  });
  
});

sock.on('end', () => {
  console.log('connection closed');
  if (timer) {clearInterval(timer); timer = 0;}
})

sock.on('data', buffer => {
  if (protocol.checkPacket(buffer).valid) {
    const { data: { type, payload }, length } = protocol.readPacket(buffer);
    console.log(type, payload);
    callbackHub.pub(type.type || type, [payload]);
  } else {
    console.log(buffer)
  }
});

sock.on('error', err => {
  console.log(err.name, err.message, err.stack);
  if (timer) {clearInterval(timer); timer = 0;}
} );