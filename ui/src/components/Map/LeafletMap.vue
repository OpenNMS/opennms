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
        @moveend="onMoveEnd"
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
              v-if="zoom > 5"
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
import { Node, Alarm } from "@/types"
import NormalIcon from '@/assets/Normal-icon.png'
import WarninglIcon from '@/assets/Warning-icon.png'
import MinorIcon from '@/assets/Minor-icon.png'
import MajorIcon from '@/assets/Major-icon.png'
import CriticalIcon from '@/assets/Critical-icon.png'
import { Map as LeafletMap } from 'leaflet'

const store = useStore()
const map = ref()
const leafletReady = ref<boolean>(false)
const leafletObject = ref({} as LeafletMap)
const zoom = ref<number>(2)
const iconWidth = 25
const iconHeight = 42
const iconSize = [iconWidth, iconHeight]

const center = computed<number[]>(() => ['latitude', 'longitude'].map(k => store.state.mapModule.mapCenter[k]))
const interestedNodes = computed<Node[]>(() => store.getters['mapModule/getInterestedNodes'])
const nodeLabelAlarmServerityMap = computed(() => {
  const alarms: Alarm[] = store.getters["mapModule/getAlarmsFromSelectedNodes"]
  const map: Map<string, string> = new Map<string, string>()
  alarms.forEach((alarm: Alarm) => {
    if (getServerityLevel(alarm.severity) > getServerityLevel(map.get(alarm.nodeLabel))) {
      map.set(alarm.nodeLabel, alarm.severity.toUpperCase())
    }
  })
  return map
})

const getServerityLevel = (severity: string | undefined) => {
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

const setIcon = (node: Node) => setMarkerColor(nodeLabelAlarmServerityMap.value.get(node.label))

const setMarkerColor = (severity: string | undefined) => {
  if (severity) {
    switch (severity.toUpperCase()) {
      case "NORMAL":
        return NormalIcon
      case "WARNING":
        return WarninglIcon
      case "MINOR":
        return MinorIcon
      case "MAJOR":
        return MajorIcon
      case "CRITICAL":
        return CriticalIcon
      default:
        return NormalIcon
    }
  }
  return NormalIcon
}

const edges = computed(() => {
  const ids: string[] = interestedNodes.value.map((node: Node) => node.id)
  const interestedNodesCoordinateMap = getInterestedNodesCoordinateMap()
  return store.state.mapModule.edges.filter((edge: [number, number]) => ids.includes(edge[0].toString()) && ids.includes(edge[1].toString()))
    .map((edge: [number, number]) => {
      let edgeCoordinatesPair = []
      edgeCoordinatesPair.push(interestedNodesCoordinateMap.get(edge[0]))
      edgeCoordinatesPair.push(interestedNodesCoordinateMap.get(edge[1]))
      return edgeCoordinatesPair
    })
})

const getCoordinateFromNode = (node: Node) => [node.assetRecord.latitude, node.assetRecord.longitude]
const getInterestedNodesCoordinateMap = () => {
  const map = new Map()
  interestedNodes.value.forEach((node: Node) => {
    map.set(node.id, getCoordinateFromNode(node))
  })
  return map
}

const onLeafletReady = async () => {
  await nextTick()
  leafletObject.value = map.value.leafletObject
  if (leafletObject.value != undefined && leafletObject.value != null) {
    leafletReady.value = true
  }
}

const onMoveEnd = () => {
  zoom.value = leafletObject.value.getZoom()
  store.dispatch('mapModule/setMapBounds', leafletObject.value.getBounds())
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
