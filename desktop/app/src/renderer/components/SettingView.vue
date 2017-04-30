<template>
  <div class="page">
    <div class="title">Server &amp; Account</div>
    <form class="form" @submit="connect">
      <div class="form-row">
        <div class="host">
          <ui-textbox required :disabled="connecting" floating-label label="Server Address" placeholder="Enter server address" v-model="host"></ui-textbox>
        </div>
        <span>:</span>
        <div class="port">
          <ui-textbox required :disabled="connecting" floating-label label="Port" type="number" :min="1024" step="1" :max="65535" placeholder="Enter your name" v-model="port"></ui-textbox>
        </div>
      </div>
      <ui-textbox required :disabled="connecting" floating-label label="User name" placeholder="Enter your user name" v-model="username"></ui-textbox>
      <ui-textbox required :disabled="connecting" floating-label label="Password" placeholder="Enter your password" v-model="password" type="password"></ui-textbox>
      <ui-button type="secondary" :loading="connecting">connect</ui-button>
    </form>
  </div>
</template>
<script>
import AppState from '../app-state';
import UiService from '../ui-service';

export default {
  data() {
    return {
      created() {
        AppState.onUpdate(() => this.$forceUpdate());
      },
      host: '0.0.0.0',
      port: 12121,
      username: 'jack',
      password: 'jack',
      connecting: false,
    };
  },
  created() {
    AppState.onUpdate(() => this.$forceUpdate());
  },
  methods: {
    connect() {
      this.connecting = true;
      AppState.connect(this.host, this.port).catch(e => {
        UiService.sendNotification('Network Error', (e && e.message) || 'Something went wrong...');
        this.connecting = false;
      })
      .then(e => {
        UiService.sendNotification('Success', `connected to server ${e}`);
        return AppState.login(this.username, this.password);
      })
      .catch(e => {
        UiService.sendNotification('Login Error', e && e.message);
        this.connecting = false;
      })
      .then(info => {
        UiService.sendNotification(`Welcome ${info.name}`, 'welcome to messenger');
        this.connecting = false;
      });
    },
  },
};
</script>
<style>
.page {
  display: flex;
  flex-direction: column;
  font-size: 1.5em;
  text-align: center;
  color: #aaa;
  width: 100%;
  height: 100%;
  justify-content: center;
}

.title {
  font-size: 2em;
  margin: 30px;
}

.form {
  width: 40%;
  margin: 0 auto;
}

.form-row {
  display: flex;
  align-items: center;
}

.host {
  flex: 4;
}

.port {
  flex: 1;
}

.form-row span {
  color: #555;
  margin: 10px;
}
</style>
