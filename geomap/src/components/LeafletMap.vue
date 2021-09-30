<template>
  <div class="leaflet">
    <div class="geo-map">
      <l-map v-model:zoom="zoom" :zoomAnimation="true" :center="openNMSHeadQuarter">
        <l-tile-layer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          layer-type="base"
          name="OpenStreetMap"
          attribution='&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
        ></l-tile-layer>

        <l-marker
          v-for="(node, index) in interestedNodes"
          :key="index"
          :lat-lng="getCoordinateFromNode(node)"
        >
        <l-popup> {{ node.label }} </l-popup>
        </l-marker>
        
        <l-polyline
          v-for="(coordinatePair, index) in edges"
          :key="index"
          :lat-lngs="[coordinatePair[0], coordinatePair[1]]"
          color="green"
        />
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
  // LTooltip,
  LPopup,
  LPolyline,
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
  let coordinate: string[] = [];
  coordinate.push(node.assetRecord.latitude);
  coordinate.push(node.assetRecord.longitude);
  return coordinate;
}

let interestedNodesID = computed(() => {
  return store.getters['mapModule/getInterestedNodesID'];
})

let edges = computed(() => {
  let ids = interestedNodesID.value;
  let interestedNodesCoordinateMap = getInterestedNodesCoordinateMap();

  return store.getters['mapModule/getEdges'].filter((edge: [number, number]) => ids.includes(edge[0]) && ids.includes(edge[1]))
    .map((edge: [number, number]) => {
      let edgeCoordinatesPair = [];
      edgeCoordinatesPair.push(interestedNodesCoordinateMap.get(edge[0]));
      edgeCoordinatesPair.push(interestedNodesCoordinateMap.get(edge[1]));
      return edgeCoordinatesPair
    });
})

function getInterestedNodesCoordinateMap() {
  var map = new Map();
  interestedNodes.value.forEach((node: any) => {
    map.set(node.id, getCoordinateFromNode(node));
  });
  return map;
}

watch(
  () => interestedNodesID.value,
  (newValue, oldValue) => {
  }
)

</script>

<style scoped>
.geo-map {
  height: 80vh;
}
</style>
