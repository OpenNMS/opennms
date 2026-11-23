<template>
  <NodeDetailsPanel
    title="System Information"
    data-test="node-system-info-panel"
  >
    <div class="onms-row node-system-info-fields">
      <div class="onms-col-6">
        <NodeDetailsFieldList :fields="nameFields" />
      </div>
      <div class="onms-col-6">
        <NodeDetailsFieldList :fields="contactFields" />
      </div>
    </div>
  </NodeDetailsPanel>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Node } from '@/types/index'
import NodeDetailsFieldList from './NodeDetailsFieldList.vue'
import NodeDetailsPanel from '@/components/Nodes/NodeDetailsPanel.vue'
import { NodeDetailsField } from './nodeEnlinkdFields'

// The node's SNMP system group, as the node info dialog (NodeDetailsDialog) shows it, opened
// from the expander beside the page's badges.
const props = defineProps<{
  node: Node
}>()

const EMPTY = '--'

const nameFields = computed<NodeDetailsField[]>(() => [
  { label: 'Sys Name', text: props.node?.sysName || EMPTY },
  { label: 'Sys Object ID', text: props.node?.sysObjectId || EMPTY },
  { label: 'Sys Location', text: props.node?.sysLocation || EMPTY }
])

const contactFields = computed<NodeDetailsField[]>(() => [
  { label: 'Sys Contact', text: props.node?.sysContact || EMPTY },
  { label: 'Sys Description', text: props.node?.sysDescription || EMPTY }
])
</script>

<style lang="scss" scoped>
// Clear of the title, which has no spacing of its own.
.node-system-info-fields {
  margin-top: 1rem;
}
</style>
