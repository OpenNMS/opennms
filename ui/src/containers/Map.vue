<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <splitpanes class="default-theme" horizontal style="height: 1200px">
        <pane min-size="1" max-size="49">
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
import { Splitpanes, Pane } from "splitpanes";
import "splitpanes/dist/splitpanes.css";
import LeafletMap from "../components/Map/LeafletMap.vue";

import { useStore } from "vuex";
import { computed} from 'vue';

const store = useStore();

let interestedNodesID = computed(() => {
  return store.getters['mapModule/getInterestedNodesID'];
})

let alarms = computed(() => {
  return store.getters['mapModule/getAlarmsFromSelectedNodes'];
})

store.dispatch("mapModule/getNodes", {
  limit: 5000,
  offset: 0,
});

store.dispatch("mapModule/getAlarms", {
  limit: 5000,
  offset: 0,
});

store.dispatch("mapModule/getNodesGraphEdges");
</script>

<style lang="scss" scoped>
#map-pane-under {
  text-align: left;
}

#map-pane-under a {
  font-family: Arial;
  font-size: 15px;
}

#map-pane-under a.router-link-exact-active {
  background-color: #cfd1df;
  font-weight: bold;
  padding: 8px;
  border-radius: 3px;
}
</style>