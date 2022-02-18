<template>
  <div class="container">
    <NetworkGraph class="graph" />
    <SideControls />
  </div>
</template>

<script setup lang="ts">
import NetworkGraph from '@/components/Topology/NetworkGraph.vue'
import SideControls from '@/components/Topology/SideControls.vue'
</script>

<style scoped lang="scss">
.container {
  display: flex;
  flex-direction: row;
}
</style>
