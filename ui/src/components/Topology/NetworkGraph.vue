<template>
  <VNetworkGraph
    :layouts="layout"
    :nodes="verticies"
    :edges="edges"
    :configs="configs"
    :zoomLevel="zoomLevel"
    :eventHandlers="eventHandlers"
    v-if="trigger"
  />
  <ContextMenu
    ref="contextMenu"
    v-if="showContextMenu"
    :x="menuXPos"
    :y="menuYPos"
    :nodeId="contextNodeId"
    :closeContextMenu="closeContextMenu"
  />
</template>

<script setup lang="ts">
import 'v-network-graph/lib/style.css'
import { computed, reactive, watch, ref, nextTick } from 'vue'
import { useStore } from 'vuex'
import { VNetworkGraph, defineConfigs, Layouts, Edges, Nodes, SimpleLayout, EventHandlers, NodeEvent } from 'v-network-graph'
import { ForceLayout, ForceNodeDatum, ForceEdgeDatum } from 'v-network-graph/lib/force-layout'
import ContextMenu from './ContextMenu.vue'
import { onClickOutside } from '@vueuse/core'

const store = useStore()
const zoomLevel = ref(1)

const contextMenu = ref(null) 
const showContextMenu = ref(false)
const contextNodeId = ref()
const menuXPos = ref(0)
const menuYPos = ref(0)
const closeContextMenu = () => showContextMenu.value = false
onClickOutside(contextMenu, () => closeContextMenu())

const verticies = computed<Nodes>(() => store.state.topologyModule.verticies)
const edges = computed<Edges>(() => store.state.topologyModule.edges)
const layout = computed<Layouts>(() => store.getters['topologyModule/getLayout'])

const eventHandlers: EventHandlers = {
  'node:contextmenu': ({ node, event }: NodeEvent<any>) => {
    event.preventDefault()
    contextNodeId.value = node
    menuXPos.value = event.layerX
    menuYPos.value = event.layerY
    showContextMenu.value = true
  }
}

const forceLayout = new ForceLayout({
  positionFixedByDrag: true,
  createSimulation: (d3, nodes, edges) => {
    const forceLink = d3.forceLink<ForceNodeDatum, ForceEdgeDatum>(edges).id(d => d.id)
    return d3
      .forceSimulation(nodes)
      .force('edge', forceLink.distance(100))
      .force('charge', d3.forceManyBody().distanceMax(300))
      .force('collide', d3.forceCollide(10))
      .force('center', d3.forceCenter(0, 0))
      .force('x', d3.forceX(0).strength(0.01))
      .force('y', d3.forceY(0).strength(0.01))
      .alphaMin(0.001)
  }
})

const d3ForceEnabled = computed({
  get: () => configs.view.layoutHandler instanceof ForceLayout,
  set: (value: boolean) => {
    if (value) {
      configs.view.layoutHandler = forceLayout
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
      layoutHandler: store.state.topologyModule.selectedView === 'd3' ? forceLayout : new SimpleLayout()
    }
  })
)
</script>
