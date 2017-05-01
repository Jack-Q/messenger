<template>
  <div class="page">
    <div class="title">Audio View</div>
    <div class="sub-title">
      chat with {{getPeerName()}}
    </div>
    <div class="status">
      <div class="icon"><ui-icon>call</ui-icon></div>
      <div>{{getStatus()}}</div>
    </div>
    <div class="actions">
      <div class="action-answer">
        <ui-button type="secondary" @click="answerCall()">
          <ui-icon>call</ui-icon>
        </ui-button>
      </div>
      <div class="action-end">
        <ui-button type="secondary" @click="endCall()">
          <ui-icon>call_end</ui-icon>
        </ui-button>
      </div>
    </div>
  </div>
</template>
<script>
import AppState from '../app-state';

export default {
  created() {
    AppState.onUpdate(() => this.$forceUpdate());
  },
  data() {
    return {
      status: 'connecting...',
    };
  },
  methods: {
    answerCall: () => AppState.answerCall(),
    endCall: () => AppState.terminateCall(),
    getPeerName: () => AppState.audioCall.peername,
    getStatus: () => AppState.audioCall.status,
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

.status {
  width: 100%;
  max-width: 450px;
  border-top: dashed 1px #aaa;
  border-bottom: dashed 1px #aaa;
  margin: 10px auto;
}
.status .ui-icon{
  font-size: 128px;
  margin: 30px;
}

.actions {
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-answer .ui-icon{
  color: #5a5;
}

.action-end .ui-icon{
  color: #a55;
}

</style>
