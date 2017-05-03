import ServerConnection from '../network/server-connection';
import PeerSocket from '../network/peer-socket';
import Audio from '../audio';
import MessageManager from './message-manager';

class AppState {
  constructor() {
    this.connected = false;
    this.isLogin = false;
    this.isAudioMode = false;
    this.audioCall = {
      peerName: '',
      peerId: '',
      status: '',
      phase: 0,
      answerMode: false,
      terminateMode: false,
      sessionId: '',
      peerSocket: null,
    };
    this.serverConnection = undefined;
    this.username = '';
    this.sessionKey = '';
    this.buddyList = [];
    this.audio = new Audio();
    this.updateCallback = [];
    this.messageManager = new MessageManager(() => this.update());
  }

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
        this.messageManager.getList(msg.user).pushReceive(msg.message);
        this.update();
      });
      // bind call related message handler
      this.bindCallMessageHandler();
    });
  }

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
  }

  register(user, pass) {
    return this.connected ? this.serverConnection.register(user, pass)
      .then((resp) => {
        if (resp.status) {
          // register finished, login account
          return this.login(user, pass);
        }
        return Promise.reject(resp);
      }) : Promise.reject({ message: 'no server configured' });
  }

  sendMessage(peerId, content) {
    const peer = this.buddyList.find(b => b.id === peerId);
    if (!peer) {
      return false;
    }

    this.messageManager.getList(peer.name).pushSend(content);

    this.serverConnection.sendMessage(peer.name, peerId, content);

    this.update();

    return true;
  }

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
  }

  prepareCall() {
    if (!this.isAudioMode) {
      return;
    }

    this.serverConnection.prepareCall(this.audioCall.sessionId);
  }

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
  }

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
  }

  bindCallMessageHandler() {
    const endAudioMode = (status) => {
      if (!this.isAudioMode) {
        // unintended message, ignore the packet
        console.log(`No in call mode: ${status}`);
        return;
      }
      console.log(`Call ended: ${status}`);
      // add call record
      if (this.audioCall.peerName) {
        this.messageManager.getList(this.audioCall.peerName).pushSystem(this.audioCall.phase === 2
            ? 'call finished' : 'call terminated');
      }

      // terminate session
      this.audio.endSession();
      this.audio.setOnPacket(null);
      // transit call state
      this.audioCall.status = status;
      if (this.audioCall.peerSocket) { this.audioCall.peerSocket.close(); }
      this.audioCall.phase = 3;
      this.audioCall.answerMode = false;
      this.audioCall.terminateMode = false;
      this.update();
      setTimeout(() => {
        this.audioCall.status = '';
        this.audioCall.peerSocket = null;
        this.audioCall.peerId = '';
        this.audioCall.peerName = '';
        this.audioCall.phase = 0;
        this.isAudioMode = false;
        this.update();
      }, 1500);
    };
    this.serverConnection.on('call-init', (msg) => {
      if (!msg.status) {
        endAudioMode(msg.message);
        return;
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
        endAudioMode(msg.message);
        return;
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
        endAudioMode(msg.message);
        return;
      }
      // start session
      this.audio.setOnPacket(packet => this.audioCall.peerSocket.sendData(packet));
      this.audioCall.peerSocket.on(this.audioCall.peerSocket.eventType.DATA_AUDIO,
        data => this.audio.receivePacket(data));
      this.audio.startSession();
      console.log('CALL-CONN: Session start');
      // transit call state
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
        endAudioMode(msg.message);
      } else {
        endAudioMode('call ended');
      }
    });
  }

  onUpdate(callback) {
    this.updateCallback.push(callback);
  }

  update() {
    this.updateCallback.map(cb => cb());
  }

  resetState() {
    if (this.audio) {
      this.audio.endSession();
    } else {
      this.audio = new Audio();
    }
    if (this.serverConnection) {
      this.serverConnection.close();
    }
    this.connected = false;
    this.isLogin = false;
    this.isAudioMode = false;
    this.username = '';
    this.buddyList = [];
    this.serverConnection = null;
    this.update();
  }
}

export default new AppState();
