import net from 'net';

import * as config from './server-config';

let timer = 0;

const sock = net.createConnection({
  port: config.port,
  host: config.host
}, () => {
  console.log('connected to server');
  timer = setInterval(() => sock.write('PING' + (new Date()).getMilliseconds()), 120);
});

sock.on('end', () => {
  console.log('connection closed');
  if (timer) {clearInterval(timer); timer = 0;}
})

sock.on('data', buffer => console.log(buffer));

sock.on('error', err => {
  console.log(err.name, err.message, err.stack);
  if (timer) {clearInterval(timer); timer = 0;}
} );