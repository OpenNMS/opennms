  
<template>
  <div class="sidebar">
    <Link to="/">Nodes</Link>
    <Link to="/provisionDConfig">Configuration</Link>
    <Link v-if="isAdmin" to="/file-editor">File Editor</Link>
    <Link v-if="isAdmin" to="/logs">Logs</Link>
  </div>
</template>
    
<script setup lang="ts">
import Link from './Link.vue'
import { computed } from 'vue'
import { useStore } from 'vuex'

const store = useStore()
const isAdmin = computed(() => store.getters['authModule/isAdmin'])
</script>
    
<style lang="scss">
.sidebar {
  height: calc(100vh - 60px);
  width: 230px;
  position: relative;
  float: left;
  background: var(--feather-secondary);
  z-index: 1;

  .link a:hover{
    text-decoration: none;
  }
}

</style>
  