<template>
  <div class="tooltip-wrapper">
    <VNetworkGraph
      ref="graph"
      v-model:selectedNodes="selectedNodes"
      :layouts="layout"
      :nodes="verticies"
      :edges="edges"
      :configs="configs"
      :zoomLevel="zoomLevel"
      :eventHandlers="eventHandlers"
      v-if="trigger && focusedNodeIds.length !== 0"
    />
    <!-- Tooltip -->
    <div ref="tooltip" class="tooltip" :style="{ ...tooltipPos, display: tooltipDisplay }">
      <div v-html="verticies[targetNodeId]?.tooltip ?? ''"></div>
    </div>
  </div>
  <NoFocusMsg :useDefaultFocus="useDefaultFocus" v-if="focusedNodeIds.length === 0" />
  <ContextMenu
    ref="contextMenu"
    v-if="showContextMenu"
    :refresh="refresh"
    :contextMenuType="contextMenuType"
    :x="menuXPos"
    :y="menuYPos"
    :nodeId="contextNodeId"
    :selectedNodeObjects="selectedNodeObjects"
    :selectedNodes="selectedNodes"
    :groupClick="groupClick"
    :closeContextMenu="closeContextMenu"
  />
</template>

<script setup lang="ts">
import 'v-network-graph/lib/style.css'
import { useStore } from 'vuex'
import { VNetworkGraph, defineConfigs, Layouts, Edges, Nodes, SimpleLayout, EventHandlers, NodeEvent, Instance, ViewEvent, Node, Edge } from 'v-network-graph'
import { ForceLayout, ForceNodeDatum, ForceEdgeDatum } from 'v-network-graph/lib/force-layout'
import ContextMenu from './ContextMenu.vue'
import NoFocusMsg from './NoFocusMsg.vue'
import { onClickOutside } from '@vueuse/core'
import { SimulationNodeDatum } from 'd3'
import { ContextMenuType } from './topology.constants'
import { useFocus } from './composables'

interface d3Node extends Required<SimulationNodeDatum> {
  id: string
}

defineProps({
  refresh: {
    type: Function,
    required: true
  }
})

const store = useStore()
const { setContextNodeAsFocus } = useFocus()
const zoomLevel = ref(1)
const graph = ref<Instance>()
const selectedNodes = ref<string[]>([]) // string ids
const selectedNodeObjects = ref<Node>([]) // full nodes
const tooltip = ref<HTMLDivElement>()
const tooltipDisplay = ref('none')
const cancelTooltipDebounce = ref(false)
const targetNodeId = ref('')
const d3Nodes = ref<d3Node[]>([])
const contextMenu = ref(null)
const showContextMenu = ref(false)
const contextNodeId = ref()
const contextMenuType = ref()
const menuXPos = ref(0)
const menuYPos = ref(0)
const groupClick = ref(false)

const getD3NodeCoords = () => d3Nodes.value.filter((d3Node) => d3Node.id === targetNodeId.value).map((d3Node) => ({ x: d3Node.x, y: d3Node.y }))[0]
const closeContextMenu = () => showContextMenu.value = false
onClickOutside(contextMenu, () => closeContextMenu())

const verticies = computed<Nodes>(() => store.state.topologyModule.verticies)
const edges = computed<Edges>(() => store.state.topologyModule.edges)
const layout = computed<Layouts>(() => store.getters['topologyModule/getLayout'])
const defaultNode = computed<Node>(() => store.state.topologyModule.defaultNode)
const focusedNodeIds = computed<string[]>(() => store.state.topologyModule.focusedNodeIds)
const highlightFocusedNodes = computed<boolean>(() => store.state.topologyModule.highlightFocusedNodes)

const tooltipPos = computed(() => {
  if (!graph.value || !tooltip.value) return { x: 0, y: 0 }
  if (!targetNodeId.value) return { x: 0, y: 0 }

  // attempt to get the node position from the layout. If layout is d3, use the function
  const nodePos = layout.value.nodes ? layout.value.nodes[targetNodeId.value] : getD3NodeCoords()
  if (!nodePos) return { x: 0, y: 0 }

  // translate coordinates: SVG -> DOM
  const domPoint = graph.value.translateFromSvgToDomCoordinates(nodePos)

  return {
    left: domPoint.x - 120 + 'px',
    top: domPoint.y - 130 + 'px',
  }
})

