import dgram from 'dgram';

import CallbackHub from '../../../../../server/src/lib/callback-hub';

export default class PeerSocket {
  eventType = {
    CONN_ERROR: 'CONN_ERROR',
  }

  static createSock() {
    return new Promise((res, rej) => {
      const dgramSock = dgram.createSocket('udp4');
      dgramSock.bind({ exclusive: true }, () => {
        res(new PeerSocket(dgramSock));
      });
      dgramSock.once('error', (error) => {
        dgramSock.close();
        rej(error);
      });
    });
  }

  constructor(sock) {
    if (!sock) {
      return;
    }
    this.sock = sock;
    this.address = sock.address().address;
    this.port = sock.address().port;
    this.callbackHub = new CallbackHub();

    sock.on('message', (msg, rinfo) => this.onMessage(msg, rinfo));
    sock.on('error', err => this.onError(err));
  }

  onMessage(message, remoteInfo) {
    const { address, port } = remoteInfo;
    console.log('udp message from ', address, port, message.toString());

    this.callbackHub.pub(this.eventType.CONN_ERROR, { message: 'unhandled message' });
  }

  onError(error) {
    console.log(error);
    this.callbackHub.pub(this.eventType.CONN_ERROR, error);
  }
}
