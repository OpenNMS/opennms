<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <splitpanes
        :dbl-click-splitter="true"
        @pane-maximize="minimizeBottomPane"
        class="default-theme map-panes"
        horizontal
        ref="split"
        @resize="resize"
      >
        <pane min-size="1" max-size="100" :size="72">
          <LeafletMap v-if="nodesReady" ref="leafletComponent" />
        </pane>
        <pane min-size="1" max-size="100" :size="28" class="bottom-pane">
          <GridTabs />
        </pane>
      </splitpanes>
    </div>
  </div>
</template>

<!-- used to keep map alive once loaded -->
<script lang="ts">
import { onMounted, ref } from 'vue'

export default { name: 'MapKeepAlive' }
</script>

<script setup lang="ts">
import { debounce } from 'lodash'
import { Splitpanes, Pane } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import LeafletMap from '../components/Map/LeafletMap.vue'
import GridTabs from '@/components/Map/GridTabs.vue'
import useSpinner from '@/composables/useSpinner'
import { useMapStore } from '@/stores/mapStore'

const mapStore = useMapStore()
const { startSpinner, stopSpinner } = useSpinner()
const split = ref()
const nodesReady = ref(false)
const leafletComponent = ref()

// resizes the map / loads missing tiles
const resize = debounce(() => leafletComponent.value.invalidateSizeFn(), 200)

const minimizeBottomPane = () => {
  // override splitpane event
  split.value.panes[0].size = 96
  split.value.panes[1].size = 4
  resize()
}

onMounted(async () => {
  startSpinner()
  await mapStore.getNodes()
  await mapStore.getAlarms()
  stopSpinner()
  resize()
  nodesReady.value = true
  // commented out until we do topology
  // mapStore.getNodesGraphEdges()
})

</script>

<style scoped lang="scss">
// Fit the height the app shell leaves for page content, or the page pushes the
// footer past the bottom of the window and clips it. The shell takes the masthead
// (--onms-header-height) off the top and the footer band (--onms-footer-height) off the bottom.
//
// The map is the whole page — no breadcrumbs or heading above it — so the shell's
// own chrome is all there is to subtract.
.map-panes {
  height: calc(100vh - var(--onms-header-height, 3.75rem) - var(--onms-footer-height, 41px));
}

.bottom-pane {
  position: relative;
}
</style>

<style lang="scss">
.default-theme {
  .splitpanes__splitter {
    height: 10px !important;
    background: var(--p-datatable-header-cell-background) !important;
  }
  .splitpanes__splitter::after,
  .splitpanes__splitter::before {
    background: var(--p-text-color) !important;
  }
}
</style>
