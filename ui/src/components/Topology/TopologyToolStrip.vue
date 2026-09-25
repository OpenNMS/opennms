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
  <nav class="topology-rail" aria-label="Panels and tools">
    <span class="topology-rail__panel-item">
      <OnmsIconButton
        :icon="ViewDetails"
        title="Details"
        tooltip="Details of what is selected"
        tooltip-position="right"
        :variant="store.sidePanel === 'details' ? 'filled' : 'text'"
        :aria-pressed="store.sidePanel === 'details'"
        @click="store.toggleSidePanel('details')"
      />
      <!-- A collapsed panel with a selection behind it. -->
      <span v-if="store.sidePanel !== 'details' && store.selectedIds.length > 0" class="topology-rail__dot" />
    </span>
    <template v-if="editing">
      <OnmsIconButton
        :icon="Nodes"
        title="Place nodes"
        tooltip="Nodes to place on the canvas"
        tooltip-position="right"
        :variant="store.sidePanel === 'palette' ? 'filled' : 'text'"
        :aria-pressed="store.sidePanel === 'palette'"
        @click="store.toggleSidePanel('palette')"
      />
      <span class="topology-rail__gap" />
      <OnmsIconButton
        :icon="SelectToolIcon"
        title="Select"
        tooltip="Select and move (Esc)"
        tooltip-position="right"
        :variant="selectActive ? 'filled' : 'text'"
        :aria-pressed="selectActive"
        @click="selectTool"
      />
      <OnmsIconButton
        :icon="Link"
        title="Draw link"
        tooltip="Draw a link: click one node, then another"
        tooltip-position="right"
        :variant="store.isLinkDrawMode ? 'filled' : 'text'"
        :aria-pressed="store.isLinkDrawMode"
        @click="store.setLinkDrawMode(!store.isLinkDrawMode)"
      />
      <OnmsIconButton
        :icon="BoxToolIcon"
        title="Draw box"
        tooltip="Draw a box: drag on the canvas"
        tooltip-position="right"
        :variant="store.isShapeDrawMode ? 'filled' : 'text'"
        :aria-pressed="store.isShapeDrawMode"
        @click="store.setShapeDrawMode(!store.isShapeDrawMode)"
      />
      <span class="topology-rail__gap" />
      <OnmsIconButton
        :icon="View"
        title="Link hints"
        tooltip="Show discovered adjacencies between placed nodes as ghost links"
        tooltip-position="right"
        :variant="store.isLinkHintsEnabled ? 'filled' : 'text'"
        :aria-pressed="store.isLinkHintsEnabled"
        @click="store.setLinkHintsEnabled(!store.isLinkHintsEnabled)"
      />
      <OnmsIconButton
        v-if="hasBackground"
        :icon="Image"
        title="Adjust background"
        tooltip="Move and resize the background image"
        tooltip-position="right"
        :variant="store.isBackgroundAdjustMode ? 'filled' : 'text'"
        :aria-pressed="store.isBackgroundAdjustMode"
        @click="store.setBackgroundAdjustMode(!store.isBackgroundAdjustMode)"
      />
    </template>
    <span class="topology-rail__gap" />
    <OnmsIconButton
      :icon="Info"
      title="Counts"
      tooltip="Show the node, link, label and selection counts"
      tooltip-position="right"
      :variant="store.showCanvasStats ? 'filled' : 'text'"
      :aria-pressed="store.showCanvasStats"
      @click="store.setShowCanvasStats(!store.showCanvasStats)"
    />
  </nav>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { OnmsIconButton } from '@opennms/onms-ui'
import Link from '@opennms/onms-ui/icons/action/Link.vue'
import View from '@opennms/onms-ui/icons/action/View.vue'
import ViewDetails from '@opennms/onms-ui/icons/action/ViewDetails.vue'
import Image from '@opennms/onms-ui/icons/file/Image.vue'
import Info from '@opennms/onms-ui/icons/action/Info.vue'
import Nodes from '@opennms/onms-ui/icons/network/Nodes.vue'
import { BoxToolIcon, SelectToolIcon } from '@/components/Topology/toolIcons'
import { useTopologyStore } from '@/stores/topologyStore'

// The rail: panel pages at the top, Edit tools below. Always present, so the
// Details page is one click away in View mode too.
const store = useTopologyStore()

const editing = computed<boolean>(() => store.isEditMode && store.discoveredGraph === null)

// Select is the state with no drawing or adjusting mode on; it never toggles
// itself off, it turns the others off.
const selectActive = computed<boolean>(
  () => !store.isLinkDrawMode && !store.isShapeDrawMode && !store.isBackgroundAdjustMode
)

const selectTool = () => {
  store.setLinkDrawMode(false)
  store.setShapeDrawMode(false)
  store.setBackgroundAdjustMode(false)
}

const hasBackground = computed<boolean>(() => !!store.background?.ref)
</script>

<style scoped>
.topology-rail {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  padding: 0.25rem;
  background: var(--onms-surface);
  border: 1px solid var(--onms-border-on-surface);
  border-radius: 4px;
  align-self: stretch;
}

/* Every item is the same box whatever its variant: PrimeVue's text, outlined
   and filled buttons differ by a border and padding, which read as uneven
   spacing in a column. */
.topology-rail :deep(.p-button) {
  width: 2.25rem;
  height: 2.25rem;
  padding: 0;
  justify-content: center;
  box-sizing: border-box;
}

.topology-rail__gap {
  height: 0.5rem;
  flex: 0 0 auto;
}

.topology-rail__panel-item {
  position: relative;
  display: flex;
}

.topology-rail__dot {
  position: absolute;
  top: 0.35rem;
  right: 0.35rem;
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 50%;
  background: var(--onms-topology-accent);
  pointer-events: none;
}
</style>
