import { createSock } from '../server/network';

export default {
  connected: false,
  isLogin: false,
  isAudioMode: false,
  serverConnection: undefined,
  buddyList: [],

  connect(host, port) {
    return createSock(host, port).then(sock => {
      console.log('connected to server');
      this.serverConnection = sock;
      this.connected = true;

      this.serverConnection.on('connection-close', () => {
        this.connected = false;
        this.serverConnection = null;
      });
    });
  },

  login(user, pass) {
    return this.connected ? this.serverConnection.login(user, pass)
      : Promise.reject('no server configured');
  },
};
