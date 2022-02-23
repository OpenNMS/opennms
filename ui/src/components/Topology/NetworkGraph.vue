<template>
  <VNetworkGraph :layouts="layout" :nodes="verticies" :edges="edges" :configs="configs" v-if="trigger" />
</template>

<script setup lang="ts">
import 'v-network-graph/lib/style.css'
import { computed, reactive, watch, ref, nextTick } from 'vue'
import { useStore } from 'vuex'
import { VNetworkGraph, defineConfigs, Layouts, Edges, Nodes, SimpleLayout } from 'v-network-graph'
import { ForceLayout } from 'v-network-graph/lib/force-layout'

const store = useStore()

const verticies = computed<Nodes>(() => store.state.topologyModule.verticies)
const edges = computed<Edges>(() => store.state.topologyModule.edges)
const layout = computed<Layouts>(() => store.getters['topologyModule/getLayout'])

const d3ForceEnabled = computed({
  get: () => configs.view.layoutHandler instanceof ForceLayout,
  set: (value: boolean) => {
    if (value) {
      configs.view.layoutHandler = new ForceLayout()
    } else {
      configs.view.layoutHandler = new SimpleLayout()
    }
  },
})

const trigger = ref(true)

watch(layout, async (layout) => {
  if (Object.keys(layout).length === 0) {
    d3ForceEnabled.value = true
  } else {
    d3ForceEnabled.value = false
  }

  trigger.value = false
  await nextTick()
  trigger.value = true
})

const configs = reactive(
  defineConfigs({
    view: {
      layoutHandler: new ForceLayout()
    }
  })
)
</script>
