<template>
  <NodeDetailsBanner
    v-if="names.length > 0"
    severity="critical"
    role="status"
    data-test="node-scheduled-outages"
  >
    <strong>This node is currently affected by the following scheduled outages:</strong>
    {{ ' ' }}
    <template
      v-for="(name, index) in names"
      :key="name"
    >
      <router-link
        v-if="adminRole"
        :to="{ path: '/scheduled-outages/edit', query: { name } }"
        data-test="node-scheduled-outage-link"
      >{{ name }}</router-link>
      <span
        v-else
        data-test="node-scheduled-outage-name"
      >{{ name }}</span>
      <template v-if="index < names.length - 1">, </template>
    </template>
  </NodeDetailsBanner>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import useRole from '@/composables/useRole'
import useVisiblePolling from '@/composables/useVisiblePolling'
import { useScheduledOutageStore } from '@/stores/scheduledOutageStore'
import NodeDetailsBanner from './NodeDetailsBanner.vue'
import useActiveNodeId from './hooks/useActiveNodeId'

// The legacy includes this in the node page only when something applies, and so does this: a
// node with no scheduled outage in effect -- or one whose check failed -- shows nothing. Polled on
// the same cycle as the node's status, since a window can open or close while the page is open.
const POLL_INTERVAL_MS = 60_000

const scheduledOutageStore = useScheduledOutageStore()
const { adminRole } = useRole()

const nodeId = useActiveNodeId()

// The store's slice is only replaced on success, so it may still be the previous node's.
const names = computed<string[]>(() =>
  (scheduledOutageStore.nodeActiveOutagesNodeId === nodeId.value ? scheduledOutageStore.nodeActiveOutageNames : []))

const fetchOutages = () => {
  if (nodeId.value) {
    scheduledOutageStore.getNodeActiveOutages(nodeId.value)
  }
}

useVisiblePolling(fetchOutages, POLL_INTERVAL_MS)

watch(nodeId, fetchOutages, { immediate: true })

defineExpose({ fetchOutages })
</script>
