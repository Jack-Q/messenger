import dgram from 'dgram';

const PORT = 42001;

const sock = dgram.createSocket('udp4');
sock.on('message', (msg, rinfo) => {
  console.log(`${rinfo.address}:${rinfo.port}`);
  console.log(`Message: ${msg}`);
});

sock.on('error', err => {
  console.log(err.message);
})
sock.bind(PORT);