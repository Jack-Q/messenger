import net from 'net';
import dgram from 'dgram';

import Server from './lib/server';

import { publicHostname, host, port } from './server-config';

const server = new Server(publicHostname, host, port);

server.initServer();