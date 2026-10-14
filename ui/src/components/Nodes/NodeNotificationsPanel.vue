<template>
  <NodeDetailsPanel title="Notifications">
    <template #actions>
      <OnmsIconButton
        aria-label="View Notifications"
        tooltip="View Notifications"
        data-test="notifications-link-button"
        :icon="IconViewDetails"
        @click="router.push({ name: 'Notifications' })"
      />
    </template>
    <div class="onms-row" v-for="link in links" :key="link.dataTest">
      <div class="onms-col-12">
        <router-link
          :data-test="link.dataTest"
          :to="{ name: 'Notifications', query: { preset: link.preset, node: String(node.id) } }"
        >{{ link.label }}</router-link>
      </div>
    </div>
  </NodeDetailsPanel>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { OnmsIconButton } from '@opennms/onms-ui'
import IconViewDetails from '@opennms/onms-ui/icons/action/ViewDetails.vue'
import NodeDetailsPanel from './NodeDetailsPanel.vue'
import { Node } from '@/types'
import { NotificationQueryPreset } from '@/types/notifications'

defineProps<{
  node: Node
}>()

const router = useRouter()

// The Notifications page applies the current user to the "your" presets itself.
const links: { dataTest: string, label: string, preset: NotificationQueryPreset }[] = [
  { dataTest: 'your-outstanding-notifications-link', label: 'Your outstanding notifications for this node', preset: 'yourOutstanding' },
  { dataTest: 'your-acknowledged-notifications-link', label: 'Your acknowledged notifications for this node', preset: 'yourAcknowledged' },
  { dataTest: 'all-outstanding-notifications-link', label: 'All outstanding notifications for this node', preset: 'allOutstanding' },
  { dataTest: 'all-acknowledged-notifications-link', label: 'All acknowledged notifications for this node', preset: 'allAcknowledged' }
]
</script>
