<template>
  <div class="page">
    <div class="title">Server &amp; Account</div>
    <div class="form">
      <div class="form-row">
        <ui-textbox floating-label label="Server Address" placeholder="Enter server address" v-model="host"></ui-textbox>
        <span>:</span>
        <ui-textbox floating-label label="Port" type="number" :min="1024" step="1" :max="65535" placeholder="Enter your name" v-model="port"></ui-textbox>
      </div>
      <ui-textbox floating-label label="User name" placeholder="Enter your user name" v-model="username"></ui-textbox>
      <ui-textbox floating-label label="Password" placeholder="Enter your password" v-model="password" type="password"></ui-textbox>
      <ui-button type="secondary" :loading="connecting">connect</ui-button>
    </div>
  </div>
</template>
<script>
export default {
  data() {
    return {
      host: '',
      port: '',
      username: '',
      password: '',
      connecting: false,
    };
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

.form-row span {
  color: #555;
  margin: 10px;
}
</style>
