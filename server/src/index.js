import net from 'net';
import dgram from 'dgram';

import Server from './lib/server';

import { host, port } from './server-config';

const server = new Server(host, port);

server.initServer();