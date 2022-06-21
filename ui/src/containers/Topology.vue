<template>
  <NetworkGraph
    v-if="displayGraph"
    :refresh="refreshGraph"
  />
  <Teleport to="body">
    <SideControls :refreshGraph="refreshGraph" />
  </Teleport>
</template>

<script
  setup
  lang="ts"
>
import { useStore } from 'vuex'
import NetworkGraph from '@/components/Topology/NetworkGraph.vue'
import SideControls from '@/components/Topology/SideControls.vue'
import { DisplayType } from '@/components/Topology/topology.constants'

const store = useStore()
const displayGraph = ref(true)

const refreshGraph = async () => {
  displayGraph.value = false
  await nextTick()
  displayGraph.value = true
}

onMounted(async () => {
  await store.dispatch('topologyModule/getTopologyGraphs')
  await store.dispatch('topologyModule/setSelectedDisplay', DisplayType.nodes) // set default graph
})
</script>

