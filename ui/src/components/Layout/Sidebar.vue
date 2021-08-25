  
<template>
  <div class="sidebar">
    <Link to="/">Nodes</Link>
    <Link to="/inventory">Inventory</Link>
  </div>
</template>
    
<script setup lang="ts">
  import Link from './Link.vue'
</script>
    
<style lang="scss">
$menuColor: #0a0c1b;
$linkColor: #14d1df;
.sidebar {
  height: 100vh;
  width: 230px;
  background: grey;
  position: relative;
  float: left;
  background: $menuColor;
  color: rgba($color: $linkColor, $alpha: 0.8);
  z-index: 1;
}
</style>
  