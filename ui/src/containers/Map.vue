<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <splitpanes class="default-theme" horizontal style="height: calc(100vh - 80px)">
        <pane min-size="1" max-size="100" size="68">
          <div class="leaflet-map">
            <LeafletMap />
          </div>
        </pane>
        <pane min-size="1" max-size="100" size="32" class="bottom-pane">
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

<script setup lang="ts">
import { onMounted, onActivated, onDeactivated } from 'vue'
import { useStore } from "vuex"
import { Splitpanes, Pane } from "splitpanes"
import "splitpanes/dist/splitpanes.css"
import LeafletMap from "../components/Map/LeafletMap.vue"
import GridTabs from '@/components/Map/GridTabs.vue'

const store = useStore()

onMounted(() => {
  store.dispatch("mapModule/getNodes")
  store.dispatch("mapModule/getAlarms")
  store.dispatch("mapModule/getNodesGraphEdges")
})

onActivated(() => store.dispatch('appModule/setNavRailOpen', false))
onDeactivated(() => store.dispatch('appModule/setNavRailOpen', true))
</script>

<style scoped lang="scss">
.bottom-pane {
  position: relative;
}
</style>

<style lang="scss">
.default-theme {
  .splitpanes__splitter {
    height: 10px !important;
    background: var(--feather-shade-3) !important;
  }
  .splitpanes__splitter::after,
  .splitpanes__splitter::before {
    background: var(--feather-primary-text-on-surface) !important;
  }
}
</style>
