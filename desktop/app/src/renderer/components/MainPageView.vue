<template>
  <div class="wrapper">
    <div class="left-aside">
      <div class="left-header">Messenger</div>
      <div class="left-status">
        connected to {{serverName}}
      </div>
      <div class="left-center">
        <div class="left-buddy-list">
          
        </div>
      </div>
      <div>
        <button @click="centerPageIndex++">change</button>
      </div>
      <div class="left-footer">
        2017 &copy; Jack Q
      </div>
    </div>
    <div class="center">
      <transition name="fade">
        <div key="instruction" class="center-page" v-if="centerPageIndex % 4 === 0">
          <instruction-view></instruction-view>
        </div>
        <div key="audio" class="center-page" v-if="centerPageIndex % 4 === 1">Audio View</div>
        <div key="message" class="center-page" v-if="centerPageIndex % 4 === 2">Message View</div>
        <div key="setting" class="center-page" v-if="centerPageIndex % 4 === 3">Setting</div>
      </transition>
    </div>
    <div class="right-aside">
      <div class="right"></div>
    </div>
  </div>
</template>

<script>
import InstructionView from './InstructionView';
export default {
  data() {
    return {
      serverName: 'Server',
      centerPageIndex: 0,
    };
  },
  components: {
    InstructionView,
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
</style>
