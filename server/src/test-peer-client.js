import dgram from 'dgram';

const PORT = 42001;

const sock = dgram.createSocket('udp4');

const remote = {
  address: undefined,
  port: undefined
}

sock.on('message', (msg, rinfo) => {
  console.log(`${rinfo.address}:${rinfo.port}`);
  console.log(`Message: ${msg}`);
  remote.address = rinfo.address;
  remote.port = rinfo.port;
});

sock.on('error', err => {
  console.log(err.message);
})

setInterval(() => {
  if (remote.address && remote.port)
    sock.send('ACK', remote.port, remote.address);
}, 1000 / 20);
sock.bind(PORT);