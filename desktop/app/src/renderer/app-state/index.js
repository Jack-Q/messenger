import ServerConnection from '../network/server-connection';
import PeerSocket from '../network/peer-socket';

const updateCallback = [];

export default {
  connected: false,
  isLogin: false,
  isAudioMode: false,
  audioCall: {
    peerName: '',
    peerId: '',
    status: '',
    phase: 0,
    answerMode: false,
    terminateMode: false,
    sessionId: '',
    peerSocket: null,
  },
  serverConnection: undefined,
  username: '',
  sessionKey: '',
  buddyList: [],
  messageList: {},

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
        this.resetState();
      });
      this.serverConnection.on('data-buddy-list', (list) => {
        this.buddyList = list;
        this.update();
      });
      this.serverConnection.on('data-message', (msg) => {
        if (!this.messageList[msg.user]) {
          this.messageList[msg.user] = [];
        }
        this.messageList[msg.user].push({
          id: +new Date(),
          time: new Date(),
          content: msg.message,
          type: 'recv',
        });
        this.update();
      });
      // bind call related message handler
      this.bindCallMessageHandler();
    });
  },

  login(user, pass) {
    return this.connected ? this.serverConnection.login(user, pass).then((resp) => {
      if (resp.status) {
        this.isLogin = true;
        this.username = user;
        this.sessionKey = resp.sessionKey;
        this.update();
        return Promise.resolve({ name: user });
      }
      return Promise.reject(resp);
    }) : Promise.reject({ message: 'no server configured' });
  },

  register(user, pass) {
    return this.connected ? this.serverConnection.register(user, pass)
      .then((resp) => {
        if (resp.status) {
          // register finished, login account
          return this.login(user, pass);
        }
        return Promise.reject(resp);
      }) : Promise.reject({ message: 'no server configured' });
  },

  sendMessage(peerId, content) {
    const peer = this.buddyList.find(b => b.id === peerId);
    if (!peer) {
      return false;
    }

    if (!this.messageList[peer.name]) {
      this.messageList[peer.name] = [];
    }

    console.log(`send "${content}" to ${peer.name}`);

    this.messageList[peer.name].push({
      id: +new Date(),
      time: new Date(),
      content,
      type: 'send',
    });

    this.serverConnection.sendMessage(peer.name, peerId, content);

    this.update();

    return true;
  },

  requestCall(peerId) {
    if (this.isAudioMode) {
      return;
    }

    const peer = this.buddyList.find(b => b.id === peerId);
    this.isAudioMode = true;
    this.audioCall.peerName = peer.name;
    this.audioCall.peerId = peer.id;
    this.serverConnection.requestCall(peer.name, peerId);

    this.audioCall.answerMode = false;
    this.audioCall.terminateMode = false;
    this.audioCall.status = 'preparing...';
    this.audioCall.phase = 0;
    this.update();
    console.log(`call ${peer.name}`);
  },

  prepareCall() {
    if (!this.isAudioMode) {
      return;
    }

    this.serverConnection.prepareCall(this.audioCall.sessionId);
  },

  answerCall() {
    if (!this.isAudioMode) {
      return;
    }

    this.audioCall.answerMode = false;
    this.audioCall.terminateMode = false;
    this.audioCall.status = 'connecting...';
    this.audioCall.phase = 0;
    this.update();
    this.serverConnection.answerCall(this.audioCall.sessionId);
  },

  terminateCall() {
    if (!this.isAudioMode) {
      return;
    }

    this.audioCall.answerMode = false;
    this.audioCall.terminateMode = false;
    this.audioCall.status = 'ending...';
    this.audioCall.phase = 3;
    this.update();
    this.serverConnection.terminateCall(this.audioCall.sessionId);
  },

  bindCallMessageHandler() {
    this.serverConnection.on('call-init', (msg) => {
      if (!msg.status) {
        console.log(`ERROR: ${msg.message}`);
      }
      if (!this.isAudioMode) {
        // Callee
        const fromUser = this.buddyList.find(b => b.id === msg.fromUser);

        this.isAudioMode = true;
        this.audioCall.status = `incoming call from ${fromUser.name}`;
        this.audioCall.peerName = fromUser.name;
        this.audioCall.peerId = fromUser.id;

        this.audioCall.answerMode = true;
        this.audioCall.terminateMode = true;
        this.audioCall.phase = 1;
      } else {
        // Caller
        this.audioCall.answerMode = false;
        this.audioCall.terminateMode = true;
        this.audioCall.phase = 1;
        this.audioCall.status = 'waiting for answering...';
      }

      this.audioCall.sessionId = msg.sessionId;
      PeerSocket.createSock(msg.sessionId, {
        address: msg.address,
        port: msg.port,
      }).then((peerSocket) => {
        this.audioCall.peerSocket = peerSocket;
        peerSocket.sendSrvAddr(this.sessionKey);
      }).catch((error) => {
        console.log(error);
      });
      this.update();
    });
    this.serverConnection.on('call-addr', (msg) => {
      if (!msg.status) {
        console.log(`ERROR: ${msg.message}`);
      }
      if (msg.sessionId !== this.audioCall.sessionId) {
        console.log(
          `ERROR SID: expecting ${this.audioCall.sessionId}, got ${msg.sessionId}`);
      }
      this.audioCall.peerSocket.updatePeer(msg.address, msg.port);
      this.audioCall.peerSocket.sendSyn();
      this.update();
    });
    this.serverConnection.on('call-conn', (msg) => {
      if (!msg.status) {
        console.log(`ERROR: ${msg.message}`);
      }
      this.audioCall.status = 'chatting';
      this.audioCall.answerMode = false;
      this.audioCall.terminateMode = true;
      this.audioCall.status = 'enjoy chatting';
      this.audioCall.phase = 2;
      this.update();
    });
    this.serverConnection.on('call-end', (msg) => {
      if (!msg.status) {
        console.log(`ERROR: ${msg.message}`);
      }
      this.audioCall.status = 'finished';
      this.audioCall.peerSocket.close();
      this.audioCall.phase = 3;
      this.update();
      setTimeout(() => {
        this.audioCall.status = '';
        this.audioCall.peerSocket = null;
        this.audioCall.peerId = '';
        this.audioCall.peerName = '';
        this.audioCall.phase = 0;
        this.isAudioMode = false;
        this.update();
      }, 1000);
    });
  },

  onUpdate(callback) {
    updateCallback.push(callback);
  },

  update() {
    updateCallback.map(cb => cb());
  },

  resetState() {
    this.connected = false;
    this.isLogin = false;
    this.isAudioMode = false;
    this.username = '';
    this.buddyList = [];
    this.serverConnection = null;
    this.update();
  },
};
