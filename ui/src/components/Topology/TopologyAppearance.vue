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
  <div class="topology-appearance" role="group" aria-label="Appearance">
    <div class="topology-appearance__row">
      <span class="topology-appearance__label" aria-hidden="true">Node size</span>
      <OnmsSlider
        v-model="nodeSize"
        :min="store.NODE_SIZE_MIN"
        :max="store.NODE_SIZE_MAX"
        class="topology-appearance__slider"
        aria-label="Node size"
      />
      <span class="topology-appearance__value">{{ store.nodeSize }}</span>
    </div>
    <div class="topology-appearance__row">
      <span class="topology-appearance__label" aria-hidden="true">Link width</span>
      <OnmsSlider
        v-model="linkWidth"
        :min="store.LINK_WIDTH_MIN"
        :max="store.LINK_WIDTH_MAX"
        class="topology-appearance__slider"
        aria-label="Link width"
      />
      <span class="topology-appearance__value">{{ store.linkWidth }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { OnmsSlider } from '@opennms/onms-ui'
import { useTopologyStore } from '@/stores/topologyStore'

// How the map reads: sizes that apply to every node and link. They save with
// a custom view through the store; a discovered graph keeps them for the session.
const store = useTopologyStore()

const nodeSize = computed<number>({
  get: () => store.nodeSize,
  set: n => store.setNodeSize(n)
})

const linkWidth = computed<number>({
  get: () => store.linkWidth,
  set: n => store.setLinkWidth(n)
})
</script>

<style scoped>
.topology-appearance {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  min-width: 16rem;
  padding: 0.25rem;
}

.topology-appearance__row {
  display: grid;
  grid-template-columns: 5.5rem 1fr 2rem;
  align-items: center;
  gap: 0.75rem;
}

.topology-appearance__label {
  color: var(--onms-secondary-text-on-surface);
  font-size: 0.875rem;
}

.topology-appearance__value {
  text-align: right;
  font-variant-numeric: tabular-nums;
  color: var(--onms-secondary-text-on-surface);
  font-size: 0.875rem;
}
</style>
