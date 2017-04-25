import net from 'net';
import dgram from 'dgram';

import * as userManager from './lib/user-manager';
import * as protocol from './lib/protocol';

import { ClientConnection } from './lib/client-connection';

const server = net.createServer(sock => new ClientConnection(sock));

server.addListener('error', err => console.log(err));

server.listen('12121', '0.0.0.0', () => {
  console.log("Server listening ", server.address());
});
