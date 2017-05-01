<template>
  <div class="page">
    <div class="title">Audio View</div>
    <div class="sub-title">
      chat with {{getPeerName()}}
    </div>
    <div class="status">
      <div class="icon">
        <transition name="fade">
          <div v-if="getPhase()===0" class="icon-item icon-connecting" key="connecting">
            <ui-icon>settings_ethernet</ui-icon>
          </div>
          <div v-if="getPhase()===1" class="icon-item icon-waiting" key="waiting">
            <ui-icon>ring_volume</ui-icon>
          </div>
          <div v-if="getPhase()===2" class="icon-item icon-chatting" key="chatting">
            <ui-icon>call</ui-icon>
          </div>
          <div v-if="getPhase()===3" class="icon-item icon-end" key="end">
            <ui-icon>call_end</ui-icon>
          </div>
        </transition>
      </div>
      <div>{{getStatus()}}</div>
    </div>
    <div class="actions">
      <div class="action-answer" v-if="getAnswerMode()">
        <ui-button type="secondary" @click="answerCall()">
          <ui-icon>call</ui-icon>
        </ui-button>
      </div>
      <div class="action-end" v-if="getTerminateMode()">
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
    return {};
  },
  methods: {
    answerCall: () => AppState.answerCall(),
    endCall: () => AppState.terminateCall(),
    getPeerName: () => AppState.audioCall.peerName,
    getStatus: () => AppState.audioCall.status || '...',
    getAnswerMode: () => AppState.audioCall.answerMode,
    getPhase: () => AppState.audioCall.phase,
    getTerminateMode: () => AppState.audioCall.terminateMode,
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

.status .icon{
  height: 230px;
  display: flex;
  flex-direction: row;
  justify-content: center;
  position: relative;
}

.status .icon-item {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  transition: all ease 400ms;
}

@keyframes icon-shaking {
  35%{transform: rotate(0)}
  40%{transform: rotate(-20deg)}
  50%{transform: rotate(22deg)}
  60%{transform: rotate(-25deg)}
  70%{transform: rotate(22deg)}
  80%{transform: rotate(-20deg)}
  85%{transform: rotate(0)}
}

.status .icon-waiting {
  animation: infinite 1.5s linear icon-shaking;
}

@keyframes icon-floating {
  0%{transform: translateY(0)}
  25%{transform: translateY(-5%)}
  75%{transform: translateY(5%)}
  0%{transform: translateY(0)}
}

.status .icon-chatting {
  animation: infinite 2s linear icon-floating;
}

/* alternating item animation */

.fade-enter-active,
.fade-leave {
  opacity: 1;
  transform: translateY(0);
}

.fade-leave {
  pointer-events: none;
}

.fade-enter,
.fade-leave-active {
  transform: translateY(50px);
  opacity: 0;
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
