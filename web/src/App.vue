  
<template>
  <div class="menubar">
    <div>
      <img 
        @click="returnHomeHandler"
        :src="Logo" 
        style="width: 8em; height: 2em; margin-left: 10px; cursor: pointer;"
      />
    </div>
    <div>
      <Search />
    </div>
    <div>
      <Button @click="returnHandler" class="p-button-md p-button-primary">Return to previous UI</Button>
    </div>
  </div>
  <div class="sidebar">
    <Link to="/">Nodes</Link>
  </div>
  <router-view />
</template>
  
<script setup lang="ts">
  import Button from 'primevue/button'
  import Logo from './assets/OpenNMS_Horizontal-Logo_Dark-BG.svg'
  import Link from './components/Layout/Link.vue'
  import Search from './components/Layout/Search.vue'
  import { useRouter } from 'vue-router'

  const router = useRouter()
  const returnHandler = () => window.location.href = '/opennms/'
  const returnHomeHandler = () => router.push('/')
</script>
  
<style lang="scss">
body {
  margin: 0px;
}

$linkColor: #14d1df;
$menuColor: #0a0c1b;
.menubar {
  display: flex;
  justify-content: space-between;
  background: $menuColor;
  height: 65px;
  padding: 12px;
}
.sidebar {
  height: 100vh;
  width: 230px;
  background: grey;
  position: relative;
  float: left;
  background: $menuColor;
  color: rgba($color: $linkColor, $alpha: 0.8);
}
input.p-dropdown-label {
  cursor: auto !important;
}
</style>
