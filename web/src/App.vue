  
<template>
  <Menubar />
  <Sidebar />
  <div id="view">
    <router-view />
  </div>
</template>
  
<script setup lang="ts">
  import Menubar from './components/Layout/Menubar.vue'
  import Sidebar from './components/Layout/Sidebar.vue'
</script>
  
<style lang="scss">
body {
  margin: 0px;
}
input.p-dropdown-label {
  cursor: auto !important;
}
#view {
  padding: 15px;
  margin-left: 230px;
}
</style>
