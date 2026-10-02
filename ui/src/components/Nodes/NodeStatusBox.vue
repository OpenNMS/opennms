<template>
  <div
    class="node-status"
    :class="`node-status--${bannerSeverity}`"
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
  </div>
</template>

<script setup lang="ts">
import { computed, onActivated, onDeactivated, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useDocumentVisibility, useIntervalFn } from '@vueuse/core'
import { useAlarmStore } from '@/stores/alarmStore'
import { useMenuStore } from '@/stores/menuStore'
import { computeNodeStatus, NodeStatus } from './nodeStatus'

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
const bannerSeverity = computed<string>(() => (status.value ? status.value.severity.toLowerCase() : 'none'))

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

const visibility = useDocumentVisibility()
const { pause, resume } = useIntervalFn(fetchStatus, POLL_INTERVAL_MS, { immediate: false })

// Poll only while someone can see it: not while the browser tab is hidden, and not while the
// Main tab is switched away (KeepAlive keeps this component, and its timer, alive).
const active = ref(true)

watch([active, visibility], ([isActive, vis]) => {
  if (isActive && vis === 'visible') {
    resume()
  } else {
    pause()
  }
}, { immediate: true })

onActivated(() => {
  // Coming back after a while: refresh now rather than up to a minute from now.
  if (!active.value) {
    fetchStatus()
  }

  active.value = true
})

onDeactivated(() => {
  active.value = false
})

watch(nodeId, fetchStatus, { immediate: true })

defineExpose({ fetchStatus })
</script>

<style lang="scss" scoped>
@use '@/styles/onms-tokens' as variables;
@use '@/styles/onms-color-utils' as utils;

.node-status {
  border: 1px solid var(--p-content-border-color);
  border-left-width: 6px;
  border-radius: 5px;
  padding: 0.75em 1em;
  margin-bottom: 15px;
  background: var(--p-content-background);

  a {
    color: inherit;
    text-decoration: underline;
  }
}

$severity-tokens: (
  'normal': variables.$success,
  'warning': variables.$warning,
  'minor': variables.$minor,
  'major': variables.$major,
  'critical': variables.$error
);

@each $name, $token in $severity-tokens {
  .node-status--#{$name} {
    border-left-color: var(#{$token});
    background: utils.alpha($token, 0.2);
  }
}
</style>
