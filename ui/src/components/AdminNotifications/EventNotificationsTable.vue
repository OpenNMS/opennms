<template>
  <TableCard class="event-notifications-table">
    <div class="header">
      <div class="card-title">Event Notifications</div>
      <OnmsButton
        variant="outlined"
        data-test="add-event-notification-button"
        @click="openEditor(null)"
      >
        <OnmsIcon :icon="AddIcon" />
        Add New Event Notification
      </OnmsButton>
    </div>

    <OnmsTable
      v-if="store.eventNotifications.length"
      :value="store.eventNotifications"
      paginator
      dataKey="name"
      :rows="10"
      :rowsPerPageOptions="[10, 20, 50, 100]"
      class="data-table"
      data-test="event-notifications-table"
    >
      <OnmsColumn
        field="status"
        header="Status"
        sortable
      >
        <template #body="{ data }">
          <OnmsTag
            :class="data.status === 'on' ? 'enabled-tag' : 'disabled-tag'"
            :value="data.status === 'on' ? 'Enabled' : 'Disabled'"
            data-test="notification-status-tag"
          />
        </template>
      </OnmsColumn>
      <OnmsColumn
        field="name"
        header="Notification"
        sortable
      />
      <OnmsColumn
        field="uei"
        header="UEI"
        sortable
      >
        <template #body="{ data }">
          <span
            class="uei-cell"
            :title="data.uei"
          >{{ data.uei }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn
        field="destinationPath"
        header="Destination Path"
        sortable
      />
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <div class="action-container">
            <OnmsIconButton
              :title="`Edit ${data.name}`"
              :aria-label="`Edit ${data.name}`"
              data-test="edit-event-notification-button"
              :icon="EditIcon"
              @click="openEditor(data)"
            />
            <OnmsIconButton
              :title="store.eventNotifications.length <= 1 ? 'At least one notification must remain; disable it instead.' : `Delete ${data.name}`"
              :aria-label="`Delete ${data.name}`"
              data-test="delete-event-notification-button"
              :icon="DeleteIcon"
              :disabled="store.eventNotifications.length <= 1"
              @click="askDelete(data)"
            />
            <OnmsIconButton
              aria-haspopup="true"
              aria-controls="event-notification-row-menu"
              :title="`More actions for ${data.name}`"
              data-test="row-menu-button"
              :icon="MenuIcon"
              @click="toggleRowMenu($event, data)"
            />
          </div>
        </template>
      </OnmsColumn>
    </OnmsTable>

    <OnmsMenu
      id="event-notification-row-menu"
      ref="rowMenu"
      :items="rowMenuItems"
    />

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
import { computed, ref } from 'vue'

import {
  OnmsConfirmationDialog,
  OnmsButton,
  OnmsColumn,
  OnmsIcon,
  OnmsIconButton,
  OnmsMenu,
  OnmsMenuItem,
  OnmsTable,
  OnmsTag
} from '@opennms/onms-ui'

import EventNotificationEditorDialog from '@/components/AdminNotifications/EventNotificationEditorDialog.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import AddIcon from '@opennms/onms-ui/icons/action/Add.vue'
import EditIcon from '@opennms/onms-ui/icons/action/Edit.vue'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'
import MenuIcon from '@opennms/onms-ui/icons/navigation/MoreHoriz.vue'
import { useNotificationConfigStore } from '@/stores/notificationConfigStore'
import { EventNotification } from '@/types/notificationConfig'

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

// Row overflow menu: expose only the status action opposite the current state.
const rowMenu = ref()
const rowMenuTarget = ref<EventNotification | null>(null)
const rowMenuItems = computed<OnmsMenuItem[]>(() => {
  const target = rowMenuTarget.value
  if (!target) {
    return []
  }
  return [
    {
      label: target.status === 'on' ? 'Disable' : 'Enable',
      command: () => onToggle(target, target.status !== 'on')
    }
  ]
})

const toggleRowMenu = (event: Event, notification: EventNotification) => {
  rowMenuTarget.value = notification
  rowMenu.value?.toggle(event)
}

const onToggle = async (notification: EventNotification, value: boolean) => {
  if (pendingName.value === notification.name) {
    return
  }
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
  align-items: center;
  gap: 5px;
}

// Long UEIs (e.g. uei.opennms.org/... URLs) would otherwise force the table
// wider than its container and produce a horizontal scrollbar; clamp the cell
// and surface the full value on hover.
.uei-cell {
  display: inline-block;
  max-width: 28rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

.enabled-tag {
  border-radius: 4px;
  background-color: #0B720C1F;

  :deep(.p-tag-label) {
    color: #0B720C !important;
  }
}

.disabled-tag {
  border-radius: 4px;
  background-color: #7575751F;

  :deep(.p-tag-label) {
    color: #757575 !important;
  }
}
</style>
