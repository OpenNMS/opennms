  
<template>
  <div class="sidebar">
    <Link to="/">Nodes</Link>
    <Link to="/map">Map</Link>

  </div>
</template>
    
<script setup lang="ts">
import Link from './Link.vue'
</script>
    
<style lang="scss">
.sidebar {
  height: 170vh;
  width: 230px;
  position: relative;
  float: left;
  background: var(--feather-secondary);
  z-index: 1;
}
</style>
  