<template>
  <NodeDetailsPanel
    v-if="criticalPath"
    title="Path Outage - Critical Path"
    data-test="critical-path-panel"
  >
    <template v-if="adminRole" #actions>
      <OnmsIconButton
        aria-label="Configure Path Outages"
        tooltip="Configure Path Outages"
        data-test="configure-path-outages-button"
        :icon="IconSettings"
        @click="onConfigureClick"
      />
    </template>
    <div data-test="critical-path">{{ criticalPathText }}</div>
  </NodeDetailsPanel>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { OnmsIconButton } from '@opennms/onms-ui'
import IconSettings from '@opennms/onms-ui/icons/action/Settings.vue'
import useRole from '@/composables/useRole'
import { useNodeStore } from '@/stores/nodeStore'
import { NodeCriticalPath } from '@/types'
import NodeDetailsPanel from './NodeDetailsPanel.vue'

// The legacy node page's "Path Outage - Critical Path" box: the node's own critical path, and
// nothing at all for a node without one -- the configured default is not shown there either.
const nodeStore = useNodeStore()
const router = useRouter()
const { adminRole } = useRole()

const criticalPath = computed<NodeCriticalPath | undefined>(() => nodeStore.criticalPath)

const criticalPathText = computed<string>(() => {
  const { criticalPathIp, criticalPathServiceName } = criticalPath.value ?? {}

  return criticalPathServiceName ? `${criticalPathIp} (${criticalPathServiceName})` : `${criticalPathIp ?? ''}`
})

// Critical paths are set in the admin-only Notifications Config page.
const onConfigureClick = () => {
  router.push({ path: '/notifications-config', query: { tab: 'path-outages' }})
}
</script>
