<template>
  <div class="geo-map">
    <MapSearch class="search-bar" @fly-to-node="flyToNode" @set-bounding-box="setBoundingBox" />
    <SeverityFilter />
    <!--
      vue-leaflet v0.7.0 needs useGlobalLeaflet=false
      See: https://github.com/vue-leaflet/vue-leaflet/releases/tag/v0.7.0
    -->
    <LMap
      ref="map"
      :center="center"
      :max-zoom="19"
      :min-zoom="2"
      :useGlobalLeaflet="false"
      :zoomAnimation="true"
      @ready="onLeafletReady"
      @moveend="onMoveEnd"
      @zoom="invalidateSizeFn"
    >
      <template v-if="leafletReady">
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
          :options="{
            showCoverageOnHover: true,
            chunkedLoading: true,
            disableClusteringAtZoom: 19,
            zoomToBoundsOnClick: false,
            iconCreateFunction
          }"
          :onClusterClick="onClusterClick"
        >
          <LMarker
            v-for="node of nodes"
            :key="node.label"
            :lat-lng="[node.assetRecord.latitude, node.assetRecord.longitude]"
            :name="node.label"
          >
            <MarkerPopup
              :baseHref="mainMenu.baseHref"
              :baseNodeUrl="baseNodeUrl"
              :node="node"
              :ipAddress="ipAddressForNode(node)"
              :nodeLabelToAlarmSeverity="nodeLabelToAlarmSeverity"
            />
            <LIcon :icon-url="setIcon(node)" :icon-size="iconSize" />
          </LMarker>
          <!-- Disable polylines until they work -->
          <!-- <LPolyline
              v-if="zoom > 5"
              v-for="coordinatePair of edges"
              :key="coordinatePair[0].toString()"
              :lat-lngs="[coordinatePair[0], coordinatePair[1]]"
              color="green"
          />-->
        </MarkerCluster>
      </template>
    </LMap>
  </div>
  <div class="marker-cluster-popup-hide">
    <MarkerClusterPopupContents
      v-if="showClusterPopup"
      :cluster="popupCluster"
      :ipListForNode="ipListForNode"
      ref="clusterPopupContent" />
  </div>
</template>

