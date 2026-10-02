<template>
  <NodeDetailsBanner
    v-if="outages.length > 0"
    severity="critical"
    role="status"
    data-test="node-scheduled-outages"
  >
    <strong>This node is currently affected by the following scheduled outages:</strong>
    {{ ' ' }}
    <template
      v-for="(outage, index) in outages"
      :key="outage.name"
    >
      <router-link
        v-if="adminRole"
        :to="{ path: '/scheduled-outages/edit', query: { name: outage.name } }"
        data-test="node-scheduled-outage-link"
      >{{ outage.name }}</router-link>
      <span
        v-else
        data-test="node-scheduled-outage-name"
      >{{ outage.name }}</span>
      <template v-if="index < outages.length - 1">, </template>
    </template>
  </NodeDetailsBanner>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import useRole from '@/composables/useRole'
import useVisiblePolling from '@/composables/useVisiblePolling'
import { useScheduledOutageStore } from '@/stores/scheduledOutageStore'
import { ScheduledOutage } from '@/types/scheduledOutage'
import NodeDetailsBanner from './NodeDetailsBanner.vue'

// The legacy includes this in the node page only when something applies, and so does this: a
// node with no scheduled outage in effect -- or one whose check failed -- shows nothing. Polled on
// the same cycle as the node's status, since a window can open or close while the page is open.
const POLL_INTERVAL_MS = 60_000

const scheduledOutageStore = useScheduledOutageStore()
const route = useRoute()
const { adminRole } = useRole()

const nodeId = computed(() => route.params.id as string)

// The store's slice is only replaced on success, so it may still be the previous node's.
const outages = computed<ScheduledOutage[]>(() =>
  (scheduledOutageStore.nodeActiveOutagesNodeId === nodeId.value ? scheduledOutageStore.nodeActiveOutages : []))

const fetchOutages = () => {
  if (nodeId.value) {
    scheduledOutageStore.getNodeActiveOutages(nodeId.value)
  }
}

useVisiblePolling(fetchOutages, POLL_INTERVAL_MS)

watch(nodeId, fetchOutages, { immediate: true })

defineExpose({ fetchOutages })
</script>
