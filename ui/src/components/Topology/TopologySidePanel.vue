<!--
//
// OpenNMS's Vaadin map derives a node's icon from its SNMP sysObjectId:
//   enlinkd Topology.getIconKey(node) -> "linkd.system" (no sysObjectId) or
//   "linkd.system.snmp.<sysObjectId>", which IconManager resolves to an SVG id
//   via a longest-prefix match against etc/org.opennms.features.topology.app.icons.linkd.cfg
//   (default "linkd.system" -> generic). We reproduce that here client-side:
//   the discovered Graph API already carries the computed iconKey on each
//   vertex, and for custom-view nodes we compute it from the node's sysObjectId.
//
// ROADMAP (overrides, not yet built): the legacy map also lets an operator
// override a vertex's icon manually (IconSelectionOperation -> per-vertex
// mapping). We should add, down the road: (1) icon overrides by node *category*
// (a more human-meaningful signal than raw OID), and (2) a custom per-node icon
// override persisted on the view. Both layer on top of this sysObjectId default.
// See topology_redesign/PARITY.md.

/** The recognized device-type icon ids we render a glyph for. */
export type DeviceIconId =
  | 'router'
-->
<template>
  <div v-if="store.sidePanel" class="topology-side-panel" :style="{ width: panelWidth + 'px' }">
    <TopologyPalette v-if="store.sidePanel === 'palette'" class="topology-side-panel__page" />
    <TopologyInspector v-else :canvas="canvas" class="topology-side-panel__page" />
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
