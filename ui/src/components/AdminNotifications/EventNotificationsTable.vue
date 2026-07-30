<template>
  <TableCard class="event-notifications-table">
    <div class="header">
      <div class="card-title">Event Notifications</div>
      <Button
        outlined
        label="Add New Event Notification"
        icon="pi pi-plus"
        data-test="add-event-notification-button"
        @click="openEditor(null)"
      />
    </div>

    <DataTable
      v-if="store.eventNotifications.length"
      :value="store.eventNotifications"
      paginator
      dataKey="name"
      :rows="10"
      :rowsPerPageOptions="[10, 20, 50, 100]"
      class="data-table"
      data-test="event-notifications-table"
    >
      <Column
        field="status"
        header="Status"
        sortable
      >
        <template #body="{ data }">
          <PToggleSwitch
            :modelValue="data.status === 'on'"
            :disabled="pendingName === data.name"
            :aria-label="`Turn ${data.name} ${data.status === 'on' ? 'off' : 'on'}`"
            data-test="notification-status-toggle"
            @update:modelValue="(value: boolean) => onToggle(data, value)"
          />
        </template>
      </Column>
      <Column
        field="name"
        header="Notification"
        sortable
      />
      <Column
        field="uei"
        header="UEI"
        sortable
      />
      <Column
        field="destinationPath"
        header="Destination Path"
        sortable
      />
      <Column header="Actions">
        <template #body="{ data }">
          <div class="action-container">
            <Button
              text
              label="Edit"
              :aria-label="`Edit ${data.name}`"
              data-test="edit-event-notification-button"
              @click="openEditor(data)"
            />
            <Button
              text
              label="Delete"
              severity="danger"
              :aria-label="`Delete ${data.name}`"
              data-test="delete-event-notification-button"
              @click="askDelete(data)"
            />
          </div>
        </template>
      </Column>
    </DataTable>

    <div v-if="!store.eventNotifications.length">
      <EmptyList
        :content="emptyListContent"
        data-test="empty-list"
      />
    </div>
  </TableCard>
  <EventNotificationEditorDialog
    v-model:visible="showEditor"
    :notification="notificationToEdit"
  />
  <OnmsConfirmationDialog
    :visible="showDeleteConfirmation"
    title="Delete Event Notification"
    actionButtonText="Delete"
    @ok="confirmDelete"
    @cancel="cancelDelete"
  >
    <template #content>
      <p>Are you sure you want to delete the event notification <strong>{{ notificationToDelete?.name }}</strong>? This action cannot be undone.</p>
    </template>
  </OnmsConfirmationDialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import { OnmsConfirmationDialog } from '@opennms/onms-ui'

import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import ToggleSwitch from 'primevue/toggleswitch'

import EventNotificationEditorDialog from '@/components/AdminNotifications/EventNotificationEditorDialog.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import { useNotificationConfigStore } from '@/stores/notificationConfigStore'
import { EventNotification } from '@/types/notificationConfig'

const PToggleSwitch = ToggleSwitch

const store = useNotificationConfigStore()

const showEditor = ref(false)
const notificationToEdit = ref<EventNotification | null>(null)
const showDeleteConfirmation = ref(false)

const openEditor = (notification: EventNotification | null) => {
  notificationToEdit.value = notification
  showEditor.value = true
}
const notificationToDelete = ref<EventNotification | null>(null)
// Guards against a second click racing the in-flight status update.
const pendingName = ref<string | null>(null)

const emptyListContent = {
  msg: 'No event notifications configured.'
}

const onToggle = async (notification: EventNotification, value: boolean) => {
  pendingName.value = notification.name
  try {
    await store.setEventNotificationStatus(notification.name, value ? 'on' : 'off')
  } finally {
    pendingName.value = null
  }
}

const askDelete = (notification: EventNotification) => {
  notificationToDelete.value = notification
  showDeleteConfirmation.value = true
}

const confirmDelete = async () => {
  if (notificationToDelete.value) {
    await store.deleteEventNotification(notificationToDelete.value.name)
  }
  showDeleteConfirmation.value = false
  notificationToDelete.value = null
}

const cancelDelete = () => {
  showDeleteConfirmation.value = false
  notificationToDelete.value = null
}
</script>

<style lang="scss" scoped>
.event-notifications-table {
  padding: 25px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.card-title {
  font-size: 1.1rem;
  font-weight: 600;
}

.action-container {
  display: flex;
  gap: 0.25rem;
}
</style>
