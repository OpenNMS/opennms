<template>
  <VNetworkGraph :nodes="verticies" :edges="edges" />
</template>

<script setup lang="ts">
import 'v-network-graph/lib/style.css'
import { computed } from 'vue'
import { VNetworkGraph } from 'v-network-graph'
import { useStore } from 'vuex'
import { NetworkGraphEdge, NetworkGraphVertex } from '@/types/topology'

const store = useStore()

const verticies = computed<Record<string, NetworkGraphVertex>>(() => store.state.topologyModule.verticies)
const edges = computed<Record<string, NetworkGraphEdge>>(() => store.state.topologyModule.edges)
</script>
