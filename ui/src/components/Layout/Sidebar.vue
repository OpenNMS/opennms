  
<template>
  <div class="sidebar">
    <Link to="/">Nodes</Link>
    <Link to="/provisionDConfig">Configuration</Link>
  </div>
</template>
    
<script setup lang="ts">
import Link from './Link.vue'
</script>
    
<style lang="scss">
.sidebar {
  height: 100vh;
  width: 230px;
  position: relative;
  float: left;
  background: var(--feather-secondary);
  z-index: 1;
}
</style>
  