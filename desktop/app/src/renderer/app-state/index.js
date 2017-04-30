import { createSock } from '../server/network';

const updateCallback = [];

export default {
  connected: false,
  isLogin: false,
  isAudioMode: false,
  serverConnection: undefined,
  buddyList: [],

  connect(host, port) {
    if (this.connected) {
      return Promise.resolve();
    }

    return createSock(host, port).then(sock => {
      console.log('connected to server');
      this.serverConnection = sock;
      this.connected = true;
      this.update();
      this.serverConnection.on('connection-close', () => {
        this.connected = false;
        this.serverConnection = null;
        this.update();
      });
    });
  },

  login(user, pass) {
    return this.connected ? this.serverConnection.login(user, pass)
      : Promise.reject({ message: 'no server configured' });
  },

  onUpdate(callback) {
    updateCallback.push(callback);
  },

  update() {
    updateCallback.map(cb => cb());
  },
};
