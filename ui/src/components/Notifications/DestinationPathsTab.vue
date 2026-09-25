<template>
  <DestinationPathsTable v-if="store.destinationPathEditMode === DestinationPathEditMode.Table" />
  <DestinationPathEditorPanel
    v-else
    :path="store.currentDestinationPath"
    @close="store.closeDestinationPathEditor()"
  />
</template>

<script setup lang="ts">
import DestinationPathEditorPanel from '@/components/Notifications/DestinationPathEditorPanel.vue'
import DestinationPathsTable from '@/components/Notifications/DestinationPathsTable.vue'
import { DestinationPathEditMode, useNotificationConfigStore } from '@/stores/notificationConfigStore'

// Same pattern as SnmpConfigDefinitionsTab: the editor replaces the table on
// the same route, driven by the store's edit mode.
const store = useNotificationConfigStore()
</script>
