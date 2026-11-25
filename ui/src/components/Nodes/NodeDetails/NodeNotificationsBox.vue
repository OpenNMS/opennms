<template>
  <NodeDetailsBanner
    :strip="false"
    class="node-notifications-box"
    data-test="node-notifications-box"
  >
    <strong>Notifications</strong>
    <span class="node-notifications-tags">
      <NodeBannerTagLink
        v-for="link in links"
        :key="link.dataTest"
        :label="link.label"
        :to="forNode(link.preset)"
        :data-test="link.dataTest"
      >
        <!-- No number until the counts have loaded, or when they failed to; the link still works. -->
        <span
          v-if="counts"
          class="node-notifications-tag-count"
          :data-test="`${link.dataTest}-count`"
        >{{ counts[link.count] }}</span>
        <OnmsIcon
          :icon="link.icon"
          class="node-notifications-icon"
        />
      </NodeBannerTagLink>
    </span>
  </NodeDetailsBanner>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { OnmsIcon } from '@opennms/onms-ui'
import Group from '@opennms/onms-ui/icons/action/Group.vue'
import Person from '@opennms/onms-ui/icons/action/Person.vue'
import IconViewDetails from '@opennms/onms-ui/icons/action/ViewDetails.vue'
import useVisiblePolling from '@/composables/useVisiblePolling'
import { useNotificationsStore } from '@/stores/notificationsStore'
import { NodeNotificationStatus } from '@/types'
import { NotificationQueryPreset } from '@/types/notifications'
import NodeBannerTagLink from './NodeBannerTagLink.vue'
import NodeDetailsBanner from './NodeDetailsBanner.vue'
import useActiveNodeId from './hooks/useActiveNodeId'

// A condensed view of the node's notifications, beside the status banner: a tag per link to the
// Notifications page, filtered to this node, each with how many notifications it leads to. It
// borrows the banner's look with no tint and no severity strip -- notifications have no severity
// of their own to signal -- so it has the panels' background.
//
// Each tag is a count and an icon for whose notifications: yours, everyone's outstanding, or all
// of the node's. The tooltip and accessible name say it in words. A count of 0 still shows: the
// tags are the way to the Notifications page too.

// How often the counts are refreshed while they are on screen, on the same cycle as the status
// banner beside them. Each refresh is three counts on the server.
const POLL_INTERVAL_MS = 60_000

interface NotificationsLink {
  dataTest: string
  label: string
  preset: NotificationQueryPreset
  count: Exclude<keyof NodeNotificationStatus, 'user'>
  icon: object
}

// The Notifications page applies the current user to the "your" preset itself; the server counts
// for the same user.
const links: NotificationsLink[] = [
  { dataTest: 'your-outstanding-notifications-link', label: 'View your outstanding notifications for this node', preset: 'yourOutstanding', count: 'yourOutstanding', icon: Person },
  { dataTest: 'all-outstanding-notifications-link', label: 'View all outstanding notifications for this node', preset: 'allOutstanding', count: 'allOutstanding', icon: Group },
  // Every notification for this node, outstanding and acknowledged, whoever it was sent to.
  { dataTest: 'view-all-notifications-link', label: 'View all notifications for this node', preset: 'all', count: 'total', icon: IconViewDetails }
]

const notificationsStore = useNotificationsStore()

const nodeId = useActiveNodeId()

const forNode = (preset: NotificationQueryPreset) => ({ name: 'Notifications', query: { preset, node: nodeId.value }})

// The store's slice is only replaced on success, so it may still be the previous node's. A failed
// refresh of this node keeps the counts it had.
const counts = computed<NodeNotificationStatus | undefined>(() =>
  (notificationsStore.nodeNotificationStatusNodeId === nodeId.value ? notificationsStore.nodeNotificationStatus : undefined))

const fetchCounts = async () => {
  if (nodeId.value) {
    await notificationsStore.getNodeNotificationStatus(nodeId.value)
  }
}

useVisiblePolling(fetchCounts, POLL_INTERVAL_MS)

watch(nodeId, fetchCounts, { immediate: true })

defineExpose({ fetchCounts })
</script>

<style lang="scss" scoped>
// Content centred vertically: the box is stretched to the status banner's height beside it.
.node-notifications-box {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.node-notifications-tags {
  display: inline-flex;
  gap: 0.5rem;
}

// As the status banner's counts.
.node-notifications-tag-count {
  padding-right: 0.5rem;
  font-weight: 700;
}

.node-notifications-icon {
  color: var(--p-button-text-primary-color);
}
</style>
