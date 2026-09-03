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
<template>
  <div v-if="tileUrl" ref="frame" class="tlm" :class="{ 'tlm-dark': isDark }">
    <LMap
      ref="mapRef"
      :zoom="ZOOM"
      :center="center"
      :use-global-leaflet="false"
      :options="{
        zoomControl: false,
        attributionControl: true,
        scrollWheelZoom: false,
        dragging: false,
        doubleClickZoom: false,
        keyboard: false
      }"
    >
      <LTileLayer :url="tileUrl" :attribution="attribution" />
      <!-- An explicit icon, because Leaflet's default one locates its images by
           parsing a CSS background-image URL, and Vite inlines marker-icon.png
           as a data URI in a packaged build, which that parser cannot read. The
           marker then requests "marker-icon.png" relative to the page and 404s.
           The sibling geomap passes an explicit icon for the same reason. -->
      <LMarker :lat-lng="center">
        <LIcon
          :icon-url="markerIconUrl"
          :icon-retina-url="markerIconRetinaUrl"
          :shadow-url="markerShadowUrl"
          :icon-size="[25, 41]"
          :icon-anchor="[12, 41]"
          :shadow-size="[41, 41]"
        />
      </LMarker>
    </LMap>
  </div>
  <p v-else class="tlm-empty">{{ status }}</p>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import 'leaflet/dist/leaflet.css'
import { LIcon, LMap, LMarker, LTileLayer } from '@vue-leaflet/vue-leaflet'
import markerIconUrl from 'leaflet/dist/images/marker-icon.png'
import markerIconRetinaUrl from 'leaflet/dist/images/marker-icon-2x.png'
import markerShadowUrl from 'leaflet/dist/images/marker-shadow.png'
import { getGeolocationConfig } from '@/services/geolocationService'
import { useAppStore } from '@/stores/appStore'

/**
 * A small map of one node's position, for the inspector. Matches what the Vaadin
 * map put in its info panel, including the zoom level.
 *
 * Renders nothing without a tile server: the URL is operator-configured and an
 * install that never set one would otherwise show an empty grey box.
 */
const props = defineProps<{ lat: number, lon: number }>()

const ZOOM = 10 // the legacy info-panel map's withInitialZoom(10)

/**
 * No dark basemap exists to switch to -- every provider is light -- so the tiles
 * are inverted in CSS, hue-rotate restoring the hues afterwards.
 */
const appStore = useAppStore()
const isDark = computed<boolean>(() => appStore.theme === 'open-dark')

const tileUrl = ref<string>('')
const attribution = ref<string>('')
const failed = ref(false)

const center = computed<[number, number]>(() => [props.lat, props.lon])

const status = computed<string>(() =>
  failed.value ? 'No tile server is configured, so the map cannot be drawn.' : 'Loading map\u2026')


// Leaflet caches its container size and re-reads it only on invalidateSize(),
// and this panel is user-resizable. LeafletMap.vue carries the same observer.
const frame = ref<HTMLElement | null>(null)
const mapRef = ref<{ leafletObject?: { invalidateSize?: () => void }} | null>(null)
let observer: ResizeObserver | null = null

const revalidateSize = () => {
  const map = mapRef.value?.leafletObject
  if (typeof map?.invalidateSize === 'function') {
    map.invalidateSize()
  }
}

// The frame only exists once a tile server resolved, so observe it when it appears.
watch(frame, (el) => {
  observer?.disconnect()
  observer = null
  if (!el) {
    return
  }
  observer = new ResizeObserver(() => revalidateSize())
  observer.observe(el)
})

onBeforeUnmount(() => {
  observer?.disconnect()
  observer = null
})

onMounted(async () => {
  // getGeolocationConfig caches per install, so mounting this for every node
  // selection costs one request in total rather than one per selection.
  const config = await getGeolocationConfig()
  if (config === false || !config.tileServerUrl) {
    failed.value = true
    return
  }
  tileUrl.value = config.tileServerUrl
  attribution.value = config.options?.attribution ?? ''
})
</script>

<style scoped>
.tlm {
  /* The Vaadin widget was a fixed 300x300; square at up to 300px keeps those
     proportions while still fitting an inspector narrower than that. */
  aspect-ratio: 1 / 1;
  max-width: 300px;
  width: 100%;
  border: 1px solid var(--onms-border-on-surface);
  border-radius: 4px;
  overflow: hidden;
}

.tlm-dark :deep(.leaflet-tile) {
  filter: invert(1) hue-rotate(180deg) brightness(0.92) contrast(0.9) saturate(0.85);
}

/* Leaflet hardcodes a white attribution strip, which stays white through the
   tile filter above because it is not a tile. */
.tlm-dark :deep(.leaflet-control-attribution) {
  background: rgba(28, 32, 48, 0.75);
  color: var(--onms-secondary-text-on-surface);
}

.tlm-dark :deep(.leaflet-control-attribution a) {
  color: var(--onms-secondary-text-on-surface);
}

.tlm-empty {
  margin: 0;
  font-size: 0.85em;
  color: var(--onms-secondary-text-on-surface);
}
</style>
