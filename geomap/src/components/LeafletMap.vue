<template>
  <div class="leaflet">
    <!-- <img src="src/assets/red-marker.png"> -->
    <div class="geo-map">
      <l-map
        ref="map"
        v-model:center="center"
        :max-zoom="19"
        :min-zoom="2"
        :zoom="zoom"
        :zoomAnimation="true"
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
          <marker-cluster :options="{ showCoverageOnHover: false, chunkedLoading: true }">
            <l-marker
          v-for="(node, index) in interestedNodes"
          :key="index"
          :lat-lng="getCoordinateFromNode(node)"
          :icon ="setIcon"
add         
>
        <l-popup> {{ node.label }} </l-popup>
        </l-marker>
        <l-polyline
          v-for="(coordinatePair, index) in edges"
          :key="index"
          :lat-lngs="[coordinatePair[0], coordinatePair[1]]"
          color="green"
        />
          </marker-cluster>
        </template>
      </l-map>
    </div>
  </div>
</template>
<script setup lang ="ts">
import { computed, watch, ref, nextTick, onMounted } from "vue";
import "leaflet/dist/leaflet.css";
import {
  LMap,
  LTileLayer,
  LMarker,
  LPopup,
  LControlLayers,
  LPolyline,
} from "@vue-leaflet/vue-leaflet";
import MarkerCluster from "./MarkerCluster.vue";
import { Vue } from "vue-class-component";
import { useStore } from "vuex";
// import commonjs from 'rollup-plugin-commonjs';
 import L from "leaflet";
// import { Console } from "console";


import { Coordinates } from "@/types";

let leafletReady = ref(false);
let leafletObject = ref("");
let visible = ref(false);
let map: any = ref();
const store = useStore();

let center = computed(() => {
  const coordinates: Coordinates = store.getters['mapModule/getMapCenter']
  return [coordinates.latitude, coordinates.longitude];
})

let zoom = ref(2);

let interestedNodes = computed(() => {
  return store.getters['mapModule/getInterestedNodes'];
})

console.log("interestedNodes>>>>>>", interestedNodes.value)

let interestedAlarms = computed(() => {
    return store.getters['mapModule/getAlarmsFromSelectedNodes'];
  // return store.state.mapModule.alarms
})
console.log("interestedAlarms>>>>>>", interestedAlarms.value)


//  onMounted(async () => {
//    const alarmData = await store.getters['mapModule/getAlarmsFromSelectedNodes'];
//    console.log("alarm data", alarmData);
//  })




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

async function onLeafletReady() {
  await nextTick();
  leafletObject.value = map.value.leafletObject;
  if (leafletObject.value != undefined && leafletObject.value != null) {
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



// onMounted(async () => {
      // const {
      //   bind,
      //   Browser,
      //   DivIcon,
      //   DomEvent,
      //   DomUtil,
      //   extend,
      //   FeatureGroup,
      //   featureGroup,
      //   Icon,
      //   LatLng,
      //   LatLngBounds,
      //   LayerGroup,
      //   Marker,
      //   marker,
      //   Point,
      //   Util,
      // } = await import("leaflet/dist/leaflet-src.esm");

      /** create a fake window.L from just the bits we need to make markercluster load properly **/
//       const L = {
//         bind,
//         Browser,
//         DivIcon,
//         DomUtil,
//         extend,
//         FeatureGroup,
//         featureGroup,
//         Icon,
//         LatLng,
//         LatLngBounds,
//         LayerGroup,
//         Marker,
//         Point,
//         Util,
//       } as any;
//       window['L'] = L;
//  });
// const {AwesomeMarkers} = await import ("leaflet.awesome-markers");

//  const redMarker = AwesomeMarkers.icon({
//      icon: 'coffee',
//      markerColor: 'red'
//    });
var iconPath
 let setIcon = new L.Icon({
   iconUrl: setMarkerColor(),
   shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
   iconSize: [25, 41],
   iconAnchor: [12, 41],
   popupAnchor: [1, -34],
   shadowSize: [41, 41]
 });

 function setMarkerColor(){
 let severity = "MAJOR";

switch(severity) {
  case "NORMAL":
    return iconPath = "src/assets/Normal-icon.png";
    break;
  case "WARNING":
    return iconPath = "src/assets/Warning-icon.png";
    break;
  case "MINOR":
    return iconPath = "src/assets/Minor-icon.png";
    break;
    case "MAJOR":
    return iconPath = "src/assets/Major-icon.png";
    break;
    case "CRITICAL":
    return iconPath = "src/assets/Critical-icon.png";
    break;
  default:
    return iconPath = "src/assets/Normal-icon.png";
} 
 }
      
  // L.marker([51.941196,4.512291], {icon: redMarker}).addTo(map);


</script>

<style scoped>
.geo-map {
  height: 80vh;
}
</style>
