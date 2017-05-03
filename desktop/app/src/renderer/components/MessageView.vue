<template>
  <div class="page">
    <div class="title">Message</div>
    <div class="sub-title">with {{peername}}</div>
    <transition-group class="message-list" name="list">
      <div v-for="msg in getMessages()" :key="msg.time" class="message-row" :class="msg.type">
        <div class="message-box" :class="msg.type">
          <div class="content">{{msg.content}}</div>
          <div class="footer">{{msg.time | time }}</div>
        </div>
      </div>
    </transition-group>
    <form class="editor" @submit.prevent="sendMessage">
      <div class="message-text-edit">
        <ui-textbox type="text" v-model.trim="currentMessage"></ui-textbox>
      </div>
      <div>
        <ui-button :type="currentMessage?'primary':'secondary'" :disabled="!currentMessage">send</ui-button>
      </div>
      <div class="editor-seperator"></div>
      <div>
        <ui-button type="secondary" buttonType="button" @click="initAudio">Audio chat</ui-button>
      </div>
    </form>
  </div>
</template>
<script>
import AppState from '../app-state';

export default {
  props: {
    peername: {
      type: String,
    },
    messageId: {
      type: String,
    },
  },
  created() {
    AppState.onUpdate(() => this.$forceUpdate());
  },
  methods: {
    getMessages() {
      return AppState.messageList[this.peername] || [];
    },
    sendMessage() {
      AppState.sendMessage(this.messageId, this.currentMessage);
      this.currentMessage = '';
    },
    initAudio() {
      AppState.requestCall(this.messageId);
    },
  },
  filters: {
    time: d => `${d.getFullYear()}-${d.getMonth()}-${d.getDate()} ${d.getHours()}:${d.getMinutes()}:${d.getSeconds()}`,
  },
  data() {
    return {
      currentMessage: '',
      messages: [],
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
  color: #999;
  width: 100%;
  height: 100%;
  justify-content: center;
}

.title {
  font-size: 2em;
  margin: 30px;
}

.sub-title {
  color: #888;
  margin: -30px auto 20px;
}

.message-list {
  width: 100%;
  height: 65%;
  max-height: 450px;
  max-width: 600px;
  margin: 0 auto;
  overflow: auto;
  border-top: dashed 1px #aaa;
  border-bottom: dashed 1px #aaa;
}

.message-row{
  margin: 20px 0;
  padding: 0 30px;
  display: flex;
}

.message-box{
  background: rgba(240,240,240,0.3);
  width: 60%;
  max-width: 500px;
  font-size: 0.8em;
  border-radius: 10px;
  text-align: left;
  padding: 10px 20px;
  color: #555;
}
.message-row.recv::before, .message-row.send::after{
  content: '';
  display: block;
  width: 8px;
  height: 8px;
  margin: auto 2px;
  background: rgba(240,240,240,0.5);
  border-radius: 50%;
}
.message-box.sys{
  text-align: center;
  margin: auto;
  font-size: 0.7em;
  padding: 5px 10px 10px;
  background: rgba(220,225,225,0.3);
}
.message-box.send{
  margin-left: auto;
  text-align: right;
}

.message-box .footer{
  margin-bottom: -10px;
  font-size: 0.7em;
  text-align: center;
  color: #999;
}
.editor{
  width: 100%;
  height: 80px;
  max-width: 600px;
  margin: 5px auto;
  display: flex;
}
.message-text-edit{
  flex: 1;
}
</style>
