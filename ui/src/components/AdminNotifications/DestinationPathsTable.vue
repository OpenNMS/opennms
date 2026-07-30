<template>
  <TableCard class="destination-paths-table">
    <div class="header">
      <div class="card-title">Destination Paths</div>
      <Button
        outlined
        label="New Path"
        icon="pi pi-plus"
        data-test="add-destination-path-button"
        @click="openEditor(null)"
      />
    </div>

    <DataTable
      v-if="store.destinationPaths.length"
      :value="store.destinationPaths"
      dataKey="name"
      class="data-table"
      data-test="destination-paths-table"
    >
      <Column
        field="name"
        header="Name"
        sortable
      />
      <Column header="Initial Delay">
        <template #body="{ data }">
          {{ data['initial-delay'] ?? '0s' }}
        </template>
      </Column>
      <Column header="Targets">
        <template #body="{ data }">
          {{ targetSummary(data) }}
        </template>
      </Column>
      <Column header="Actions">
        <template #body="{ data }">
          <div class="action-container">
            <Button
              text
              label="Edit"
              :aria-label="`Edit ${data.name}`"
              data-test="edit-destination-path-button"
              @click="openEditor(data)"
            />
            <Button
              text
              label="Test"
              :aria-label="`Send test notification via ${data.name}`"
              data-test="test-destination-path-button"
              @click="askTest(data)"
            />
            <Button
              text
              label="Delete"
              severity="danger"
              :aria-label="`Delete ${data.name}`"
              data-test="delete-destination-path-button"
              @click="askDelete(data)"
            />
          </div>
        </template>
      </Column>
    </DataTable>

    <div v-if="!store.destinationPaths.length">
      <EmptyList
        :content="emptyListContent"
        data-test="empty-list"
      />
    </div>
  </TableCard>
  <DestinationPathEditorDialog
    v-model:visible="showEditor"
    :path="pathToEdit"
  />
  <OnmsConfirmationDialog
    :visible="showDeleteConfirmation"
    title="Delete Destination Path"
    actionButtonText="Delete"
    @ok="confirmDelete"
    @cancel="cancelDelete"
  >
    <template #content>
      <p>Are you sure you want to delete the destination path <strong>{{ pathToDelete?.name }}</strong>? Event notifications referencing it will stop resolving targets. This action cannot be undone.</p>
    </template>
  </OnmsConfirmationDialog>
  <OnmsConfirmationDialog
    :visible="showTestConfirmation"
    title="Send Test Notification"
    actionButtonText="Send Test"
    @ok="confirmTest"
    @cancel="cancelTest"
  >
    <template #content>
      <p>Send a test notification to every target of <strong>{{ pathToTest?.name }}</strong>? Real notifications (email, pager, etc.) will be delivered to the configured users and groups.</p>
    </template>
  </OnmsConfirmationDialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import { OnmsConfirmationDialog } from '@opennms/onms-ui'

import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'

import DestinationPathEditorDialog from '@/components/AdminNotifications/DestinationPathEditorDialog.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import { useNotificationConfigStore } from '@/stores/notificationConfigStore'
import { DestinationPath } from '@/types/notificationConfig'

const store = useNotificationConfigStore()

const showEditor = ref(false)
const pathToEdit = ref<DestinationPath | null>(null)

const openEditor = (path: DestinationPath | null) => {
  pathToEdit.value = path
  showEditor.value = true
}

const showDeleteConfirmation = ref(false)
const pathToDelete = ref<DestinationPath | null>(null)
const showTestConfirmation = ref(false)
const pathToTest = ref<DestinationPath | null>(null)

const emptyListContent = {
  msg: 'No destination paths configured.'
}

const targetSummary = (path: DestinationPath) => {
  const targets = (path.target ?? []).map((t) => t.name)
  const escalations = path.escalate?.length ?? 0
  const summary = targets.join(', ') || 'none'
  return escalations ? `${summary} (+${escalations} escalation${escalations > 1 ? 's' : ''})` : summary
}

const askDelete = (path: DestinationPath) => {
  pathToDelete.value = path
  showDeleteConfirmation.value = true
}

const confirmDelete = async () => {
  if (pathToDelete.value) {
    await store.deleteDestinationPath(pathToDelete.value.name)
  }
  showDeleteConfirmation.value = false
  pathToDelete.value = null
}

const cancelDelete = () => {
  showDeleteConfirmation.value = false
  pathToDelete.value = null
}

const askTest = (path: DestinationPath) => {
  pathToTest.value = path
  showTestConfirmation.value = true
}

const confirmTest = async () => {
  if (pathToTest.value) {
    await store.testDestinationPath(pathToTest.value.name)
  }
  showTestConfirmation.value = false
  pathToTest.value = null
}

const cancelTest = () => {
  showTestConfirmation.value = false
  pathToTest.value = null
}
</script>

<style lang="scss" scoped>
.destination-paths-table {
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
