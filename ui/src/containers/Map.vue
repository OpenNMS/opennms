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
          <router-link :to="{ name: 'MapNodes' }">Nodes({{ interestedNodesID.length }})</router-link>|
          <router-link :to="{ name: 'MapAlarms' }">Alarms({{ alarms.length }})</router-link>
          <router-view />
        </pane>
      </splitpanes>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useStore } from "vuex"
import { Splitpanes, Pane } from "splitpanes"
import "splitpanes/dist/splitpanes.css"
import LeafletMap from "../components/Map/LeafletMap.vue"

const store = useStore()

const interestedNodesID = computed<string[]>(() => store.state.mapModule.interestedNodesID)
const alarms = computed(() => store.getters['mapModule/getAlarmsFromSelectedNodes'])

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
#map-pane-under {
  text-align: left;
  a {
    font-family: Arial;
    font-size: 15px;
    &.router-link-exact-active {
      background-color: #cfd1df;
      font-weight: bold;
      padding: 8px;
      border-radius: 3px;
    }
  }
}
.map {
  padding: 10px;
}
</style>
