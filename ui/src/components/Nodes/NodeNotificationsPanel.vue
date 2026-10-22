<template>
  <NodeDetailsPanel title="Notifications">
    <template #actions>
      <OnmsIconButton
        aria-label="View Notifications"
        tooltip="View Notifications"
        data-test="notifications-link-button"
        :icon="IconViewDetails"
        @click="onLinkClick"
      />
    </template>
    <div class="onms-row" v-for="link in links" :key="link.dataTest">
      <div class="onms-col-12">
        <a
          :data-test="link.dataTest"
          :href="link.href"
        >{{ link.label }}</a>
      </div>
    </div>
  </NodeDetailsPanel>
</template>

<script setup lang="ts">
import { PropType, computed } from 'vue'
import { OnmsIconButton } from '@opennms/onms-ui'
import IconViewDetails from '@opennms/onms-ui/icons/action/ViewDetails.vue'
import NodeDetailsPanel from './NodeDetailsPanel.vue'
import { useMenuStore } from '@/stores/menuStore'
import { Node } from '@/types'

// Link to the legacy notification browse page which accept query parameters.
// Will need to replace with Vue page once it's implemented.
const NOTIFICATION_BROWSE_PATH = 'notification/browse'

// Link to the legacy notification main page.
// Will need to replace with Vue page once it's implemented.
const NOTIFICATION_INDEX_PATH = 'notification/index.jsp'

const props = defineProps({
  baseHref: {
    required: true,
    type: String
  },
  node: {
    required: true,
    type: Object as PropType<Node>
  }
})

const menuStore = useMenuStore()

const username = computed<string>(() => menuStore.mainMenu.username)

const userFilter = computed<string>(() => `&filter=${encodeURIComponent(`user=${username.value}`)}`)

const nodeFilter = computed<string>(() => `&filter=${encodeURIComponent(`node=${props.node.id}`)}`)

const browseLink = (ackType: 'ack' | 'unack') =>
  `${props.baseHref}${NOTIFICATION_BROWSE_PATH}?acktype=${ackType}${nodeFilter.value}${userFilter.value}`

// Don't display links if the username is not yet available.
const links = computed(() => {
  if (!username.value) {
    return []
  }

  return [
    {
      dataTest: 'outstanding-notifications-link',
      label: 'Your outstanding notifications for this node',
      href: browseLink('unack')
    },
    {
      dataTest: 'acknowledged-notifications-link',
      label: 'Your acknowledged notifications for this node',
      href: browseLink('ack')
    }
  ]
})

const onLinkClick = () => {
  window.location.assign(`${props.baseHref}${NOTIFICATION_INDEX_PATH}`)
}
</script>
