<template>
  <div class="leaflet">
    <div class="geo-map">
      <LMap
        ref="map"
        :max-zoom="19"
        v-model="zoom"
        :zoomAnimation="true"
        :center="{ lat: 51.289404225298256, lng: 9.697202050919614 }"
        @ready="onLeafletReady"
      >
        <!-- <l-tile-layer 
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        attribution='&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
      ></l-tile-layer>
      <l-marker :lat-lng="markerLatLng"></l-marker>
      <l-marker :lat-lng="markerLatLng1"></l-marker> -->
        <!-- <l-control-layers /> -->
        <template v-if="leafletReady">
          <l-tile-layer url="https://{s}.tile.osm.org/{z}/{x}/{y}.png" />
          <marker-cluster
            :options="{ showCoverageOnHover: false, chunkedLoading: true }"
          >
            <l-marker :lat-lng="[47.7515953048815, 8.757179159967961]" />
            <l-marker :lat-lng="[54.379448751829784, 8.890621239746661]" />
            <l-marker :lat-lng="[48.41432462648719, 11.172363685423019]" />
            <l-marker :lat-lng="[54.34757868763789, 11.410597389004957]" />
            <l-marker :lat-lng="[51.741295879474464, 13.693138753473695]" />
            <l-marker :lat-lng="[53.574845165295145, 6.875185458821902]" />
            <l-marker :lat-lng="[51.42494690949777, 6.901031944520698]" />
          </marker-cluster>
        </template>
      </LMap>
    </div>
  </div>
</template>
<script setup lang ="ts">
import { reactive, onMounted, ref, nextTick } from "vue";
import "leaflet/dist/leaflet.css";
import { LMap, LTileLayer, LMarker } from "@vue-leaflet/vue-leaflet";
import MarkerCluster from "./MarkerCluster.vue";
import { Vue } from "vue-class-component";

const zoom = ref(7);
let leafletReady = ref(false);
let leafletObject = ref('');
let visible = ref(false);
let map: any = ref();

async function onLeafletReady() {
  await nextTick();
  leafletObject.value = map.value.leafletObject;
  leafletReady.value = true;
}

//  const openNMSHeadQuarter = ref([27.175014, -78.042152])
//  const markerLatLng = [47.7515953048815, 8.757179159967961];
//  const markerLatLng1 = [54.379448751829784, 8.890621239746661];
</script>

<style scoped>
.geo-map {
  height: 80vh;
}
</style>
