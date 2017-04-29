<template>
  <div class="page">
    <div class="title">Audio View</div>
    <div class="sub-title">
      chat with {{peername}}
    </div>
    <div class="status">
      <div class="icon"><ui-icon>call</ui-icon></div>
      <div>{{status}}</div>
    </div>
    <div class="actions">
      <ui-button type="secondary">
        <ui-icon>call_end</ui-icon>
      </ui-button>
    </div>
  </div>
</template>
<script>
export default {
  data() {
    return {
      peername: 'Jack Q',
      status: 'connecting...',
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

</style>
