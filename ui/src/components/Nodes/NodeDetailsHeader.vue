<template>
  <div class="card">
      <div class="node-badge-wrapper">
        <OnmsMenu :model="menuItems" class="node-details-header" />
      </div>
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
import { OnmsMenu, OnmsTag, OnmsTagSeverity } from '@opennms/onms-ui'

import { createLinkItemsList } from './nodeActionLinks'
import { Node } from '@/types'
import { getNodeStatusString } from './utils'

const props = defineProps({
  baseHref: {
    required: true,
    type: String
  },
  node: {
    required: true,
    type: Object as PropType<Node>
  }
})

const menuItems = computed((): any[] => {
  const linkItemsList = createLinkItemsList(props.node)

  const links = linkItemsList
    .map(li => ({
      label: li.label,
      command: () => window.location.assign(`${props.baseHref}${li.link}`)
    }))

  return [
    {
      label: 'Actions',
      items: links
    }
  ]
})

const nodeStatus = computed(() => {
  return getNodeStatusString(props.node)
})

const nodeSeverity = computed(() => {
  const status = nodeStatus.value

  if (status === 'Deleted') {
    return 'danger' as OnmsTagSeverity
  } else if (status === 'Unknown') {
    return 'danger' as OnmsTagSeverity
  } else if (status === 'Active') {
    return 'danger' as OnmsTagSeverity
  } else {
    return 'danger' as OnmsTagSeverity
  }
})
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
