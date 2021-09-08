<template>
  <div class="leaflet">
    <div class="geo-map">
    <l-map
      v-model:zoom="zoom"
      :zoomAnimation="true"
      :center="openNMSHeadQuarter"
    >
      <l-tile-layer
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution='&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
      ></l-tile-layer>
      <l-control-layers />

      <l-marker
        v-for="(node, index) in interestedNodes"
        :key="index"
        :lat-lng="getCoordinateFromNode(node)"
      ></l-marker>
    </l-map>
  </div>
  </div>
</template>
<script setup lang="ts">
import { computed, watch, ref } from 'vue'
import {
  LMap,
  LTileLayer,
  LMarker,
  LControlLayers,
  // LTooltip,
  // LPopup,
  // LPolyline,
} from "@vue-leaflet/vue-leaflet";
import "leaflet/dist/leaflet.css";
import { useStore } from "vuex";

const store = useStore();

const openNMSHeadQuarter = ref([35.849613, -78.794882])
const zoom = ref(4)

let interestedNodes = computed(() => {
  return store.getters['mapModule/getInterestedNodes'];
})

function getCoordinateFromNode(node: any) {
      let coordinate = [];
      coordinate.push(node.assetRecord.latitude);
      coordinate.push(node.assetRecord.longitude);
      return coordinate;
}

let interestedNodesID = computed(() => {
  return store.getters['mapModule/getInterestedNodesID'];
})

watch(
  () => interestedNodesID.value,
  (newValue, oldValue) => {
    console.log("LeafletMap page. I'm changed from " + oldValue + " to " + newValue)
  }
)

</script>

<style scoped>
.geo-map {
  height: 80vh;
}
</style>