<script setup lang ="ts">
import { computed, nextTick, onActivated, onBeforeUnmount, onDeactivated, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { debounce } from 'lodash'

import 'leaflet/dist/leaflet.css'
import { Map as LeafletMap, divIcon, LatLngTuple, MarkerCluster as Cluster, PopupOptions } from 'leaflet'
import {
  LMap,
  LTileLayer,
  LMarker,
  LIcon,
  LControlLayers
  // LPolyline,
} from '@vue-leaflet/vue-leaflet'
import CriticalIcon from '@/assets/Critical-icon.png'
import MinorIcon from '@/assets/Minor-icon.png'
import MajorIcon from '@/assets/Major-icon.png'
import NormalIcon from '@/assets/Normal-icon.png'
import WarninglIcon from '@/assets/Warning-icon.png'

import MarkerCluster from './MarkerCluster.vue'
import MapSearch from './MapSearch.vue'
import MarkerPopup from './MarkerPopup.vue'
import MarkerClusterPopupContents from './MarkerClusterPopupContent.vue'
import SeverityFilter from './SeverityFilter.vue'
import { mapPopupOptions, numericSeverityLevel } from './utils'

import { isNumber } from '@/lib/utils'
import { useGeolocationStore } from '@/stores/geolocationStore'
import { useIpInterfacesStore } from '@/stores/ipInterfacesStore'
import { useMapStore } from '@/stores/mapStore'
import { useMenuStore } from '@/stores/menuStore'
import { useNodeStore } from '@/stores/nodeStore'
import { Coordinates, IpInterface, Node } from '@/types'
import { MainMenu } from '@/types/mainMenu'
import { TileProviderItem } from '@/types'

const geolocationStore = useGeolocationStore()
const ipInterfaceStore = useIpInterfacesStore()
const mapStore = useMapStore()
const menuStore = useMenuStore()
const nodeStore = useNodeStore()
const map = ref()
const route = useRoute()
const leafletReady = ref<boolean>(false)
const leafletObject = ref({} as LeafletMap)

const showClusterPopup = ref(false)
const clusterPopupContent = ref()
const popupCluster = ref<Cluster>()

const zoom = ref<number>(2)
const iconWidth = 25
const iconHeight = 42
const iconSize = [iconWidth, iconHeight]

const numberOrStringToFloat = (n: number | string) => {
  return isNumber(n) ? Number(n) : parseFloat('' + n)
}

const center = computed<number[]>(() => {
  const { latitude, longitude } = mapStore.mapCenter
  const vals = [latitude, longitude].map(x => numberOrStringToFloat(x))
  return vals
})

const nodes = computed<Node[]>(() => mapStore.getNodes())
const allNodes = computed<Node[]>(() => mapStore.nodesWithCoordinates)
const tileProviders = computed<TileProviderItem[]>(() => geolocationStore.tileProviders)

// get all valid coordinates from existing nodes
const bounds = computed(() => {
  const coordinatedMap = getNodeCoordinateMap.value
  const coordinates = nodes.value.map(node => coordinatedMap.get(node.id)).filter(coord => coord !== undefined)

  return coordinates.map(c => [numberOrStringToFloat(c!.latitude), numberOrStringToFloat(c!.longitude)])
})

const nodeLabelAlarmSeverityMap = computed(() => mapStore.getNodeAlarmSeverityMap())
const mainMenu = computed<MainMenu>(() => menuStore.mainMenu)
const baseNodeUrl = computed<string>(() => `${mainMenu.value.baseHref}${mainMenu.value.baseNodeUrl}`)
const ipInterfaces = computed<IpInterface[]>(() => ipInterfaceStore.ipInterfaces)

// cached map of node id -> IpInterface list
const ipInterfaceMap = computed<Map<string, IpInterface[]>>(() => {
  const nodeIfMap = new Map<string, IpInterface[]>()

  for (let ip of ipInterfaces.value) {
    const key = ip.nodeId.toString()

    if (nodeIfMap.has(key)) {
      nodeIfMap.get(key)?.push(ip)
    } else {
      nodeIfMap.set(key, [ip])
    }
  }

  return nodeIfMap
})

const ipListForNode = (node: Node | null): IpInterface[] => {
  const key = node?.id || ''
  return (key && ipInterfaceMap.value.get(key)) || []
}

const ipAddressForNode = (node: Node | null) => {
  const ifList = ipListForNode(node)
  return (ifList.length > 0 && ifList[0].ipAddress) || ''
}

const nodeLabelToAlarmSeverity = (label: string) => {
  return nodeLabelAlarmSeverityMap.value[label] || 'NORMAL'
}

const getHighestSeverity = (severities: string[]) => {
  let highestSeverity = 'NORMAL'

  for (const severity of severities) {
    if (numericSeverityLevel(severity) > numericSeverityLevel(highestSeverity)) {
      highestSeverity = severity
    }
  }
  return highestSeverity
}

// for custom marker cluster icon
const iconCreateFunction = (cluster: Cluster) => {
  const childMarkers = cluster.getAllChildMarkers()
  // find highest level of severity
  const severities = []

  for (const marker of childMarkers) {
    const markerSeverity = nodeLabelAlarmSeverityMap.value[(marker as any).options.name]

    if (markerSeverity) {
      severities.push(markerSeverity)
    }
  }

  const highestSeverity = getHighestSeverity(severities)
  return divIcon({ html: `<span class=${highestSeverity}>` + cluster.getChildCount() + '</span>' })
}

// display the Marker Cluster / node group popup
const onClusterClick = async (ev: any) => {
  const cluster: Cluster = ev.layer as Cluster
  await initializeClusterPopup(cluster)
  launchClusterPopup(cluster)
}

// initialize the MarkerClusterPopupContent component and await its rendering
const initializeClusterPopup = async (cluster: Cluster) => {
  popupCluster.value = cluster
  showClusterPopup.value = true
  await nextTick()
}

// get markup content generated by the MarkerClusterPopupContent component
// to inject into the Leaflet popup component and launch the popup
const launchClusterPopup = (cluster: Cluster) => {
  const content = clusterPopupContent.value.$refs.clusterPopupContent.innerHTML
  const options: PopupOptions = { ...mapPopupOptions }

  const leafletObject = window['L']

  leafletObject.popup(options)
    .setLatLng(cluster.getLatLng())
    .setContent(content)
    .openOn(map.value.leafletObject)
}

const setIcon = (node: Node) => setMarkerColor(nodeLabelAlarmSeverityMap.value[node.label])

const setMarkerColor = (severity: string | undefined) => {
  if (severity) {
    switch (severity.toUpperCase()) {
      case 'NORMAL':
        return NormalIcon
      case 'WARNING':
        return WarninglIcon
      case 'MINOR':
        return MinorIcon
      case 'MAJOR':
        return MajorIcon
      case 'CRITICAL':
        return CriticalIcon
      default:
        return NormalIcon
    }
  }
  return NormalIcon
}

// const edges = computed(() => {
//   const ids: string[] = nodes.value.map((node: Node) => node.id)
//   const interestedNodesCoordinateMap = getNodeCoordinateMap.value
//   return mapStore.edges.filter((edge: [number, number]) => ids.includes(edge[0].toString()) && ids.includes(edge[1].toString()))
//     .map((edge: [number, number]) => {
//       let edgeCoordinatesPair = []
//       edgeCoordinatesPair.push(interestedNodesCoordinateMap.get(edge[0]))
//       edgeCoordinatesPair.push(interestedNodesCoordinateMap.get(edge[1]))
//       return edgeCoordinatesPair
//     })
// })

const getNodeCoordinateMap = computed(() => {
  const map = new Map<string, Coordinates>()

  allNodes.value.forEach((node: Node) => {
    const coord = { latitude: node.assetRecord.latitude, longitude: node.assetRecord.longitude }
    map.set(node.id, coord)
    map.set(node.label, coord)
  })

  return map
})

// Close the open map popup on Esc. Leaflet's own Esc-to-close only fires while
// the map *container* is focused (its Keyboard handler binds/unbinds the Esc
// listener on the container's focus/blur), so tabbing into the popup's links
// blurs the container and Esc stops working. This document listener is attached
// only while a popup is open (see popupopen/popupclose below) and runs in the
// bubble phase, so the menu search's Esc handler — which stopImmediatePropagation()s
// before the event reaches document — always takes precedence and is unaffected.
const onPopupEscapeKeydown = (e: KeyboardEvent) => {
  if (e.key !== 'Escape') {
    return
  }
  // The map search (PrimeVue AutoComplete) also uses Esc to dismiss its own
  // suggestions but doesn't stopPropagation, so its Esc bubbles to this
  // document listener. Don't let dismissing search suggestions also close the
  // open marker popup.
  if ((e.target as HTMLElement | null)?.closest('.map-search')) {
    return
  }
  leafletObject.value.closePopup()
}

// Leaflet caches its pixel size and only re-reads it on invalidateSize(). The
// map container reflows when the side menu is pinned (it sets padding-left on
// .app-layout) or on any other layout change, but Leaflet won't repaint to the
// new size on its own, so observe the container and revalidate on resize.
const debouncedInvalidateSize = debounce(() => {
  if (typeof leafletObject.value?.invalidateSize === 'function') {
    leafletObject.value.invalidateSize()
  }
}, 150)
let containerResizeObserver: ResizeObserver | null = null

onBeforeUnmount(() => {
  document.removeEventListener('keydown', onPopupEscapeKeydown)
  containerResizeObserver?.disconnect()
  debouncedInvalidateSize.cancel()
})

// Map.vue is <keep-alive>'d (name: MapKeepAlive), so navigating away deactivates
// rather than unmounts and onBeforeUnmount never fires. Remove the document Esc
// listener here too — otherwise, leaving with a popup open keeps it installed
// app-wide and a stray Esc later closes the popup on the cached map.
onDeactivated(() => {
  document.removeEventListener('keydown', onPopupEscapeKeydown)
})

// Returning to the cached map: its container may have resized while away (e.g.
// the side menu was pinned on another page), so revalidate Leaflet's size.
onActivated(() => {
  debouncedInvalidateSize()
})

const onLeafletReady = async () => {
  await nextTick()

  leafletObject.value = map.value.leafletObject

  if (leafletObject.value !== undefined && leafletObject.value !== null) {
    // set default map view port
    leafletObject.value.zoomControl.setPosition('topright')

    // Keyboard is left enabled (Leaflet keeps the container focusable via a
    // tabindex) so keyboard-only users retain arrow-key pan / +- zoom — removing
    // it fails WCAG 2.1.1. Leaflet focuses the container on click and the browser
    // scrolls it into view; because the map sits under the fixed top menu bar
    // that could shift it upward. `scroll-margin-top` on the container (see the
    // style block below) keeps that scroll clear of the menu bar instead.

    // Open popups below the marker. Leaflet has no popup "direction", so once a
    // popup is in the DOM (popupopen fires synchronously, before paint) push it
    // down by its own height plus 40px — the popup anchors at the marker's
    // vertical centre, so the extra clears the marker icon's lower half and
    // leaves a small gap below it. See mapPopupOptions (autoPan off,
    // .onms-popup-below hides the tip).
    leafletObject.value.on('popupopen', (e) => {
      const el = e.popup.getElement()
      if (!el) {
        return
      }
      e.popup.options.offset = [0, el.offsetHeight + 40]
      e.popup.update()

      // autoPan is off, so a below-popup can spill past any edge of the map
      // pane: bottom for low markers, left/right for the east/west-most markers,
      // top for a popup taller than the pane. Pan just enough on each axis to
      // bring it into view, biasing toward keeping the popup's top (its header
      // and close button) visible when it is taller than the pane.
      const pad = 16
      const popupRect = el.getBoundingClientRect()
      const mapRect = leafletObject.value.getContainer().getBoundingClientRect()

      let dx = 0
      if (popupRect.right > mapRect.right) {
        dx = popupRect.right - mapRect.right + pad
      } else if (popupRect.left < mapRect.left) {
        dx = popupRect.left - mapRect.left - pad
      }

      let dy = 0
      if (popupRect.bottom > mapRect.bottom) {
        // Don't pan so far down that the popup's top scrolls off the pane.
        const maxPanKeepingTopVisible = Math.max(0, popupRect.top - mapRect.top - pad)
        dy = Math.min(popupRect.bottom - mapRect.bottom + pad, maxPanKeepingTopVisible)
      } else if (popupRect.top < mapRect.top) {
        dy = popupRect.top - mapRect.top - pad
      }

      if (dx !== 0 || dy !== 0) {
        leafletObject.value.panBy([dx, dy], { animate: true })
      }

      document.addEventListener('keydown', onPopupEscapeKeydown)
    })

    leafletObject.value.on('popupclose', () => {
      document.removeEventListener('keydown', onPopupEscapeKeydown)
    })

    leafletReady.value = true

    // Revalidate Leaflet's cached size whenever its container resizes (side-menu
    // pin, window resize, splitter drag). See debouncedInvalidateSize above.
    const container = leafletObject.value.getContainer()
    if (container && typeof ResizeObserver !== 'undefined') {
      containerResizeObserver?.disconnect()
      containerResizeObserver = new ResizeObserver(() => debouncedInvalidateSize())
      containerResizeObserver.observe(container)
    }

    await nextTick()

    // save the bounds to state
    mapStore.setMapBounds(leafletObject.value.getBounds())
    await nextTick()

    if (bounds.value.length > 0) {
      try {
        leafletObject.value.fitBounds(bounds.value.map(b => [b[0], b[1]] as LatLngTuple))
      } catch (err) {
        console.log(err, `Invalid bounds array: ${bounds.value}`)
      }
    }

    // if nodeid query param, fly to it
    if (route.query.nodeid) {
      flyToNode(route.query.nodeid as string)
    }
  }
}

const onMoveEnd = () => {
  zoom.value = leafletObject.value.getZoom()
  mapStore.setMapBounds(leafletObject.value.getBounds())
}

const flyToNode = (nodeLabelOrId: string) => {
  const coordinateMap = getNodeCoordinateMap.value
  const nodeCoordinates = coordinateMap.get(nodeLabelOrId)

  if (nodeCoordinates) {
    leafletObject.value.flyTo([nodeCoordinates.latitude, nodeCoordinates.longitude] as LatLngTuple, 7)
  }
}

const setBoundingBox = (nodeLabels: string[]) => {
  const coordinateMap = getNodeCoordinateMap.value
  const bounds = nodeLabels.map(nodeLabel => coordinateMap.get(nodeLabel))

  if (bounds.length) {
    leafletObject.value.fitBounds(bounds.map(c => [c!.latitude, c!.longitude] as LatLngTuple))
  }
}

const invalidateSizeFn = () => leafletObject.value.invalidateSize()

onMounted(async () => {
  geolocationStore.fetchTileProviders()
  mapStore.fetchNodes()
  mapStore.fetchAlarms()
  nodeStore.getNodes()
  ipInterfaceStore.getAllIpInterfaces()
})

defineExpose({ invalidateSizeFn })
</script>

<style lang="scss" scoped>
.search-bar {
  position: absolute;
  // Top-left overlay inset from the map corner; mirrors the Show Severity
  // control on the right (right: 80px; top: 2em).
  top: 2em;
  left: 2em;
  z-index: 1020;
}
.geo-map {
  // Positioning context for the absolutely-positioned overlays (search bar,
  // Show Severity) so they anchor to the map area, not the viewport — otherwise
  // they slide under the pinned side-menu rail.
  position: relative;
  height: 100%;
}
.marker-cluster-popup-hide {
  display: none;
}
</style>

<style lang="scss">
@import "@/styles/onms-tokens";

// Inset Leaflet's top controls (zoom + layers, both top-right) to line up with
// the Search / Show Severity overlays. A stable CSS offset also survives
// Leaflet's invalidateSize() re-layout on interaction (which otherwise lets the
// controls settle back to the container's top edge).
.geo-map .leaflet-top {
  top: 2em;
}

// Leaflet focuses the container on click; the browser then scrolls it into
// view. Since the map sits under the fixed top menu bar, reserve the menu-bar
// height so that scroll stops below the bar instead of sliding the map (and its
// controls) under it. Keeps keyboard nav enabled without the focus-scroll jump.
.geo-map .leaflet-container {
  scroll-margin-top: var(--onms-header-height, 3.75rem);
}

// Popups open below the marker (offset applied in the popupopen handler), so
// hide Leaflet's tip — it points down from the popup's top edge, away from the
// marker below it, once the popup is flipped underneath.
.geo-map .leaflet-popup.onms-popup-below .leaflet-popup-tip-container {
  display: none;
}

// Leaflet expands the layers control's list *in place* — the list's top sits at
// the toggle icon's top and it grows leftward, sliding over the Show Severity
// control. Keep the toggle icon and drop the expanded list below it instead, so
// its top aligns with the bottom of the icon.
.geo-map .leaflet-control-layers {
  position: relative;
}
.geo-map .leaflet-control-layers-expanded {
  padding: 0;
}
.geo-map .leaflet-control-layers-expanded .leaflet-control-layers-toggle {
  display: block;
}
.geo-map .leaflet-control-layers-expanded .leaflet-control-layers-list {
  position: absolute;
  top: calc(100% + 5px);
  right: 0;
  min-width: 20em;
  padding: 6px 10px;
  // Theme-aware, matching the sibling controls and the popup wrapper — was
  // hardcoded #fff/#333, the one white element left in dark mode.
  background: var(--p-content-background);
  color: var(--p-text-color);
  border-radius: 5px;
  box-shadow: 0 1px 5px rgba(0, 0, 0, 0.4);
}
// Bridge the 5px gap between the toggle and the dropped-down list. Leaflet
// collapses the control on mouseleave of its DOM subtree; a bare gap lets the
// pointer sample outside the control mid-traverse and collapse the list before
// it's reached. A transparent strip filling the gap keeps the pointer inside.
.geo-map .leaflet-control-layers-expanded .leaflet-control-layers-list::before {
  content: "";
  position: absolute;
  top: -5px;
  left: 0;
  right: 0;
  height: 5px;
}

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
        background: var($success);
      }
      &.WARNING,
      &.MINOR,
      &.MAJOR {
        background: var($warning);
      }
      &.CRITICAL {
        background: var($error);
      }
      opacity: 0.7;
    }
  }
}

.leaflet-popup-content-wrapper {
  background: var(--p-content-background);
  color: var(--p-text-muted-color);
}

.leaflet-popup-content {
  min-width: 440px;
}

.flex {
  display: flex;
  width: 100%;
  flex-wrap: wrap;
  > div {
    line-height: 1.5em;
    margin-right: 16px;
    width: calc(60% - 16px);
    flex-grow: 1;
    &:first-child {
      width: calc(40%);
      margin-right: 0;
    }
    > span.alarm-severity {
      font-weight: var($font-semibold);
      text-align: center;
      padding: 2px;

      &.NORMAL {
        background: var($success);
        color: var($state-text-color-on-surface-dark);
      }
      &.WARNING,
      &.MINOR,
      &.MAJOR {
        background: var($warning);
      }
      &.CRITICAL {
        background: var($error);
        color: var($state-text-color-on-surface-dark);
      }
    }
  }
}
</style>
