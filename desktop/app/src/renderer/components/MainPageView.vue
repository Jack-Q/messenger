<template>
  <div class="wrapper">
    <div class="left-aside">
      <div class="left-header">
        <div class="left-header-close" @click="closeWindow">x</div>
        <span>Messenger</span>
      </div>
      <div class="left-status">
        <span v-if="isLogin()"> {{getUsername()}} @ {{getServer()}} </span>
        <span v-else-if="isConnected()"> connected to {{getServer()}} </span>
        <span v-else>not connected</span>
        <div class="left-disconnect" v-if="isConnected()" @click="closeConnection()">
          <span v-if="isLogin()">log out</span>
          <span v-else-if="isConnected()">disconnect</span>
        </div>
      </div>
      <div class="left-center">
        <transition name="fade">
          <div key="left-no-server" v-if="!isConnected()" class="left-center-page">
            <div class="left-center-tip">
              <ui-icon>settings_ethernet</ui-icon>
              maybe an connection to server is required...
            </div>
          </div>
          <div key="left-no-login" v-else-if="!isLogin()" class="left-center-page">
            <div class="left-center-tip">
              <ui-icon>verified_user</ui-icon>
              maybe you need an identity...
            </div>
          </div>
          <div key="left-on-audio" v-else-if="isAudioMode()" class="left-center-page">
            <div class="left-center-tip">
              <ui-icon>keyboard_voice</ui-icon>
              chating...
            </div>
          </div>
          <div key="left-buddy-list" v-else-if="getBuddyList() && getBuddyList().length" class="left-center-page">
            <transition-group class="left-buddy-list" name="list">
              <buddy-view v-for="buddy in getBuddyList()" :key="buddy.id" :buddy="buddy" @click.native="clickBuddy(buddy)"></buddy-view>
            </transition-group>
          </div>
          <div key="left-null-list" v-else class="left-center-page">
            <div class="left-center-tip">
              <ui-icon>face</ui-icon>
              you're some kind of alone...
            </div>
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
          <message-view :peername="messagePeer.name" :messageId="messagePeer.id"></message-view>
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
const createBuddy = (function* c() {
  for (let i = 0; ; i++) {
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
      messageOpen: false,
      messagePeer: null,
    };
  },
  created() {
    AppState.onUpdate(() => this.$forceUpdate());
  },
  methods: {
    closeWindow() {
      window.close();
    },
    clickBuddy(buddy) {
      if (this.messageOpen) {
        this.messageOpen = false;
        setTimeout(() => this.clickBuddy(buddy), 400);
        return;
      }
      this.messageOpen = true;
      this.messagePeer = buddy;
    },
    closeConnection: () => AppState.resetState(),
    isConnected: () => AppState.connected,
    isAudioMode: () => AppState.isAudioMode,
    getUsername: () => AppState.username,
    getBuddyList: () => AppState.buddyList,
    isLogin: () => AppState.isLogin,
    getServer: () => AppState.serverConnection.getServerName(),
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
.wrapper {
  width: 100%;
  height: 100%;
  display: flex;
}

.left-aside {
  flex: 1;
  min-width: 350px;
  display: flex;
  flex-direction: column;
}

.left-header {
  font-size: 1.8em;
  padding: 25px;
  /*let left header aside to be dragable*/
  -webkit-app-region: drag;
  display: flex;
  position: relative;
}

.left-header-close {
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

.left-header-close:hover {
  background: #f55;
}

.left-status {
  margin: 0 30px;
  background: #aaa;
  color: #333;
  border-radius: 15px;
  text-align: center;
  position: relative;
}

.left-status .left-disconnect{
  position: absolute;
  z-index: 1;
  width: 100%;
  opacity: 0;
  top: 0;
  left: 0;
  transition: all ease 400ms;
  border-radius: 15px;
  background: #955;
  color: #eee;
  cursor: pointer;
}

.left-status:hover .left-disconnect{
  opacity: 1;
}

.left-center {
  flex: 1;
  position: relative;
}

.left-center-page {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  transition: all ease 400ms;
  display: flex;
}

.left-center-tip {
  margin: auto;
  font-size: 1.5em;
  color: #888;
  padding: 0 40px;
  text-align: center;
}

.left-center-tip .ui-icon {
  color: #777;
  font-size: 3em;
  display: block;
  margin: auto;
}

.left-buddy-list {
  transition: all ease 400ms;
  overflow-y: auto;
  width: 100%;
  position: relative;
  overflow-x: hidden;
}

.left-footer {
  text-align: center;
  font-size: 0.7em;
  padding: 5px;
  border-top: dashed 1px #aaa;
}

.center {
  color: #555;
  flex: 3;
  background: rgba(255, 255, 255, 0.7);
  box-shadow: 0 0 25px 5px rgba(255, 255, 255, 0.7);
  position: relative;
  overflow: hidden;
}

.center-page {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  transition: all ease 400ms;
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



/* left list animation */

.list-enter {
  opacity: 0;
  transform: translateX(-100%);
}

.list-leave-active {
  opacity: 0;
}

.list-enter-active,
.list-leave,
.list-move {
  transition: all ease 400ms;
}

.list-leave-active {
  width: 100%;
  position: absolute!important;
}
</style>
