<template>
  <NetworkGraph v-if="displayGraph" :refresh="refreshGraph" />
  <Teleport to="body">
    <SideControls :refreshGraph="refreshGraph" />
  </Teleport>
</template>

<script setup lang="ts">
import NetworkGraph from '@/components/Topology/NetworkGraph.vue'
import SideControls from '@/components/Topology/SideControls.vue'
import { ref, nextTick } from 'vue'
import { useStore } from 'vuex'

const store = useStore()
const displayGraph = ref(true)

const refreshGraph = async () => {
  displayGraph.value = false
  await nextTick()
  displayGraph.value = true
}

onMounted(() => store.dispatch('topologyModule/getTopologyGraphs'))
</script>

