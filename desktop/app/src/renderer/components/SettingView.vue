<template>
  <div class="page">
    <div class="title">Server &amp; Account</div>
    <form class="form" @submit.prevent="connect">
      <div class="form-row">
        <div class="host">
          <ui-textbox required :disabled="connecting" floating-label label="Server Address" placeholder="Enter server address" v-model.trim="host"></ui-textbox>
        </div>
        <span>:</span>
        <div class="port">
          <ui-textbox required :disabled="connecting" floating-label label="Port" type="number" :min="1024" step="1" :max="65535" placeholder="Enter your name" v-model="port"></ui-textbox>
        </div>
      </div>
      <ui-textbox required :disabled="connecting" floating-label label="User name" placeholder="Enter your user name" v-model.trim="username"></ui-textbox>
      <ui-textbox required :disabled="connecting" floating-label label="Password" placeholder="Enter your password" v-model.trim="password" type="password"></ui-textbox>
      <div ref="createNewUser" style="position: relative">
        <ui-checkbox v-model="createNewUser">
          Create new user
        </ui-checkbox>
      </div>
      <ui-tooltip trigger="createNewUser">
        check this option to create a new user. after which you can reserve this username and login with the password
      </ui-tooltip>
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
      createNewUser: false,
      connecting: false,
    };
  },
  created() {
    AppState.onUpdate(() => this.$forceUpdate());
  },
  methods: {
    connect() {
      this.connecting = true;
      AppState.connect(this.host, this.port)
        .catch((e) => {
          if (e) {
            UiService.sendNotification('Network Error',
              (e && e.message) || 'Something went wrong...');
            this.connecting = false;
          }
          return Promise.reject(false);
        })
        .then((e) => {
          console.log(e);
          return this.createNewUser ? AppState.register(this.username, this.password)
            : AppState.login(this.username, this.password);
        })
        .then((info) => {
          UiService.sendNotification(`Welcome ${info.name}`, 'welcome to messenger');
          this.connecting = false;
          this.createNewUser = false;
        })
        .catch((e) => {
          if (e) {
            UiService.sendNotification('Login Error', e && e.message);
            this.connecting = false;
          }
          return Promise.reject(false);
        })
        .catch(e => console.log(e));
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
  min-width: 240px;
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
  min-width: 55px;
}

.form-row span {
  color: #555;
  margin: 10px;
}




/* change keen ui element style */

.form .ui-checkbox__checkmark {
  background-color: transparent;
}
</style>
