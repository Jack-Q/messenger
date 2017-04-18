import net from 'net';
import dgram from 'dgram';

const BUFFER_SIZE = 10240;

const server = net.createServer(sock => {
  const readBuffer = Buffer.allocUnsafe(BUFFER_SIZE);
  let bufferLow = 0, bufferHigh = 0;

  const writeBuffer = Buffer.allocUnsafe(BUFFER_SIZE);

  console.log(sock.remoteAddress + ':' + sock.remotePort + ' connected');

  sock.on('data', data => {
    console.log('data from ' + sock.remoteAddress + ':' + sock.remotePort);
    console.log(data, data.toString());
    // buffer.copy(targetBuffer, targetStart, sourceStart, sourceEnd)
    const len = writeBuffer.write("DATA_RECV");
    sock.write(writeBuffer.slice(0, len));
  });

  sock.on('timeout', data => {
    console.log(sock.remoteAddress + ':' + sock.remotePort + ' timeout');
  })

  sock.on('close', data => {
    console.log(sock.remoteAddress + ':' + sock.remotePort + ' closed');
  })
})

server.listen('12121', '0.0.0.0', () => {
  console.log("Server listening ", server.address());
});
