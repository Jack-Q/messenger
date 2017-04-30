import ServerConnection from '../server/server-connection';

const updateCallback = [];

export default {
  connected: false,
  isLogin: false,
  isAudioMode: false,
  serverConnection: undefined,
  username: '',
  buddyList: [],

  connect(host, port) {
    if (this.connected) {
      return Promise.resolve();
    }

    return ServerConnection.createSock(host, port).then((sock) => {
      console.log('connected to server');
      this.serverConnection = sock;
      this.connected = true;
      this.update();
      this.serverConnection.on('connection-close', () => {
        this.connected = false;
        this.serverConnection = null;
        this.update();
      });
      this.serverConnection.on('data-buddy-list', (list) => {
        this.buddyList = list;
      });
    });
  },

  login(user, pass) {
    return this.connected ? this.serverConnection.login(user, pass).then((resp) => {
      if (resp.status) {
        this.isLogin = true;
        this.username = user;
        this.update();
        return Promise.resolve({ name: user });
      }
      return Promise.reject(resp);
    }) : Promise.reject({ message: 'no server configured' });
  },

  register(user, pass) {
    return this.connected ? this.serverConnection.register(user, pass)
        .then(() => this.login(user, pass))
      : Promise.reject({ message: 'no server configured' });
  },

  onUpdate(callback) {
    updateCallback.push(callback);
  },

  update() {
    updateCallback.map(cb => cb());
  },
};
