<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <splitpanes
        :dbl-click-splitter="true"
        @pane-maximize="minimizeBottomPane"
        class="default-theme"
        horizontal
        style="height: calc(100vh - 80px)"
        ref="split"
        @resize="resize"
      >
        <pane
          min-size="1"
          max-size="100"
          :size="72"
        >
          <DrawerBtn />
          <TopologyLeftDrawer>
            <template v-slot:search>
              <TopologySearch v-if="isTopologyView" />
              <MapSearch
                class="search-bar"
                @fly-to-node="flyToNode"
                @set-bounding-box="setBoundingBox"
                v-else
              />
            </template>
            <template v-slot:view>
              <ViewSelect />
            </template>
          </TopologyLeftDrawer>
          <Topology v-if="isTopologyView" />
          <LeafletMap
            v-if="nodesReady"
            v-show="!isTopologyView"
            ref="leafletComponent"
          />
        </pane>
        <pane
          min-size="1"
          max-size="100"
          :size="28"
          class="bottom-pane"
        >
          <GridTabs />
        </pane>
      </splitpanes>
    </div>
  </div>
</template>

<!-- used to keep map alive once loaded -->
<script lang="ts">
export default { name: 'MapKeepAlive' }
</script>

<script
  setup
  lang="ts"
>
import { useStore } from 'vuex'
import { Splitpanes, Pane } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import LeafletMap from '../components/Map/LeafletMap.vue'
import Topology from './Topology.vue'
import GridTabs from '@/components/Map/GridTabs.vue'
import { debounce } from 'lodash'
import useSpinner from '@/composables/useSpinner'
import TopologyLeftDrawer from '@/components/Topology/TopologyLeftDrawer.vue'
import ViewSelect from '@/components/Topology/ViewSelect.vue'
import TopologySearch from '@/components/Topology/TopologySearch.vue'
import MapSearch from '@/components/Map/MapSearch.vue'
import DrawerBtn from '@/components/Topology/DrawerBtn.vue'

const store = useStore()
const { startSpinner, stopSpinner } = useSpinner()
const split = ref()
const nodesReady = ref(false)
const leafletComponent = ref()

const isTopologyView = computed<boolean>(() => store.state.topologyModule.isTopologyView)

const minimizeBottomPane = () => {
  // override splitpane event
  split.value.panes[0].size = 96
  split.value.panes[1].size = 4
  if (!isTopologyView.value) {
    setTimeout(() => leafletComponent.value.invalidateSizeFn(), 200)
  }
}

// resize the map when splitter dragged
const resize = debounce(() => {
  if (!isTopologyView.value) {
    leafletComponent.value.invalidateSizeFn(), 200
  }
})

const flyToNode = (node: string) => leafletComponent.value.flyToNode(node)
const setBoundingBox = (nodeLabels: string[]) => leafletComponent.value.setBoundingBox(nodeLabels)

onMounted(async () => {
  startSpinner()
  await store.dispatch('mapModule/getNodes')
  await store.dispatch('mapModule/getAlarms')
  stopSpinner()
  resize()
  nodesReady.value = true
  store.dispatch('topologyModule/getVerticesAndEdges')
})

onActivated(() => store.dispatch('appModule/setNavRailOpen', false))
onDeactivated(() => store.dispatch('appModule/setNavRailOpen', true))
</script>

<style
  scoped
  lang="scss"
>
.bottom-pane {
  position: relative;
}
</style>

<style lang="scss">
@import "@featherds/styles/themes/variables";
.default-theme {
  .splitpanes__splitter {
    height: 10px !important;
    background: var($shade-3) !important;
  }
  .splitpanes__splitter::after,
  .splitpanes__splitter::before {
    background: var($primary-text-on-surface) !important;
  }
}
</style>

