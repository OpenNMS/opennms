<!--
Licensed to The OpenNMS Group, Inc (TOG) under one or more
contributor license agreements.  See the LICENSE.md file
distributed with this work for additional information
regarding copyright ownership.

TOG licenses this file to You under the GNU Affero General
Public License Version 3 (the "License") or (at your option)
any later version.  You may not use this file except in
compliance with the License.  You may obtain a copy of the
License at:

     https://www.gnu.org/licenses/agpl-3.0.txt

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied.  See the License for the specific
language governing permissions and limitations under the
License.
-->

<!--
  Dashboard panel replicating the legacy "Regional Status" map: geolocated nodes
  on a Leaflet/OSM map, markers colored by each node's highest alarm severity.
  Reuses the modern UI's node geo data (/api/v2/nodes assetRecord) + vue-leaflet.
-->
<template>
  <div
    ref="containerRef"
    class="map-panel"
  >
    <LMap
      ref="mapRef"
      :zoom="zoom"
      :center="center"
      :max-zoom="19"
      :min-zoom="1"
      :use-global-leaflet="false"
      @ready="onReady"
    >
      <template v-if="ready">
        <LTileLayer
          :url="tileUrl"
          :attribution="attribution"
        />
        <LMarker
          v-for="m in markers"
          :key="m.id"
          :lat-lng="[m.lat, m.lng]"
        >
          <LIcon
            :icon-url="iconFor(m.severity)"
            :icon-size="[16, 16]"
          />
        </LMarker>
      </template>
    </LMap>
    <div
      v-if="loading"
      class="map-panel__overlay"
    >
      Loading…
    </div>
    <div
      v-else-if="!markers.length"
      class="map-panel__overlay"
    >
      No geolocated nodes.
    </div>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import 'leaflet/dist/leaflet.css'
import { LMap, LTileLayer, LMarker, LIcon } from '@vue-leaflet/vue-leaflet'
import type { Map as LeafletMapType } from 'leaflet'
import API from '@/services'
import { SORT } from '@/types'
import type { Alarm, Node } from '@/types'
import type { PanelComponentProps } from '@/types/dashboard'
import { useGeolocationStore } from '@/stores/geolocationStore'
import { filterFiqlClauses, resolveFilterNodeIds } from '../filter'
import { maxSeverity } from '../severity'
import CriticalIcon from '@/assets/Critical-icon.png'
import MajorIcon from '@/assets/Major-icon.png'
import MinorIcon from '@/assets/Minor-icon.png'
import WarningIcon from '@/assets/Warning-icon.png'
import NormalIcon from '@/assets/Normal-icon.png'

const props = defineProps<PanelComponentProps>()

interface MapMarker {
  id: number
  lat: number
  lng: number
  severity: string
  label: string
}

// Remember the user's pan/zoom across reloads (per browser, per panel — two
// map panels on one dashboard must not fight over a shared key).
const VIEW_KEY = `opennms.dashboard.regionalMap.view.${props.panelId}`
interface SavedView {
  lat: number
  lng: number
  zoom: number
}
const loadSavedView = (): SavedView | null => {
  try {
    const s = localStorage.getItem(VIEW_KEY)
    return s ? (JSON.parse(s) as SavedView) : null
  } catch {
    return null
  }
}
const saved = loadSavedView()

const containerRef = ref<HTMLElement | null>(null)
const mapRef = ref()
const ready = ref(false)
const loading = ref(true)
const markers = ref<MapMarker[]>([])
const center = ref<[number, number]>(saved ? [saved.lat, saved.lng] : [20, 0])
const zoom = ref<number>(saved ? saved.zoom : 2)

let leaflet: LeafletMapType | null = null
let resizeObserver: ResizeObserver | null = null

// The configured tile server (gwt.openlayers.url via the geolocation config
// API) wins, exactly like the main map; OSM is only the fallback.
const geolocationStore = useGeolocationStore()
const tileUrl = ref('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png')
const attribution = ref('&copy; OpenStreetMap contributors')
const resolveTileProvider = async () => {
  await geolocationStore.fetchTileProviders()
  const provider = geolocationStore.tileProviders.find(p => p.visible)
  if (provider?.url) {
    tileUrl.value = provider.url
    attribution.value = provider.attribution ?? ''
  }
}

