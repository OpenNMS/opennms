<template>
  <div class="leaflet">
    <div class="geo-map">
      <LMap
        ref="map"
        :center="center"
        :max-zoom="19"
        :min-zoom="2"
        :zoomAnimation="true"
        @ready="onLeafletReady"
        @moveend="onMoveEnd"
      >
        <template v-if="leafletReady">
          <MapSearch class="search-bar" />
          <LControlLayers />
          <LTileLayer
            v-for="tileProvider in tileProviders"
            :key="tileProvider.name"
            :name="tileProvider.name"
            :visible="tileProvider.visible"
            :url="tileProvider.url"
            :attribution="tileProvider.attribution"
            layer-type="base"
          />
          <MarkerCluster
            :options="{ showCoverageOnHover: false, chunkedLoading: true, iconCreateFunction }"
          >
            <LMarker
              v-for="node of nodes"
              :key="node.label"
              :lat-lng="[node.assetRecord.latitude, node.assetRecord.longitude]"
              :name="node.label"
            >
              <LPopup>{{ node.label }}</LPopup>
              <LIcon :icon-url="setIcon(node)" :icon-size="iconSize" />
            </LMarker>
            <LPolyline
              v-if="zoom > 5"
              v-for="coordinatePair of edges"
              :key="coordinatePair[0].toString()"
              :lat-lngs="[coordinatePair[0], coordinatePair[1]]"
              color="green"
            />
          </MarkerCluster>
        </template>
      </LMap>
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
import { Map as LeafletMap, divIcon, MarkerCluster as Cluster } from 'leaflet'
import MapSearch from './MapSearch.vue'

const store = useStore()
const map = ref()
const leafletReady = ref<boolean>(false)
const leafletObject = ref({} as LeafletMap)
const zoom = ref<number>(2)
const iconWidth = 25
const iconHeight = 42
const iconSize = [iconWidth, iconHeight]
const center = computed<number[]>(() => ['latitude', 'longitude'].map(k => store.state.mapModule.mapCenter[k]))
const nodes = computed<Node[]>(() => store.getters['mapModule/getNodes'])
const nodeLabelAlarmServerityMap = computed(() => {
  const alarms: Alarm[] = store.getters["mapModule/getAlarms"]
  const map: Map<string, string> = new Map<string, string>()
  alarms.forEach((alarm: Alarm) => {
    if (getServerityLevel(alarm.severity) > getServerityLevel(map.get(alarm.nodeLabel))) {
      map.set(alarm.nodeLabel, alarm.severity.toUpperCase())
    }
  })
  return map
})

const getHighestSeverity = (severitites: string[]) => {
  let highestSeverity = 'NORMAL'
  for (const severity of severitites) {
    if (getServerityLevel(severity) > getServerityLevel(highestSeverity)) {
      highestSeverity = severity
    }
  }
  return highestSeverity
}

// for custom marker cluster icon
const iconCreateFunction = (cluster: Cluster) => {
  const childMarkers = cluster.getAllChildMarkers()
  // find highest level of severity
  const severitites = []
  for (const marker of childMarkers) {
    const markerSeverity = nodeLabelAlarmServerityMap.value.get((marker as any).options.name)
    if (markerSeverity) {
      severitites.push(markerSeverity)
    }
  }
  const highestSeverity = getHighestSeverity(severitites)
  return divIcon({ html: `<span class=${highestSeverity}>` + cluster.getChildCount() + '</span>' })
}

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
  const ids: string[] = nodes.value.map((node: Node) => node.id)
  const interestedNodesCoordinateMap = getInterestedNodesCoordinateMap()
  return store.state.mapModule.edges.filter((edge: [number, number]) => ids.includes(edge[0].toString()) && ids.includes(edge[1].toString()))
    .map((edge: [number, number]) => {
      let edgeCoordinatesPair = []
      edgeCoordinatesPair.push(interestedNodesCoordinateMap.get(edge[0]))
      edgeCoordinatesPair.push(interestedNodesCoordinateMap.get(edge[1]))
      return edgeCoordinatesPair
    })
})

const getInterestedNodesCoordinateMap = () => {
  const map = new Map()
  nodes.value.forEach((node: Node) => {
    map.set(node.id, [node.assetRecord.latitude, node.assetRecord.longitude])
  })
  return map
}

const onLeafletReady = async () => {
  await nextTick()
  leafletObject.value = map.value.leafletObject
  leafletObject.value.zoomControl.setPosition('topright')
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
.search-bar {
  margin-left: 5px;
  margin-bottom: 23px;
  margin-top: -5px;
}
.geo-map {
  height: 80vh;
}
</style>

<style lang="scss">
.leaflet-marker-pane {
  div {
    width: 30px !important;
    height: 30px !important;
    margin-left: -15px !important;
    margin-top: -15px !important;
    text-align: center;
    font: 12px "Helvetica Neue", Arial, Helvetica, sans-serif;
    border-radius: 15px;
    border: none;
    span {
      border-radius: 15px;
      line-height: 30px;
      width: 100%;
      display: block;
      &.NORMAL {
        background: var(--feather-success);
      }
      &.WARNING,
      &.MINOR,
      &.MAJOR {
        background: var(--feather-warning);
      }
      &.CRITICAL {
        background: var(--feather-error);
      }
      opacity: 0.7;
    }
  }
}
</style>
