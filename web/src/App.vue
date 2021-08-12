  
<template>
  <Menubar />
  <Sidebar />
  <router-view />
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
</style>
