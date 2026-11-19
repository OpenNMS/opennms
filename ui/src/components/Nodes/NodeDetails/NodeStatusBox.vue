<template>
  <NodeDetailsBanner
    :severity="severity"
    role="status"
    data-test="node-status"
  >
    <template v-if="status">
      <SeverityIcon
        :severity="status.severity"
        class="node-status-icon"
        data-test="node-status-icon"
      />
      <strong data-test="node-status-message">{{ status.message }}</strong>
      <!--
        A tag per severity with unacknowledged alarms, highest first: the count and the severity's
        icon, each a link to those alarms in the legacy list. A severity with none has no tag.
      -->
      <span
        v-if="status.unackBySeverity.length > 0"
        class="node-status-tags"
      >
        <NodeBannerTagLink
          v-for="item in status.unackBySeverity"
          :key="item.severity"
          :label="countLabel(item)"
          :href="alarmListHref(item.severity)"
          :data-test="`node-status-unack-${item.severity.toLowerCase()}-link`"
        >
          <span class="node-status-tag-count">{{ item.count }}</span>
          <SeverityIcon :severity="item.severity" />
        </NodeBannerTagLink>
      </span>
    </template>
    <template v-else-if="loadFailed">
      <!-- With no status, the severity is not known: the "?" icon. -->
      <SeverityIcon
        class="node-status-icon"
        data-test="node-status-icon"
      />
      <span data-test="node-status-unavailable">Node status is unavailable.</span>
    </template>
    <span v-else>Checking node status&hellip;</span>
  </NodeDetailsBanner>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { severityId, severityName } from '@/lib/severity'
import { plural } from '@/lib/utils'
import useVisiblePolling from '@/composables/useVisiblePolling'
import SeverityIcon from '@/components/Common/SeverityIcon.vue'
import { useAlarmStore } from '@/stores/alarmStore'
import { useMenuStore } from '@/stores/menuStore'
import NodeBannerTagLink from './NodeBannerTagLink.vue'
import NodeDetailsBanner from './NodeDetailsBanner.vue'
import { BannerSeverity, bannerSeverity, computeNodeStatus, NodeStatus, SeverityCount } from './nodeStatus'
import useActiveNodeId from './hooks/useActiveNodeId'

// How often the status is refreshed while it is on screen. The JSP worked it out once per page
// load; the alarms behind it change without the page knowing, so this one keeps up. Each refresh
// is a handful of counts on the server, not the node's alarms.
const POLL_INTERVAL_MS = 60_000

const alarmStore = useAlarmStore()
const menuStore = useMenuStore()

const nodeId = useActiveNodeId()

// The store's node slice is only replaced on success, so it may still be the previous node's.
const isThisNode = computed<boolean>(() => alarmStore.nodeAlarmStatusNodeId === nodeId.value)

const status = computed<NodeStatus | undefined>(() =>
  (isThisNode.value && alarmStore.nodeAlarmStatus ? computeNodeStatus(alarmStore.nodeAlarmStatus) : undefined))

// A failed refresh of a node already on screen keeps the status it had -- a minute-old status
// says more than none -- so this only shows when there is nothing for this node to fall back on.
// The failure is the store's, kept by node id: another node's failure never shows here.
const loadFailed = computed<boolean>(() => !isThisNode.value && alarmStore.nodeAlarmStatusFailedNodeId === nodeId.value)

// Before the first answer there is no severity to show, so the banner stays neutral.
const severity = computed<BannerSeverity>(() => bannerSeverity(status.value?.severity))

// The tag shows only a number and an icon, so its tooltip and accessible name say what it counts.
const countLabel = ({ severity, count }: SeverityCount) =>
  plural(count, `unacknowledged ${severityName(severity).toLowerCase()} alarm`)

// The legacy alarm list, filtered to this node's unacknowledged alarms at one severity. Will need
// to point at the Vue Alarms page once it's implemented.
const alarmListHref = (severity: string) =>
  `${menuStore.mainMenu.baseHref}alarm/list.htm?filter=node%3d${nodeId.value}&filter=severity%3d${severityId(severity)}&acktype=unack`

const fetchStatus = async () => {
  if (nodeId.value) {
    await alarmStore.getNodeAlarmStatus(nodeId.value)
  }
}

useVisiblePolling(fetchStatus, POLL_INTERVAL_MS)

watch(nodeId, fetchStatus, { immediate: true })

defineExpose({ fetchStatus })
</script>

<style lang="scss" scoped>
.node-status-icon {
  margin-right: 0.5rem;
}

.node-status-tags {
  display: inline-flex;
  gap: 0.5rem;
  margin-left: 0.75rem;
  vertical-align: middle;
}

.node-status-tag-count {
  font-weight: 700;
}
</style>
