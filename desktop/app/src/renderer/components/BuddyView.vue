<template>
  <div class="buddy" ref="buddy-root">
    <div class="name">{{buddy.name}}</div>
    <div class="ip">{{buddy.ip}}</div>
    <div class="red-dot" :class="unreadItem > 0 || 'empty'">{{unreadItem || 0}}</div>
    <ui-ripple-ink trigger="buddy-root" ></ui-ripple-ink> 
  </div>
</template>

<script>
export default {
  props: {
    buddy: {
      type: Object,
      required: true,
    },
    unreadItem: {
      type: Number,
      required: false,
    },
  },
};
</script>

<style>
.buddy{
  text-align: center;
  height: 60px;
  padding: 10px;
  transition: all ease 400ms;
  cursor: pointer;
  margin: 0;
  position: relative;
}
.buddy:hover {
  background: rgba(150, 150, 150, 0.3);
}
.name{
  font-size: 1.5em;
  height: 40px;
  line-height: 40px;
}
.ip{
  color: #999;
  height: 20px;
  line-height: 20px;
}
.buddy + .buddy {
  border-top: dotted 1px #888;
}

.red-dot {
  position: absolute;
  right: 20px;
  top: 30px;
  height: 20px;
  width: 20px;
  line-height: 20px;
  background: #c55;
  border-radius: 5px;
  transition: all ease 400ms;
  opacity: 1;
}

.red-dot.empty {
  opacity: 0;
}
</style>
