<template>
  <div class="leaflet">
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
            >
              <l-popup>{{ node.label }}</l-popup>
              <l-icon :icon-url="setIcon(node)" :icon-size="iconSize" />
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
import { computed, ref, nextTick } from "vue"
import "leaflet/dist/leaflet.css"
import {
  LMap,
  LTileLayer,
  LMarker,
  LIcon,
  LPopup,
  LControlLayers,
  LPolyline,
} from "@vue-leaflet/vue-leaflet"
import MarkerCluster from "./MarkerCluster.vue"
import { useStore } from "vuex"
import { Coordinates, Node, Alarm } from "@/types"
let leafletReady = ref<boolean>(false)
let leafletObject = ref("")
let map = ref()
const store = useStore()
const center = computed<[number, number]>(() => {
  const coordinates: Coordinates = store.state.mapModule.mapCenter
  return [coordinates.latitude, coordinates.longitude]
})
const zoom = ref<number>(2)
const interestedNodes = computed<Node[]>(() => store.getters['mapModule/getInterestedNodes'])
function getCoordinateFromNode(node: Node) {
  const coordinate: string[] = []
  coordinate.push(node.assetRecord.latitude)
  coordinate.push(node.assetRecord.longitude)
  return coordinate
}

const iconWidth = ref((25))
const iconHeight = ref((42))
const iconSize = computed(() => [iconWidth.value, iconHeight.value])
const nodeLabelAlarmServerityMap = computed(() => {
  const alarms: Alarm[] = store.getters["mapModule/getAlarmsFromSelectedNodes"]
  const map: Map<string, string> = new Map<string, string>()
  alarms.forEach(function (alarm: Alarm) {
    if (getServerityLevel(alarm.severity) > getServerityLevel(map.get(alarm.nodeLabel))) {
      map.set(alarm.nodeLabel, alarm.severity.toUpperCase())
    }
  })
  return map
})

function getServerityLevel(severity: string | undefined) {
  if (severity) {
    switch (severity.toUpperCase()) {
      case "NORMAL":
        return 11
      case "WARNING":
        return 22
      case "MINOR":
        return 33
      case "MAJOR":
        return 44
      case "CRITICAL":
        return 55
      default:
        return 0
    }
  }
  return 0
}

function setIcon(node: Node) {
  return setMarkerColor(nodeLabelAlarmServerityMap.value.get(node.label))
}

function setMarkerColor(severity: string | undefined) {
  if (severity) {
    switch (severity.toUpperCase()) {
      case "NORMAL":
        return ("src/assets/Normal-icon.png")
      case "WARNING":
        return ("src/assets/Warning-icon.png")
      case "MINOR":
        return ("src/assets/Minor-icon.png")
      case "MAJOR":
        return ("src/assets/Major-icon.png")
      case "CRITICAL":
        return ("src/assets/Critical-icon.png")
      default:
        return ("src/assets/Normal-icon.png")
    }
  }
  return ("src/assets/Normal-icon.png")
}

const interestedNodesID = computed<string[]>(() => store.state.mapModule.interestedNodesID)
const edges = computed(() => {
  const ids: string[] = interestedNodesID.value
  const interestedNodesCoordinateMap = getInterestedNodesCoordinateMap()
  return store.state.mapModule.edges.filter((edge: [number, number]) => ids.includes(edge[0].toString()) && ids.includes(edge[1].toString()))
    .map((edge: [number, number]) => {
      let edgeCoordinatesPair = []
      edgeCoordinatesPair.push(interestedNodesCoordinateMap.get(edge[0]))
      edgeCoordinatesPair.push(interestedNodesCoordinateMap.get(edge[1]))
      return edgeCoordinatesPair
    })
})
function getInterestedNodesCoordinateMap() {
  const map = new Map()
  interestedNodes.value.forEach((node: Node) => {
    map.set(node.id, getCoordinateFromNode(node))
  })
  return map
}
async function onLeafletReady() {
  await nextTick()
  leafletObject.value = map.value.leafletObject
  if (leafletObject.value != undefined && leafletObject.value != null) {
    leafletReady.value = true
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
]
</script>

<style scoped>
.geo-map {
  height: 80vh;
}
</style>
