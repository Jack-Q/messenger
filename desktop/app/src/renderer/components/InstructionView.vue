<template>
<div class="page">
  <div class="title">Welcome {{getUsername()}}</div>
  <div>click user label on left panel to begin conversation</div>
</div>
</template>
<script>
import AppState from '../app-state';

export default {
  created() {
    AppState.onUpdate(() => this.$forceUpdate());
  },
  methods: {
    getUsername: () => AppState.username,
  },
};
</script>
<style>
.page{
  display: flex;
  flex-direction: column;  
  font-size: 1.5em;
  text-align: center;
  color: #aaa;
  width: 100%;
  height: 100%;
  justify-content: center;
}
.title{
  font-size: 2em;
  margin: 30px;
}
</style>
