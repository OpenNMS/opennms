<template>
  <EventNotificationsTable v-if="store.eventNotificationEditMode === NotificationConfigEditMode.Table" />
  <EventNotificationEditorPanel
    v-else
    :notification="store.currentEventNotification"
    @close="store.closeEventNotificationEditor()"
  />
</template>

<script setup lang="ts">
import EventNotificationEditorPanel from '@/components/Notifications/EventNotificationEditorPanel.vue'
import EventNotificationsTable from '@/components/Notifications/EventNotificationsTable.vue'
import { NotificationConfigEditMode, useNotificationConfigStore } from '@/stores/notificationConfigStore'

// Same pattern as DestinationPathsTab: the editor replaces the table on the
// same route, driven by the store's edit mode.
const store = useNotificationConfigStore()
</script>
