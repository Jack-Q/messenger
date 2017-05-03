const makeSuccess = msg => ({ status: true, message: msg || 'ok' });
const makeError = msg => ({ status: false, message: msg || 'server error' });

class UserStorage {
  constructor() {
    this.store = [];
  }

  add(name, token) {
    if (name.trim().length === 0) {
      return makeError('name cannot be empty');
    }
    if (token.trim().length === 0) {
      return makeError('token cannot be empty');
    }

    if (this.store.some(u => u.name === name)) {
      return makeError('requested name has already been taken');
    }

    this.store.push({ name, token })
    return makeSuccess();
  }

  check(name, token) {
    return this.store.filter(i => i.name == name && i.token == token).length > 0
  }
}

const userStorage = new UserStorage();

export const createUser = (name, token) => userStorage.add(name, token);

export const checkUser = (name, token) => userStorage.check(name, token);
