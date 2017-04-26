<template>
  <div class="buddy">
    <div class="name">{{buddy.name}}</div>
    <div class="ip">{{buddy.ip}}</div>
  </div>
</template>

<script>
export default {
  props: {
    buddy: {
      type: Object,
      required: true,
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
</style>
