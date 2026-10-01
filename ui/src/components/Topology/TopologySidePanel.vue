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
  <div v-if="store.sidePanel" class="topology-side-panel" :style="{ width: panelWidth + 'px' }">
    <!-- Both pages stay mounted: the Inspector keeps its fetched node detail
         and assets across a switch, and nothing reflows on the way back. -->
    <TopologyPalette v-show="store.sidePanel === 'palette'" class="topology-side-panel__page" />
    <TopologyInspector v-show="store.sidePanel === 'details'" :canvas="canvas" class="topology-side-panel__page" />
    <!-- Drag strip on the canvas-facing edge. Width persists per browser. -->
    <div class="topology-side-panel__handle" title="Drag to resize" @mousedown.prevent="startResize" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import TopologyInspector, { type CanvasLinkApi } from '@/components/Topology/TopologyInspector.vue'
import TopologyPalette from '@/components/Topology/TopologyPalette.vue'
import { useTopologyStore } from '@/stores/topologyStore'

// The one panel beside the rail. Which page it shows is the store's; the
// width is shared by both pages so switching does not reflow the canvas.
defineProps<{
  canvas: CanvasLinkApi | null
}>()

const store = useTopologyStore()

const WIDTH_STORAGE_KEY = 'opennms.topology.sidePanelWidth'
const WIDTH_MIN = 240
const WIDTH_MAX = 640
const WIDTH_DEFAULT = 300

const readWidth = (): number => {
  try {
    const stored = Number(localStorage.getItem(WIDTH_STORAGE_KEY))
    return Number.isFinite(stored) && stored >= WIDTH_MIN && stored <= WIDTH_MAX ? stored : WIDTH_DEFAULT
  } catch {
    return WIDTH_DEFAULT
  }
}

const panelWidth = ref<number>(readWidth())

let resizeStart: { x: number; width: number } | null = null
const onResizeMove = (e: MouseEvent) => {
  if (!resizeStart) {
    return
  }
  panelWidth.value = Math.min(WIDTH_MAX, Math.max(WIDTH_MIN, resizeStart.width + (e.clientX - resizeStart.x)))
}
const endResize = () => {
  if (!resizeStart) {
    return
  }
  resizeStart = null
  window.removeEventListener('mousemove', onResizeMove)
  window.removeEventListener('mouseup', endResize)
  document.body.style.userSelect = ''
  try {
    localStorage.setItem(WIDTH_STORAGE_KEY, String(panelWidth.value))
  } catch {
    // nothing to do
  }
}
const startResize = (e: MouseEvent) => {
  resizeStart = { x: e.clientX, width: panelWidth.value }
  document.body.style.userSelect = 'none'
  window.addEventListener('mousemove', onResizeMove)
  window.addEventListener('mouseup', endResize)
}
</script>

<style scoped>
.topology-side-panel {
  position: relative;
  flex: 0 0 auto;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.topology-side-panel__page {
  width: 100%;
  flex: 1 1 auto;
  min-height: 0;
}

.topology-side-panel__handle {
  position: absolute;
  top: 0;
  bottom: 0;
  right: -3px;
  width: 6px;
  cursor: col-resize;
  z-index: 2;
}

.topology-side-panel__handle:hover {
  background: var(--onms-border-on-surface);
}
</style>
