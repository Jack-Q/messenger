class UserStorage {
  constructor() {
    this.store = [];
  }

  add(name, token) {
    this.store.push({ name, token })
  }

  check(name, token) {
    return this.store.filter(i => i.name == name && i.token == token).length > 0
  }
}

const userStorage = new UserStorage();

export const createUser = (name, token) => userStorage.add(name, token);

export const checkUser = (name, token) => userStorage.check(name, token);
