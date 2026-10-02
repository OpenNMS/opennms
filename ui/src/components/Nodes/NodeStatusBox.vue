<template>
  <NodeDetailsBanner
    :severity="severity"
    role="status"
    data-test="node-status"
  >
    <template v-if="status">
      <strong data-test="node-status-message">{{ status.message }}</strong>
      <template v-if="status.hasProblems">
        There {{ status.unackCount === 1 ? 'is' : 'are' }}
        <a
          :href="alarmListHref('unack')"
          data-test="node-status-unack-link"
        ><strong>{{ status.unackCount }}</strong> unacknowledged</a>
        {{ problems(status.unackCount) }}, and
        <a
          :href="alarmListHref('ack')"
          data-test="node-status-ack-link"
        ><strong>{{ status.ackCount }}</strong> acknowledged</a>
        {{ problems(status.ackCount) }}.
      </template>
    </template>
    <span
      v-else-if="loadFailed"
      data-test="node-status-unavailable"
    >Node status is unavailable.</span>
    <span v-else>Checking node status&hellip;</span>
  </NodeDetailsBanner>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import useVisiblePolling from '@/composables/useVisiblePolling'
import { useAlarmStore } from '@/stores/alarmStore'
import { useMenuStore } from '@/stores/menuStore'
import NodeDetailsBanner from './NodeDetailsBanner.vue'
import { BannerSeverity, bannerSeverity, computeNodeStatus, NodeStatus } from './nodeStatus'

// How often the status is refreshed while it is on screen. The JSP worked it out once per page
// load; the alarms behind it change without the page knowing, so this one keeps up.
const POLL_INTERVAL_MS = 60_000

const alarmStore = useAlarmStore()
const menuStore = useMenuStore()
const route = useRoute()

const nodeId = computed(() => route.params.id as string)

// Whether the last fetch for this node failed. A failed refresh of a node already on screen
// keeps the status it had -- a minute-old status says more than none -- so this only shows
// when there is nothing for this node to fall back on.
const lastFetchFailed = ref(false)

// The store's node slice is only replaced on success, so it may still be the previous node's.
const isThisNode = computed<boolean>(() => alarmStore.nodeAlarmsNodeId === nodeId.value)

const status = computed<NodeStatus | undefined>(() => (isThisNode.value ? computeNodeStatus(alarmStore.nodeAlarms) : undefined))
const loadFailed = computed<boolean>(() => !isThisNode.value && lastFetchFailed.value)

// Before the first answer there is no severity to show, so the banner stays neutral.
const severity = computed<BannerSeverity>(() => bannerSeverity(status.value?.severity))

const problems = (count: number) => (count === 1 ? 'problem' : 'problems')

// The legacy alarm list, filtered to this node. Will need to point at the Vue Alarms page once
// it's implemented.
const alarmListHref = (ackType: 'ack' | 'unack') =>
  `${menuStore.mainMenu.baseHref}alarm/list.htm?filter=node%3d${nodeId.value}&acktype=${ackType}`

const fetchStatus = async () => {
  const id = nodeId.value

  if (!id) {
    return
  }

  const result = await alarmStore.getNodeAlarms(id)

  // A response for a node the page has since left says nothing about this one.
  if (id === nodeId.value) {
    lastFetchFailed.value = !result.success
  }
}

useVisiblePolling(fetchStatus, POLL_INTERVAL_MS)

watch(nodeId, fetchStatus, { immediate: true })

defineExpose({ fetchStatus })
</script>
