import net from 'net';

import * as config from './server-config';
import * as protocol from './lib/protocol';

let timer = 0;

const sock = net.createConnection({
  port: config.port,
  host: config.host
}, () => {
  console.log('connected to server');
  timer = setInterval(() => sock.write(protocol.makePacket(protocol.packetType.SERVER_CHECK)), 1200);
});

sock.on('end', () => {
  console.log('connection closed');
  if (timer) {clearInterval(timer); timer = 0;}
})

sock.on('data', buffer => {
  if (protocol.checkPacket(buffer).valid) {
    console.log(protocol.readPacket(buffer));
  } else {
    console.log(buffer)
  }
});

sock.on('error', err => {
  console.log(err.name, err.message, err.stack);
  if (timer) {clearInterval(timer); timer = 0;}
} );