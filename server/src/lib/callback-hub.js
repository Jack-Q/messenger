const addCallback = (map, type, callback) => {
    if (map[type] === undefined) map[type] = [];
    map[type].push(callback);
}

export default class CallbackHub {
  constructor() {
    this.once = {};
    this.each = {};
  }

  listen(type, callback) {
    addCallback(this.each, type, callback);
  }

  listenOnce(type, callback) {
    addCallback(this.once, type, callback);
  }

  pub(type, ...args) {
    if (this.once[type] && this.once[type].length > 0) {
      this.once[type].map(cb => cb.apply(null, args));
      this.once[type] = [];
    }

    this.each[type] && this.each[type].map(cb => cb.apply(null, args));
  }
}