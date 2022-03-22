<template>
  <div class="tooltip-wrapper">
    <VNetworkGraph
      ref="graph"
      :layouts="layout"
      :nodes="verticies"
      :edges="edges"
      :configs="configs"
      :zoomLevel="zoomLevel"
      :eventHandlers="eventHandlers"
      v-if="trigger"
    />
    <!-- Tooltip -->
    <div ref="tooltip" class="tooltip" :style="{ ...tooltipPos, opacity: tooltipOpacity }">
      <div v-html="verticies[targetNodeId]?.tooltip ?? ''"></div>
    </div>
  </div>
  <ContextMenu
    ref="contextMenu"
    v-if="showContextMenu"
    :x="menuXPos"
    :y="menuYPos"
    :nodeId="contextNodeId"
    :closeContextMenu="closeContextMenu"
    v-model:selectedNodes="selectedNodes"
  />
</template>

<script setup lang="ts">
import 'v-network-graph/lib/style.css'
import { useStore } from 'vuex'
import { VNetworkGraph, defineConfigs, Layouts, Edges, Nodes, SimpleLayout, EventHandlers, NodeEvent, Instance } from 'v-network-graph'
import { ForceLayout, ForceNodeDatum, ForceEdgeDatum } from 'v-network-graph/lib/force-layout'
import { SimulationNodeDatum } from 'd3'
import ContextMenu from './ContextMenu.vue'
import { onClickOutside } from '@vueuse/core'

interface d3Node extends Required<SimulationNodeDatum> {
  id: string
}

const store = useStore()
const zoomLevel = ref(1)
const graph = ref<Instance>()
const selectedNodes = ref<string[]>([])
const tooltip = ref<HTMLDivElement>()
const tooltipOpacity = ref(0) // 0 or 1
const cancelTooltipDebounce = ref(false)
const targetNodeId = ref('')
const d3Nodes = ref<d3Node[]>([])
const contextMenu = ref(null)
const showContextMenu = ref(false)
const contextNodeId = ref()
const menuXPos = ref(0)
const menuYPos = ref(0)
const NODE_RADIUS = 20

const getD3NodeCoords = () => d3Nodes.value.filter((d3Node) => d3Node.id === targetNodeId.value).map((d3Node) => ({ x: d3Node.x, y: d3Node.y }))[0]
const closeContextMenu = () => showContextMenu.value = false
onClickOutside(contextMenu, () => closeContextMenu())

const verticies = computed<Nodes>(() => { console.log(store.state.topologyModule.verticies); return store.state.topologyModule.verticies })
const edges = computed<Edges>(() => store.state.topologyModule.edges)
const layout = computed<Layouts>(() => store.getters['topologyModule/getLayout'])

const tooltipPos = computed(() => {
  if (!graph.value || !tooltip.value) return { x: 0, y: 0 }
  if (!targetNodeId.value) return { x: 0, y: 0 }

  // attempt to get the node position from the layout. If layout is d3, use the function
  const nodePos = layout.value.nodes ? layout.value.nodes[targetNodeId.value] : getD3NodeCoords()

  // translate coordinates: SVG -> DOM
  const domPoint = graph.value.translateFromSvgToDomCoordinates(nodePos)

  // calculates top-left position of the tooltip.
  return {
    left: domPoint.x - tooltip.value.offsetWidth / 2 + 'px',
    top: domPoint.y - NODE_RADIUS - tooltip.value.offsetHeight - 10 + 'px',
  }
})

const eventHandlers: EventHandlers = {
  // on right clicking node
  'node:contextmenu': ({ node, event }: NodeEvent<any>) => {
    event.preventDefault()
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
        tooltipOpacity.value = 1 // show
      }
    }, 500)

    showTooltip()
  },
  'node:pointerout': () => {
    cancelTooltipDebounce.value = true
    tooltipOpacity.value = 0 // hide
  }
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

const configs = reactive(
  defineConfigs({
    view: {
      layoutHandler: store.state.topologyModule.selectedView === 'd3' ? forceLayout : new SimpleLayout()
    },
    node: {
      selectable: true
    }
  })
)
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
    opacity: 0;
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
    transition: opacity 0.2s linear;
  }
}
</style>
