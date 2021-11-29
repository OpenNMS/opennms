<template>
  <div class="feather-row map">
    <div class="feather-col-12">
      <splitpanes class="default-theme" horizontal style="height: calc(100vh - 80px)">
        <pane min-size="1" max-size="60">
          <div class="leaflet-map">
            <LeafletMap />
          </div>
        </pane>
        <pane id="map-pane-under">
          <GridTabs />
        </pane>
      </splitpanes>
    </div>
  </div>
</template>

<!-- used to keep map alive once loaded -->
<script lang="ts">
  export default {name: 'MapKeepAlive'}
</script>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useStore } from "vuex"
import { Splitpanes, Pane } from "splitpanes"
import "splitpanes/dist/splitpanes.css"
import LeafletMap from "../components/Map/LeafletMap.vue"
import GridTabs from '@/components/Map/GridTabs.vue'

const store = useStore()

onMounted(() => {
  store.dispatch("mapModule/getNodes", {
    limit: 5000,
    offset: 0,
  })

  store.dispatch("mapModule/getAlarms", {
    limit: 5000,
    offset: 0,
  })

  store.dispatch("mapModule/getNodesGraphEdges")
})
</script>

<style lang="scss" scoped>
.map {
  padding: 10px;
}
</style>

<style lang="scss">
.ag-row,
.ag-header-row,
.ag-paging-panel,
.ag-center-cols-viewport,
.ag-header-viewport {
  background: var(--feather-background) !important;
  color: var(--feather-primary-text-on-surface) !important;
}
.ag-icon {
  color: var(--feather-primary-text-on-surface) !important;
}
</style>
