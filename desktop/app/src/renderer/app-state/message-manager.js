class MessageList {
  constructor(id, updateCallback) {
    this.id = id;
    this.list = [];
    this.unread = 0;
    this.updateCallback = updateCallback;
  }

  push(message) {
    this.unread++;
    this.list.push(message);
  }

  readAll() {
    if (this.unread) {
      this.unread = 0;
      this.updateCallback();
    }
  }

  pushReceive(message) {
    this.push({
      id: +new Date(),
      time: new Date(),
      content: message,
      type: 'recv',
    });
  }
  pushSend(message) {
    this.push({
      id: +new Date(),
      time: new Date(),
      content: message,
      type: 'send',
    });
  }
  pushSystem(message) {
    this.push({
      id: +new Date(),
      time: new Date(),
      content: message,
      type: 'sys',
    });
  }
}

export default class MessageManager {
  constructor(updateCallback) {
    this.messageList = {};
    this.updateCallback = updateCallback;
  }

  addNewMessage(id, message) {
    this.getList(id).push(message);
  }

  getList(id) {
    if (!this.messageList[id]) {
      this.messageList[id] = new MessageList(id, this.updateCallback);
    }
    return this.messageList[id];
  }

  getUnread(id) {
    return this.getList(id).unread;
  }

  readAll(id) {
    this.getList(id).readAll();
  }

  getMessages(id) {
    return this.getList(id).list;
  }
}
