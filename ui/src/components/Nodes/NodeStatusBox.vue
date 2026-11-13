<template>
  <NodeDetailsBanner
    :severity="severity"
    role="status"
    data-test="node-status"
  >
    <template v-if="status">
      <strong data-test="node-status-message">{{ status.message }}</strong>
      <!--
        A tag per count, each a link to the node's alarms in the legacy list. A count of 0 has no
        tag; acknowledged alarms show even when nothing is left unacknowledged.
      -->
      <span
        v-if="status.unackCount > 0 || status.ackCount > 0"
        class="node-status-tags"
      >
        <a
          v-if="status.unackCount > 0"
          v-onms-tooltip.top="countLabel(status.unackCount, 'unacknowledged')"
          :href="alarmListHref('unack')"
          :aria-label="countLabel(status.unackCount, 'unacknowledged')"
          class="node-status-tag-link"
          data-test="node-status-unack-link"
        >
          <OnmsTag
            severity="warn"
            class="node-status-tag"
          >
            <span class="node-status-tag-count">{{ status.unackCount }}</span>
            <OnmsIcon
              :icon="Cancel"
              class="node-status-tag-icon node-status-tag-icon--unack"
            />
          </OnmsTag>
        </a>
        <a
          v-if="status.ackCount > 0"
          v-onms-tooltip.top="countLabel(status.ackCount, 'acknowledged')"
          :href="alarmListHref('ack')"
          :aria-label="countLabel(status.ackCount, 'acknowledged')"
          class="node-status-tag-link"
          data-test="node-status-ack-link"
        >
          <OnmsTag
            severity="success"
            class="node-status-tag"
          >
            <span class="node-status-tag-count">{{ status.ackCount }}</span>
            <OnmsIcon
              :icon="CheckCircle"
              class="node-status-tag-icon node-status-tag-icon--ack"
            />
          </OnmsTag>
        </a>
      </span>
    </template>
    <span
      v-else-if="loadFailed"
      data-test="node-status-unavailable"
    >Node status is unavailable.</span>
    <span v-else>Checking node status&hellip;</span>
  </NodeDetailsBanner>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { OnmsIcon, OnmsTag } from '@opennms/onms-ui'
import Cancel from '@opennms/onms-ui/icons/action/Cancel.vue'
import CheckCircle from '@opennms/onms-ui/icons/action/CheckCircle.vue'
import useVisiblePolling from '@/composables/useVisiblePolling'
import { useAlarmStore } from '@/stores/alarmStore'
import { useMenuStore } from '@/stores/menuStore'
import NodeDetailsBanner from './NodeDetailsBanner.vue'
import { BannerSeverity, bannerSeverity, computeNodeStatus, NodeStatus } from './nodeStatus'
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
const countLabel = (count: number, kind: 'unacknowledged' | 'acknowledged') =>
  `${count} ${kind} ${count === 1 ? 'alarm' : 'alarms'}`

// The legacy alarm list, filtered to this node. Will need to point at the Vue Alarms page once
// it's implemented.
const alarmListHref = (ackType: 'ack' | 'unack') =>
  `${menuStore.mainMenu.baseHref}alarm/list.htm?filter=node%3d${nodeId.value}&acktype=${ackType}`

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
.node-status-tags {
  display: inline-flex;
  gap: 0.5rem;
  margin-left: 0.75rem;
  vertical-align: middle;
}

// The banner underlines its links; here the tag is the link's whole face, so no underline. The
// selector carries the banner's class to outrank its :deep(a) rule.
.node-details-banner .node-status-tag-link {
  text-decoration: none;
  border-radius: 6px;

  &:focus-visible {
    outline: 2px solid var(--p-focus-ring-color, currentColor);
    outline-offset: 2px;
  }

  &:hover .node-status-tag {
    filter: brightness(1.1);
  }
}

.node-status-tag {
  gap: 0.3rem;
}

.node-status-tag-count {
  font-weight: 700;
}

.node-status-tag-icon {
  font-size: 1rem;
}

.node-status-tag-icon--unack {
  color: var(--onms-error);
}

// Dark mode lightens --onms-error to a pink; PrimeVue's red still reads as red there. The whole
// selector goes in :global(): Vue drops whatever follows a :global(...) prefix.
:global(.open-dark .node-status-tag-icon--unack) {
  color: var(--p-red-500);
}

.node-status-tag-icon--ack {
  color: var(--onms-success);
}
</style>
