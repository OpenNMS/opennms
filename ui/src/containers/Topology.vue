<template>
  <NetworkGraph v-if="displayGraph" />
  <SideControls :refreshGraph="refreshGraph" />
</template>

<script setup lang="ts">
import NetworkGraph from '@/components/Topology/NetworkGraph.vue'
import SideControls from '@/components/Topology/SideControls.vue'
import { ref, nextTick } from 'vue'

const displayGraph = ref(true)

const refreshGraph = async () => {
  displayGraph.value = false
  await nextTick()
  displayGraph.value = true
}
</script>

