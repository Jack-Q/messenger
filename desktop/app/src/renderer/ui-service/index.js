

export default {
  sendNotification(title, content) {
    const notification = new Notification(title, {
      body: content,
    });
    return new Promise((res, rej) => {
      notification.addEventListener('click', e => res(e));
      notification.addEventListener('close', e => res(e));
      notification.addEventListener('error', e => rej(e));
    });
  },
};
