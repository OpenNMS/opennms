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
        :to="link.to"
        :state="link.state"
        :data-test="link.dataTest"
      >
        <OnmsIcon
          :icon="WHO_ICONS[link.who]"
          class="node-notifications-who-icon"
        />
      </NodeBannerTagLink>
    </span>
  </NodeDetailsBanner>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouteLocationRaw } from 'vue-router'
import { OnmsIcon } from '@opennms/onms-ui'
import Group from '@opennms/onms-ui/icons/action/Group.vue'
import Person from '@opennms/onms-ui/icons/action/Person.vue'
import IconViewDetails from '@opennms/onms-ui/icons/action/ViewDetails.vue'
import { NotificationQueryPreset } from '@/types/notifications'
import NodeBannerTagLink, { NodeBannerTagState } from './NodeBannerTagLink.vue'
import NodeDetailsBanner from './NodeDetailsBanner.vue'
import useActiveNodeId from './hooks/useActiveNodeId'

// A condensed view of the node's notifications, beside the status banner: a tag per link to the
// Notifications page, filtered to this node. It borrows the banner's look with no tint and no severity strip --
// notifications have no severity of their own to signal -- so it has the panels' background.
//
// Each node tag is two icons: whose notifications (yours, or everyone's) and which (outstanding or
// acknowledged). The last tag, all of the node's notifications, is the one icon. The tooltip and
// accessible name say each in words.

// Whose notifications a tag links to: yours, everyone's, or all of the node's whatever their state.
type Who = 'you' | 'everyone' | 'all'

const WHO_ICONS: Record<Who, object> = {
  you: Person,
  everyone: Group,
  all: IconViewDetails
}

interface NotificationsLink {
  dataTest: string
  label: string
  to: RouteLocationRaw
  who: Who
  state?: NodeBannerTagState
}

const nodeId = useActiveNodeId()

// The Notifications page applies the current user to the "your" presets itself.
const forNode = (preset: NotificationQueryPreset) => ({ name: 'Notifications', query: { preset, node: nodeId.value }})

const links = computed<NotificationsLink[]>(() => [
  { dataTest: 'your-outstanding-notifications-link', label: 'Your outstanding notifications for this node', to: forNode('yourOutstanding'), who: 'you', state: 'outstanding' },
  { dataTest: 'your-acknowledged-notifications-link', label: 'Your acknowledged notifications for this node', to: forNode('yourAcknowledged'), who: 'you', state: 'acknowledged' },
  { dataTest: 'all-outstanding-notifications-link', label: 'All outstanding notifications for this node', to: forNode('allOutstanding'), who: 'everyone', state: 'outstanding' },
  { dataTest: 'all-acknowledged-notifications-link', label: 'All acknowledged notifications for this node', to: forNode('allAcknowledged'), who: 'everyone', state: 'acknowledged' },
  // Every notification for this node, outstanding and acknowledged, whoever it was sent to.
  { dataTest: 'view-all-notifications-link', label: 'All notifications for this node', to: forNode('all'), who: 'all' }
])
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

.node-notifications-who-icon {
  color: var(--p-button-text-primary-color);
}
</style>
