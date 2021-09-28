<template>
  <div class="leaflet">
    <div class="geo-map">
      <l-map
        ref="map"
        :max-zoom="19"
        v-model="zoom"
        :zoomAnimation="true"
        :center="center"
        @ready="onLeafletReady"
      >
        <template v-if="leafletReady">
          <l-control-layers />
          <l-tile-layer
            v-for="tileProvider in tileProviders"
            :key="tileProvider.name"
            :name="tileProvider.name"
            :visible="tileProvider.visible"
            :url="tileProvider.url"
            :attribution="tileProvider.attribution"
            layer-type="base"
          />
          <marker-cluster
            :options="{ showCoverageOnHover: false, chunkedLoading: true }"
          >
            <l-marker
              v-for="coordinate in coordinates"
              :key="coordinate"
              :lat-lng="coordinate.latlng"
            >
              <l-popup>{{ coordinate.popmessage }}</l-popup>
            </l-marker>
          </marker-cluster>
        </template>
      </l-map>
    </div>
  </div>
</template>
<script setup lang ="ts">
import { ref, nextTick } from "vue";
import "leaflet/dist/leaflet.css";
import {
  LMap,
  LTileLayer,
  LMarker,
  LPopup,
  LControlLayers,
} from "@vue-leaflet/vue-leaflet";
import MarkerCluster from "./MarkerCluster.vue";
import { Vue } from "vue-class-component";

const zoom = ref(7);
const center = ref({ lat: 51.289404225298256, lng: 9.697202050919614 });
let leafletReady = ref(false);
let leafletObject = ref("");
let visible = ref(false);
let map: any = ref();

/*****Multiple Markers*****/

const coordinates = [
  { latlng: [47.7515953048815, 8.757179159967961], popmessage: "Street 01" },
  { latlng: [54.379448751829784, 8.890621239746661], popmessage: "Street 02" },
  { latlng: [48.41432462648719, 11.172363685423019], popmessage: "Street 03" },
  { latlng: [54.34757868763789, 11.410597389004957], popmessage: "Street 04" },
  { latlng: [51.741295879474464, 13.693138753473695], popmessage: "Street 05" },
  { latlng: [53.574845165295145, 6.875185458821902], popmessage: "Street 06" },
  { latlng: [51.42494690949777, 6.901031944520698], popmessage: "Street 07" },
];

async function onLeafletReady() {
  await nextTick();
  leafletObject.value = map.value.leafletObject;
  if(leafletObject.value != undefined && leafletObject.value != null){
    leafletReady.value = true;
  }
}

/*****Tile Layer*****/

const tileProviders = [
  {
    name: "OpenStreetMap",
    visible: true,
    attribution:
      '&copy; <a target="_blank" href="http://osm.org/copyright">OpenStreetMap</a> contributors',
    url: "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
  },
  {
    name: "OpenTopoMap",
    visible: false,
    url: "https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png",
    attribution:
      'Map data: &copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>, <a href="http://viewfinderpanoramas.org">SRTM</a> | Map style: &copy; <a href="https://opentopomap.org">OpenTopoMap</a> (<a href="https://creativecommons.org/licenses/by-sa/3.0/">CC-BY-SA</a>)',
  },
];
</script>

<style scoped>
.geo-map {
  height: 80vh;
}
</style>