const eventHandlers: EventHandlers = {
  // on right clicking background
  'view:contextmenu': ({ event }: ViewEvent<any>) => {
    event.preventDefault()
    contextMenuType.value = ContextMenuType.background
    menuXPos.value = event.layerX
    menuYPos.value = event.layerY
    showContextMenu.value = true
  },
  // on right clicking node
  'node:contextmenu': ({ node, event }: NodeEvent<any>) => {
    event.preventDefault()

    // if right clicking on a selected group of nodes
    if (selectedNodes.value.length > 1 && selectedNodes.value.includes(node)) {
      groupClick.value = true
      getNodesFromSelectedIds()
    } else {
      groupClick.value = false
      selectedNodeObjects.value = []
    }

    contextMenuType.value = ContextMenuType.node
    contextNodeId.value = node
    menuXPos.value = event.layerX
    menuYPos.value = event.layerY
    showContextMenu.value = true
  },
  // on hover, display tooltip
  'node:pointerover': ({ node }: NodeEvent<any>) => {
    cancelTooltipDebounce.value = false
    targetNodeId.value = node

    const showTooltip = useDebounceFn(() => {
      if (!cancelTooltipDebounce.value) {
        tooltipDisplay.value = 'block' // show
      }
    }, 1000)

    showTooltip()
  },
  'node:pointerout': () => {
    cancelTooltipDebounce.value = true
    tooltipDisplay.value = 'none' // hide
  }
}

const getNodesFromSelectedIds = () => {
  selectedNodeObjects.value = selectedNodes.value.map((nodeId) => {
    return verticies.value[nodeId]
  })
}

const forceLayout = new ForceLayout({
  positionFixedByDrag: true,
  createSimulation: (d3, nodes, edges) => {
    const forceLink = d3.forceLink<ForceNodeDatum, ForceEdgeDatum>(edges).id(d => d.id)
    const force = d3
      .forceSimulation(nodes)
      .force('edge', forceLink.distance(100))
      .force('charge', d3.forceManyBody().distanceMax(300))
      .force('collide', d3.forceCollide(10))
      .force('center', d3.forceCenter(0, 0))
      .force('x', d3.forceX(0).strength(0.01))
      .force('y', d3.forceY(0).strength(0.01))
      .alphaMin(0.001)

    d3Nodes.value = force.nodes() as d3Node[]

    return force
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

const setNodeColor = (node: Node) => {
  if (highlightFocusedNodes.value && !node.focused) {
    return 'rgb(39, 49, 128, 0.5)'
  }

  return 'rgb(39, 49, 128)' // feather primary
}

const setEdgeColor = (edge: Edge) => {
  if (highlightFocusedNodes.value && !edge.focused) {
    return 'rgb(39, 49, 128, 0.5)'
  }

  return 'rgb(39, 49, 128)' // feather primary
}

const configs = reactive(
  defineConfigs({
    view: {
      layoutHandler: store.state.topologyModule.selectedView === 'd3' ? forceLayout : new SimpleLayout()
    },
    node: {
      selectable: true,
      normal: {
        type: 'circle',
        color: node => setNodeColor(node),
      },
    },
    edge: {
      normal: {
        color: edge => setEdgeColor(edge)
      }
    }
  })
)

// sets the default focused node
const useDefaultFocus = () => {
  if (defaultNode.value) {
    setContextNodeAsFocus(defaultNode.value)
  }
}
onMounted(() => useDefaultFocus())
</script>

<style lang="scss" scoped>
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";

.tooltip-wrapper {
  position: relative;
  display: contents;

  .tooltip {
    @include elevation(2);
    @include subtitle1;
    top: 0;
    left: 0;
    display: "none";
    position: absolute;
    width: auto;
    min-width: 100px;
    height: auto;
    min-height: 30px;
    padding: 10px;
    text-align: center;
    font-size: 12px;
    background-color: var($surface);
    border: 1px solid var($primary);
  }
}
</style>
