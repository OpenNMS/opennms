<template>
  <div class="leaflet">
    <div class="geo-map">
      <l-map
        ref="map"
        :max-zoom="19"
        v-model="zoom"
        :zoomAnimation="true"
        :center="openNMSHeadQuarter"
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
          v-for="(node, index) in interestedNodes"
          :key="index"
          :lat-lng="getCoordinateFromNode(node)"
          :icon ="redMarker"
add         >
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
import { computed, watch, ref, nextTick, onMounted, onBeforeUnmount } from "vue";
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
// import L from "leaflet";
import AwesomeMarkers from "leaflet.awesome-markers";


let leafletReady = ref(false);
let leafletObject = ref("");
let visible = ref(false);
let map: any = ref();
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

onMounted(async () => {
      const {
        bind,
        Browser,
        DivIcon,
        DomEvent,
        DomUtil,
        extend,
        FeatureGroup,
        featureGroup,
        Icon,
        LatLng,
        LatLngBounds,
        LayerGroup,
        Marker,
        marker,
        Point,
        Util,
      } = await import("leaflet/dist/leaflet-src.esm");

      /** create a fake window.L from just the bits we need to make markercluster load properly **/
      const L = {
        bind,
        Browser,
        DivIcon,
        DomUtil,
        extend,
        FeatureGroup,
        featureGroup,
        Icon,
        LatLng,
        LatLngBounds,
        LayerGroup,
        Marker,
        Point,
        Util,
      } as any;
      window['L'] = L;
});

 let redMarker = L.AwesomeMarkers.icon({
     icon: 'coffee',
     markerColor: 'red'
   });

// let greenIcon = new L.Icon({
//   iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
//   shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
//   iconSize: [25, 41],
//   iconAnchor: [12, 41],
//   popupAnchor: [1, -34],
//   shadowSize: [41, 41]
// });
      
  // L.marker([51.941196,4.512291], {icon: redMarker}).addTo(map);


</script>

<style scoped>
.geo-map {
  height: 80vh;
}
</style>
