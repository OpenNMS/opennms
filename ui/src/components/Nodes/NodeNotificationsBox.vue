<template>
  <NodeDetailsBanner
    :strip="false"
    class="node-notifications-box"
    data-test="node-notifications-box"
  >
    <strong>Notifications</strong>
    <span class="node-notifications-tags">
      <router-link
        v-for="link in links"
        :key="link.dataTest"
        v-onms-tooltip.top="link.label"
        :to="link.to"
        :aria-label="link.label"
        class="node-notifications-tag-link"
        :data-test="link.dataTest"
      >
        <OnmsTag
          severity="secondary"
          class="node-notifications-tag"
        >
          <OnmsIcon
            :icon="link.who"
            class="node-notifications-tag-icon"
          />
          <OnmsIcon
            v-if="link.state"
            :icon="link.state"
            class="node-notifications-tag-icon"
            :class="link.state === Cancel ? 'node-notifications-tag-icon--outstanding' : 'node-notifications-tag-icon--acknowledged'"
          />
        </OnmsTag>
      </router-link>
    </span>
  </NodeDetailsBanner>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouteLocationRaw } from 'vue-router'
import { OnmsIcon, OnmsTag } from '@opennms/onms-ui'
import Cancel from '@opennms/onms-ui/icons/action/Cancel.vue'
import CheckCircle from '@opennms/onms-ui/icons/action/CheckCircle.vue'
import Group from '@opennms/onms-ui/icons/action/Group.vue'
import Person from '@opennms/onms-ui/icons/action/Person.vue'
import IconViewDetails from '@opennms/onms-ui/icons/action/ViewDetails.vue'
import { NotificationQueryPreset } from '@/types/notifications'
import NodeDetailsBanner from './NodeDetailsBanner.vue'
import useActiveNodeId from './hooks/useActiveNodeId'

// A condensed view of the node's notifications, beside the status banner: the Notifications
// panel's links as tags. It borrows the banner's look with no tint and no severity strip --
// notifications have no severity of their own to signal -- so it has the panels' background.
//
// Each node tag is two icons: whose notifications (yours, or everyone's) and which (outstanding or
// acknowledged). The last tag, the panel's View Notifications button, is the one icon. The tooltip
// and accessible name say each in words.

interface NotificationsLink {
  dataTest: string
  label: string
  to: RouteLocationRaw
  who: unknown
  state?: unknown
}

const nodeId = useActiveNodeId()

// The Notifications page applies the current user to the "your" presets itself.
const forNode = (preset: NotificationQueryPreset) => ({ name: 'Notifications', query: { preset, node: nodeId.value }})

const links = computed<NotificationsLink[]>(() => [
  { dataTest: 'your-outstanding-notifications-link', label: 'Your outstanding notifications for this node', to: forNode('yourOutstanding'), who: Person, state: Cancel },
  { dataTest: 'your-acknowledged-notifications-link', label: 'Your acknowledged notifications for this node', to: forNode('yourAcknowledged'), who: Person, state: CheckCircle },
  { dataTest: 'all-outstanding-notifications-link', label: 'All outstanding notifications for this node', to: forNode('allOutstanding'), who: Group, state: Cancel },
  { dataTest: 'all-acknowledged-notifications-link', label: 'All acknowledged notifications for this node', to: forNode('allAcknowledged'), who: Group, state: CheckCircle },
  // Where the panel's View Notifications button went: the Notifications page, not for this node.
  { dataTest: 'view-all-notifications-link', label: 'View All Notifications', to: { name: 'Notifications', query: { preset: 'yourOutstanding' }}, who: IconViewDetails }
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

// The banner underlines its links; here the tag is the link's whole face, so no underline. The
// selector carries the banner's class to outrank its :deep(a) rule.
.node-details-banner .node-notifications-tag-link {
  text-decoration: none;
  border-radius: 6px;

  &:focus-visible {
    outline: 2px solid var(--p-focus-ring-color, currentColor);
    outline-offset: 2px;
  }

  &:hover .node-notifications-tag {
    filter: brightness(1.1);
  }
}

.node-notifications-tag {
  gap: 0.25rem;
}

.node-notifications-tag-icon {
  font-size: 1.5rem;
}

.node-notifications-tag-icon--outstanding {
  color: var(--onms-error);
}

// Dark mode lightens --onms-error to a pink; PrimeVue's red still reads as red there. The whole
// selector goes in :global(): Vue drops whatever follows a :global(...) prefix.
:global(.open-dark .node-notifications-tag-icon--outstanding) {
  color: var(--p-red-500);
}

.node-notifications-tag-icon--acknowledged {
  color: var(--onms-success);
}
</style>
