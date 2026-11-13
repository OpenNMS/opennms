<template>
  <NodeScheduledOutagesBox />

  <!-- The node's status on the left, a condensed view of its notifications on the right. -->
  <div class="onms-row node-summary-boxes">
    <div class="onms-col-6">
      <NodeStatusBox />
    </div>
    <div class="onms-col-6">
      <NodeNotificationsBox />
    </div>
  </div>

  <!--
    One row of two columns, each stacking its own panels, rather than a row per pair of panels.
    A row is a grid track, so its height is that of its tallest panel: with a row per pair, a
    short panel left a gap beneath it until the next row could start -- most visibly under
    Notifications, waiting on the much taller Availability panel beside it.

    The panels need no nested .onms-row of their own. They are block-level and fill the column
    already, and the cards carry their own bottom margin, so nesting a fresh 12-column grid per
    panel would add a grid to configure without changing the result.
  -->
  <div class="onms-row">
    <div class="onms-col-6">
      <NodeAvailabilityGraph
        v-if="nodeLoaded"
        :node="node"
        :base-href="baseHref"
      />
      <InterfacesTabs />
    </div>
    <div class="onms-col-6">
      <NodeNotificationsPanel
        v-if="nodeLoaded"
        :node="node"
      />
      <AlarmsTable />
      <NodeCriticalPathPanel v-if="nodeLoaded" />
    </div>
  </div>
</template>

<script setup lang="ts">
import AlarmsTable from '@/components/Nodes/AlarmsTable.vue'
import InterfacesTabs from '@/components/Nodes/InterfacesTabs.vue'
import NodeCriticalPathPanel from '@/components/Nodes/NodeCriticalPathPanel.vue'
import NodeAvailabilityGraph from '@/components/Nodes/NodeAvailabilityGraph.vue'
import NodeNotificationsBox from '@/components/Nodes/NodeNotificationsBox.vue'
import NodeNotificationsPanel from '@/components/Nodes/NodeNotificationsPanel.vue'
import NodeScheduledOutagesBox from '@/components/Nodes/NodeScheduledOutagesBox.vue'
import NodeStatusBox from '@/components/Nodes/NodeStatusBox.vue'
import { Node } from '@/types'

// Panels that read the node wait for a real one (see nodeStore.nodeLoaded); the interface
// tables fetch by node id themselves and do not need it.
defineProps<{
  node: Node
  nodeLoaded: boolean
  baseHref: string
}>()
</script>

<style lang="scss" scoped>
// The two boxes are as tall as each other, whichever has more to say: each fills its column, and
// the row's columns are as tall as the taller one.
.node-summary-boxes > .onms-col-6 {
  display: flex;
  flex-direction: column;

  > * {
    flex: 1;
  }
}
</style>