const saveView = () => {
  if (!leaflet) {
    return
  }
  try {
    const c = leaflet.getCenter()
    localStorage.setItem(VIEW_KEY, JSON.stringify({ lat: c.lat, lng: c.lng, zoom: leaflet.getZoom() }))
  } catch {
    /* ignore storage errors */
  }
}

const iconFor = (severity: string) => {
  switch ((severity || '').toUpperCase()) {
    case 'CRITICAL':
      return CriticalIcon
    case 'MAJOR':
      return MajorIcon
    case 'MINOR':
      return MinorIcon
    case 'WARNING':
      return WarningIcon
    default:
      return NormalIcon
  }
}

const onReady = (mapObject: LeafletMapType) => {
  leaflet = mapObject
  ready.value = true
  // persist pan/zoom as the user moves
  leaflet.on('moveend', saveView)
  leaflet.on('zoomend', saveView)
  // the panel may size after the map mounts; fix tile layout once laid out
  setTimeout(() => leaflet?.invalidateSize(), 150)
  setTimeout(() => leaflet?.invalidateSize(), 600)
}

let loadSeq = 0
const load = async () => {
  const seq = ++loadSeq
  loading.value = true
  // The dashboard filter constrains both queries (node ids from categories +
  // the ip clause); alarms are severity-ordered so the fetch cap keeps the
  // worst problems rather than an arbitrary page. The node id is `id` on the
  // nodes entity and `node.id` on alarms.
  const nodeIds = await resolveFilterNodeIds(props.filter)
  const nodeFiql = filterFiqlClauses(props.filter, nodeIds, 'id').join(';')
  const alarmFiql = filterFiqlClauses(props.filter, nodeIds, 'node.id').join(';')
  const [nodesResp, alarmsResp] = await Promise.all([
    API.getNodes({ limit: 2000, ...(nodeFiql ? { _s: nodeFiql } : {}) }),
    API.getAlarms({ limit: 2000, orderBy: 'severity', order: SORT.DESCENDING, ...(alarmFiql ? { _s: alarmFiql } : {}) })
  ])
  if (seq !== loadSeq) {
    return
  }
  const nodes: Node[] = nodesResp ? nodesResp.node : []
  const alarms: Alarm[] = alarmsResp ? alarmsResp.alarm : []

  // highest alarm severity per node id
  const sevByNode = new Map<number, string>()
  for (const a of alarms) {
    if (a.nodeId == null) {
      continue
    }
    const cur = sevByNode.get(a.nodeId)
    sevByNode.set(a.nodeId, cur ? maxSeverity([cur, a.severity]) : a.severity)
  }

  const result: MapMarker[] = []
  for (const n of nodes) {
    const lat = Number(n.assetRecord?.latitude)
    const lng = Number(n.assetRecord?.longitude)
    const hasCoords = !!n.assetRecord?.latitude && !!n.assetRecord?.longitude && Number.isFinite(lat) && Number.isFinite(lng)
    if (!hasCoords) {
      continue
    }
    const id = Number(n.id)
    result.push({ id, lat, lng, severity: sevByNode.get(id) ?? 'NORMAL', label: n.label })
  }
  markers.value = result
  loading.value = false
  setTimeout(() => leaflet?.invalidateSize(), 50)
}

onMounted(() => {
  resolveTileProvider()
  load()
  if (containerRef.value) {
    resizeObserver = new ResizeObserver(() => leaflet?.invalidateSize())
    resizeObserver.observe(containerRef.value)
  }
})
watch([() => props.refreshTick, () => props.filter], load, { deep: true })
onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  resizeObserver = null
  leaflet = null
})
</script>

<style scoped lang="scss">
.map-panel {
  position: relative;
  height: 100%;
  min-height: 200px;

  :deep(.leaflet-container) {
    height: 100%;
    width: 100%;
    background: #aadaff;
  }

  &__overlay {
    position: absolute;
    top: 0.5rem;
    left: 50%;
    transform: translateX(-50%);
    z-index: 500; // above leaflet tiles
    pointer-events: none;
    padding: 0.25rem 0.75rem;
    border-radius: 1rem;
    background: rgba(255, 255, 255, 0.85);
    color: var(--p-text-muted-color, #555);
    font-size: 0.8125rem;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  }
}
</style>
