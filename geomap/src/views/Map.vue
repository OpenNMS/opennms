<template>
  <splitpanes class="default-theme" horizontal style="height: 1000px">
    <pane min-size="1" max-size="60">
      <div class="leaflet-map">
        <LeafletMap />
      </div>
    </pane>
    <pane id="map-pane-under">
      <router-link :to="{ name: 'MapAlarms' }">Alarms</router-link>
      |
      <router-link :to="{ name: 'MapNodes' }">Nodes</router-link>
      <router-view />
    </pane>
  </splitpanes>
</template>

<script setup lang="ts">
import LeafletMap from "../components/LeafletMap.vue";
import { Splitpanes, Pane } from "splitpanes";
import "splitpanes/dist/splitpanes.css";
</script>

<style scoped>
#map-pane-under {
  text-align: left;
}

#map-pane-under a {
  font-family: Arial;
  font-size: 15px;
  /* font-weight: bold; */
  color: #7e8198;
}

#map-pane-under a.router-link-exact-active {
  color: #131736;
}
</style>