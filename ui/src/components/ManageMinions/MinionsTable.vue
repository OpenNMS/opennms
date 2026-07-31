<template>
  <TableCard class="minions-table">
    <div class="header">
      <div class="card-title">Minions</div>
      <div class="header-right">
        <IconField v-if="store.minions.length" class="search">
          <InputIcon class="pi pi-search" />
          <InputText v-model="search" placeholder="Search minions" data-test="minion-search" />
        </IconField>
        <Button
          text
          icon="pi pi-refresh"
          title="Refresh"
          aria-label="Refresh minions"
          :loading="store.isLoading"
          data-test="refresh-button"
          @click="store.getMinions()"
        />
      </div>
    </div>

    <DataTable
      :value="store.minions"
      v-model:filters="filters"
      :globalFilterFields="['id', 'label', 'location', 'type', 'status', 'version']"
      :paginator="store.minions.length > 0"
      :loading="store.isLoading"
      dataKey="id"
      sortField="label"
      :sortOrder="1"
      :rows="10"
      :rowsPerPageOptions="[10, 20, 50, 100]"
      class="data-table"
      data-test="minions-table"
    >
      <template #empty>
        <EmptyList v-if="!store.isLoading" :content="store.loadError ? errorListContent : emptyListContent" data-test="empty-list" />
      </template>
      <Column field="id" header="ID" sortable />
      <Column field="label" header="Label" sortable />
      <Column field="location" header="Location" sortable />
      <Column field="type" header="Type" sortable />
      <Column field="status" header="Status" sortable>
        <template #body="{ data }">
          <Tag
            :value="data.status ?? 'unknown'"
            :severity="statusSeverity(data.status)"
          />
        </template>
      </Column>
      <Column field="version" header="Version" sortable />
      <Column field="date" header="Last Updated" sortable>
        <template #body="{ data }">{{ formatDate(data.date) }}</template>
      </Column>
      <Column header="Properties">
        <template #body="{ data }">
          <span class="props-count">{{ Object.keys(data.properties ?? {}).length }} propert{{ Object.keys(data.properties ?? {}).length === 1 ? 'y' : 'ies' }}</span>
        </template>
      </Column>
      <Column header="Actions">
        <template #body="{ data }">
          <div class="action-container">
            <Button
              text
              label="Edit"
              :aria-label="`Edit ${data.label ?? data.id}`"
              data-test="edit-minion-button"
              @click="openEditor(data)"
            />
            <Button
              text
              label="Delete"
              severity="danger"
              :aria-label="`Delete ${data.label ?? data.id}`"
              data-test="delete-minion-button"
              @click="askDelete(data)"
            />
          </div>
        </template>
      </Column>
    </DataTable>
  </TableCard>

  <MinionEditorDialog v-model:visible="showEditor" :minion="minionToEdit" />
  <OnmsConfirmationDialog
    :visible="showDeleteConfirmation"
    title="Delete Minion"
    actionButtonText="Delete"
    @ok="confirmDelete"
    @cancel="cancelDelete"
  >
    <template #content>
      <p>
        Are you sure you want to delete the minion
        <strong>{{ minionToDelete?.label ?? minionToDelete?.id }}</strong>? Its
        auto-created requisition node is removed as well. If the Minion process
        is still running it will re-register on its next check-in. This action
        cannot be undone.
      </p>
    </template>
  </OnmsConfirmationDialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

import { OnmsConfirmationDialog } from '@opennms/onms-ui'

import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import InputText from 'primevue/inputtext'
import Tag from 'primevue/tag'

import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import MinionEditorDialog from '@/components/ManageMinions/MinionEditorDialog.vue'
import { useMinionAdminStore } from '@/stores/minionAdminStore'
import { Minion } from '@/types/minionAdmin'

const store = useMinionAdminStore()

const showEditor = ref(false)
const minionToEdit = ref<Minion | null>(null)
const showDeleteConfirmation = ref(false)
const minionToDelete = ref<Minion | null>(null)

const emptyListContent = { msg: 'No minions found.' }
const errorListContent = { msg: 'Failed to load minions. Check your connection or session and reload.' }

const search = ref('')
const filters = ref({ global: { value: null as string | null, matchMode: 'contains' } })
watch(search, (value) => { filters.value.global.value = value || null })

const statusSeverity = (status?: string | null) => {
  const s = (status ?? '').toLowerCase()
  return s === 'up' ? 'success' : s === 'down' ? 'danger' : 'secondary'
}

const formatDate = (value?: string | number | null) => {
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  const date = new Date(value)
  return isNaN(date.getTime()) ? '-' : date.toLocaleString()
}

const openEditor = (minion: Minion) => {
  minionToEdit.value = minion
  showEditor.value = true
}

const askDelete = (minion: Minion) => {
  minionToDelete.value = minion
  showDeleteConfirmation.value = true
}

const confirmDelete = async () => {
  if (minionToDelete.value) {
    await store.deleteMinion(minionToDelete.value.id)
  }
  showDeleteConfirmation.value = false
  minionToDelete.value = null
}

const cancelDelete = () => {
  showDeleteConfirmation.value = false
  minionToDelete.value = null
}
</script>

<style lang="scss" scoped>
.minions-table {
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

.header-right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.props-count {
  color: var(--p-text-muted-color);
}

.action-container {
  display: flex;
  gap: 0.25rem;
  flex-wrap: wrap;
}
</style>
