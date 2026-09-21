<template>
  <div class="card">
      <div class="node-badge-wrapper">
        <OnmsTag class="node-chip" :value="`Status: ${nodeStatus}`" :severity="nodeSeverity" />
      </div>
      <div class="node-badge-wrapper">
        <OnmsTag class="node-chip" :value="`Label: ${props.node?.label}`" severity="info" />
      </div>
      <div class="node-badge-wrapper">
        <OnmsTag class="node-chip" :value="`ID: ${props.node?.id}`" severity="info" />
      </div>
      <div class="node-badge-wrapper">
        <OnmsTag class="node-chip" :value="`Monitoring Location: ${props.node?.location}`" severity="info" />
      </div>
  </div>
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue'
import { OnmsTag, OnmsTagSeverity } from '@opennms/onms-ui'

import { Node } from '@/types'
import { getNodeStatusString } from './utils'

const props = defineProps({
  node: {
    required: true,
    type: Object as PropType<Node>
  }
})

const nodeStatus = computed(() => {
  return getNodeStatusString(props.node)
})

// getNodeStatusString only ever returns these three, but anything it does not recognise reads
// as unknown rather than as a problem.
const SEVERITY_BY_STATUS: Record<string, OnmsTagSeverity> = {
  Active: 'success',
  Deleted: 'danger',
  Unknown: 'info'
}

const nodeSeverity = computed<OnmsTagSeverity>(() => SEVERITY_BY_STATUS[nodeStatus.value] ?? 'info')
</script>

<style lang="scss" scoped>
.card {
  padding: 1rem;
  margin-bottom: 1rem;

  .node-badge-wrapper {
    display: inline-block;
    margin-right: 0.5rem;
  }
}
</style>
