<template>
  <div class="wrapper">
    <div class="left-aside">
      <div class="left-header">
        <div class="left-header-close" @click="closeWindow">x</div>
        <span>Messenger</span>
      </div>
      <div class="left-status">
        <span v-if="isConnected()"> connected to {{serverName}} </span>
        <span v-else>not connected</span>
      </div>
      <div class="left-center">
        <transition name="fade">
          <transition-group v-if="getBuddyList() && getBuddyList().length" class="left-buddy-list" name="list">
            <buddy-view v-for="buddy in getBuddyList()" :key="buddy.id" :buddy="buddy" @click.native="getBuddyList().splice(buddyList.indexOf(buddy), 1)"></buddy-view>
          </transition-group>
          <div v-else class="left-center-tip">
            <ui-icon>face</ui-icon>
            you're kind of alone...
          </div>
        </transition>
      </div>
      <div class="left-footer">
        2017 &copy; Jack Q
      </div>
    </div>
    <div class="center">
      <transition name="fade">
        <div key="setting" class="center-page" v-if="!isConnected() || !isLogin()">
          <setting-view></setting-view>
        </div>
        <div key="audio" class="center-page" v-else-if="isAudioMode()">
          <audio-view></audio-view>
        </div>
        <div key="message" class="center-page" v-else-if="messageOpen">
          <message-view></message-view>
        </div>
        <!-- default view -->
        <div key="instruction" class="center-page" v-else>
          <instruction-view></instruction-view>
        </div>
      </transition>
    </div>
  </div>
</template>

<script>
import InstructionView from './InstructionView';
import SettingView from './SettingView';
import BuddyView from './BuddyView';
import AudioView from './AudioView';
import MessageView from './MessageView';

import AppState from '../app-state';

const rnd = () => (Math.random() * 255).toFixed(0);
const createBuddy = (function *c() {
  for (let i = 0; ;i++) {
    yield {
      id: i,
      name: `Buddy ${rnd()}`,
      ip: `${rnd()}.${rnd()}.${rnd()}.${rnd()}`,
    };
  }
}());
const buddyList = (new Array(20)).fill(0).map(() => createBuddy.next().value);
console.log(buddyList);

export default {
  data() {
    return {
      serverName: 'Server',
      messageOpen: false,
    };
  },
  created() {
    AppState.onUpdate(() => this.$forceUpdate());
  },
  methods: {
    addBuddy() {
      this.buddyList.splice(Math.min(5, this.buddyList.length), 0, createBuddy.next().value);
    },
    closeWindow() {
      window.close();
    },
    isConnected: () => AppState.connected,
    isAudioMode: () => AppState.isAudioMode,
    getBuddyList: () => AppState.buddyList,
    isLogin: () => AppState.isLogin,
  },
  components: {
    InstructionView,
    SettingView,
    BuddyView,
    AudioView,
    MessageView,
  },
};
</script>

<style>

.wrapper{
  width: 100%;
  height: 100%;
  display: flex;
}
.left-aside{
  flex: 1;
  min-width: 350px;
  display: flex;
  flex-direction: column;
}
.left-header{
  font-size: 1.8em;
  padding: 25px;
  /*let left header aside to be dragable*/
  -webkit-app-region: drag;
  display: flex;
  position: relative;
}
.left-header-close{
  display: block;
  position: absolute;
  width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  border-radius: 50%;
  top: 3px;
  left: 3px;
  cursor: pointer;
  z-index: 10;
  background: #cc5555;
  transition: all ease 400ms;
  flex: 0;
  border: solid 1px #666;
  font-family: monospace;
  font-size: 18px;
  /*let left header aside to be dragable*/
  -webkit-app-region: no-drag;
}
.left-header-close:hover{
  background: #f55;
}
.left-status{
  margin: 0 30px;
  background: #aaa;
  color: #333;
  border-radius: 15px;
  text-align: center;
}
.left-center{
  flex: 1;
  display: flex;
}
.left-center-tip{
  margin: auto;
  font-size: 1.5em;
  color: #888;
}
.left-center-tip .ui-icon{
  color: #777;
  font-size: 3em;
  display: block;
  margin: auto;
}
.left-buddy-list {
  overflow-y: auto;
  width: 100%;
  position: relative;
  overflow-x: hidden;
}
.left-footer{
  text-align: center;
  font-size: 0.7em;
  padding: 5px;
  border-top: dashed 1px #aaa;
}
.center{
  color: #555;
  flex: 3;
  background: rgba(255,255,255,0.7);
  box-shadow: 0 0 25px 5px rgba(255,255,255,0.7);
  position: relative;
  overflow: hidden;
}
.center-page{
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  transition: all ease 400ms;
}

/* alternating item animation */
.fade-enter-active, .fade-leave{
  opacity: 1;
  transform: translateY(0);
}
.fade-leave{
  pointer-events: none;
}
.fade-enter, .fade-leave-active{
  transform: translateY(50px);
  opacity: 0;
}

/* left list animation */
.list-enter {
  opacity: 0;
  transform: translateX(-100%);
}
.list-leave-active {
  opacity: 0;
}
.list-enter-active, .list-leave, .list-move{
  transition: all ease 400ms;
}
.list-leave-active{
  width: 100%;
  position: absolute!important;
}
</style>
