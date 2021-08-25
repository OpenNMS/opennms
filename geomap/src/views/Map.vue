<template>
  <div class="leaflet-map">
    <LeafletMap />
  </div>
  <router-link :to="{ name: 'MapAlarms' }">Alarms</router-link>
  |
  <router-link :to="{ name: 'MapNodes' }">Nodes</router-link>
  <router-view />
</template>

<script lang="ts">
import LeafletMap from "../components/LeafletMap.vue";

export default {
  name: "Map",
  components: {
    LeafletMap,
  },
};
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