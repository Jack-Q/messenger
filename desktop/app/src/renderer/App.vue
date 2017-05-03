<template>
  <div id="app">
    <router-view></router-view>
  </div>
</template>

<script>
</script>

<style>
  @import url(./resource/font.css);

  ::-webkit-scrollbar {
    background: transparent;
    width: 8px;
    height: 8px;
    overflow: visible; 
  }

  ::-webkit-scrollbar-corner{
    background: transparent;
  }
  
  ::-webkit-scrollbar-thumb {
    border-radius: 4px;
    background-color:rgba(220,220,220,0.5);
    border: solid 1px rgba(0,0,0,0.3);
  }


  * {
    margin: 0;
    padding: 0;
  }

  html,
  body { 
    height: 100%;
    background: transparent;
  }

  body {
    align-items: center;
    background-position: center;
    display: flex;
    font-family: Lato, Helvetica, sans-serif;
    justify-content: center;
    /*disable selection by default*/
    user-select: none;
    cursor: default;
    overflow: hidden;
  }

  #app{
    color: #eee;
    background: #555;
    width: 100%;
    height: 100%;
    border-radius: 10px;
    overflow: hidden;
  }
</style>
