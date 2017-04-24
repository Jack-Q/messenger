import net from 'net';
import dgram from 'dgram';

import { Manager } from './lib/user-manager';
import * as protocol from './lib/protocol';

const BUFFER_SIZE = 10240;

const createClient = sock => {
  const readBuffer = Buffer.allocUnsafe(BUFFER_SIZE);
  let bufferLow = 0, bufferHigh = 0;

  const writeBuffer = Buffer.allocUnsafe(BUFFER_SIZE);

  console.log(sock.remoteAddress + ':' + sock.remotePort + ' connected');

  sock.on('data', data => {
    console.log('data from ' + sock.remoteAddress + ':' + sock.remotePort);
    console.log(data, data.toString());
    
    // Copy buffer
    if (readBuffer.length - bufferHigh < data.length) {
      if (readBuffer.length > bufferHigh - bufferLow + data.length) {
        readBuffer.copy(readBuffer, 0, bufferLow, bufferHigh);
        bufferHigh -= bufferLow;
        bufferLow = 0;
      } else {
        console.error("large package size");
        // TODO: close this connection since the state of this connection is unstable
        return;
      }
    }
    data.copy(readBuffer, bufferHigh);
    bufferHigh += data.length;

    const { valid, complete } = protocol.checkPacket(readBuffer, bufferLow, bufferHigh);
    if (!valid) {
      console.error("invalid package received, reset package");
      // TODO: close or ignore this invalid packet
      return;
    }

    if (!complete)
      return;  

    const { data: { type, payload }, length } = protocol.readPacket(readBuffer, bufferLow, bufferHigh);

    console.log("receive data:" + type + ":" + payload);
    
    bufferLow += length;
    if (bufferLow == bufferHigh)
      bufferLow = bufferHigh = 0;  

    const len = writeBuffer.write("DATA_RECV");
    sock.write(writeBuffer.slice(0, len));
  });

  sock.on('timeout', data => {
    console.log(sock.remoteAddress + ':' + sock.remotePort + ' timeout');
  })

  sock.on('close', data => {
    console.log(sock.remoteAddress + ':' + sock.remotePort + ' closed');
  })
};

const server = net.createServer(createClient)

server.addListener('error', err => console.log(err));

server.listen('12121', '0.0.0.0', () => {
  console.log("Server listening ", server.address());
});

Manager.checkUser('Jack', 'test');