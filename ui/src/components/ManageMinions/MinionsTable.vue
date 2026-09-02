<template>
  <TableCard class="minions-table">
    <div class="header">
      <div class="card-title">Minions</div>
      <div class="header-right">
        <OnmsSearchInput
          v-if="store.minions.length"
          v-model="search"
          placeholder="Search minions"
          dataTest="minion-search"
          class="search"
        />
        <OnmsIconButton
          :icon="Refresh"
          title="Refresh"
          aria-label="Refresh minions"
          data-test="refresh-button"
          @click="store.getMinions()"
        />
      </div>
    </div>

    <p v-if="store.loadError && store.minions.length" class="stale-note" data-test="stale-note">
      Showing the last loaded data — the most recent refresh failed.
    </p>
    <p v-if="store.truncated" class="truncation-note" data-test="truncation-note">
      Showing the first {{ store.minions.length }} of {{ store.totalCount }} minions. Use search to narrow the list.
    </p>

    <OnmsTable
      :value="store.minions"
      v-model:filters="filters"
      :globalFilterFields="['id', 'label', 'location', 'type', 'status', 'version']"
      :paginator="store.minions.length > 0"
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
      <OnmsColumn field="id" header="ID" sortable>
        <template #body="{ data }">
          <a
            v-if="store.nodeIdFor(data)"
            :href="nodeUrl(store.nodeIdFor(data))"
            data-test="minion-node-link"
          >{{ data.id }}</a>
          <span v-else>{{ data.id }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn field="label" header="Label" sortable />
      <OnmsColumn field="location" header="Location" sortable />
      <OnmsColumn field="type" header="Type" sortable />
      <OnmsColumn field="status" header="Status" sortable>
        <template #body="{ data }">
          <OnmsTag
            :value="data.status ?? 'unknown'"
            :severity="statusSeverity(data.status)"
          />
        </template>
      </OnmsColumn>
      <OnmsColumn field="version" header="Version" sortable />
      <OnmsColumn field="date" header="Last Updated" sortable>
        <template #body="{ data }">{{ formatDate(data.date) }}</template>
      </OnmsColumn>
      <OnmsColumn header="Properties">
        <template #body="{ data }">
          <span class="props-count">{{ Object.keys(data.properties ?? {}).length }} propert{{ Object.keys(data.properties ?? {}).length === 1 ? 'y' : 'ies' }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <div class="action-container">
            <OnmsButton
              variant="text"
              label="Edit"
              :aria-label="`Edit ${data.label ?? data.id}`"
              data-test="edit-minion-button"
              @click="openEditor(data)"
            />
            <OnmsButton
              variant="text"
              label="Delete"
              severity="danger"
              :aria-label="`Delete ${data.label ?? data.id}`"
              data-test="delete-minion-button"
              @click="askDelete(data)"
            />
          </div>
        </template>
      </OnmsColumn>
    </OnmsTable>
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

import { OnmsButton, OnmsColumn, OnmsConfirmationDialog, OnmsIconButton, OnmsSearchInput, OnmsTable, OnmsTag } from '@opennms/onms-ui'

import EmptyList from '@/components/Common/EmptyList.vue'
import Refresh from '@opennms/onms-ui/icons/navigation/Refresh.vue'
import TableCard from '@/components/Common/TableCard.vue'
import MinionEditorDialog from '@/components/ManageMinions/MinionEditorDialog.vue'
import { useMinionAdminStore } from '@/stores/minionAdminStore'
import { Minion } from '@/types/minionAdmin'

const store = useMinionAdminStore()

// the minion's node lives on the legacy node page, one level up from /ui
const NODE_BASE = import.meta.env.BASE_URL.replace(/ui\/?$/, '')
const nodeUrl = (id?: number) => `${NODE_BASE}element/node.jsp?node=${id}`

const showEditor = ref(false)
const minionToEdit = ref<Minion | null>(null)
const showDeleteConfirmation = ref(false)
const minionToDelete = ref<Minion | null>(null)

const emptyListContent = { msg: 'No minions found.' }
const errorListContent = { msg: 'Failed to load minions. Check your connection or session and reload.' }

const search = ref('')
const filters = ref({ global: { value: null as string | null, matchMode: 'contains' }})
watch(search, (value) => {
  filters.value.global.value = value || null
})

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

.truncation-note {
  margin: 0 0 0.75rem 0;
  font-size: 0.85rem;
  color: var(--p-text-muted-color);
}

.stale-note {
  margin: 0 0 0.75rem 0;
  padding: 0.5rem 0.75rem;
  font-size: 0.85rem;
  border-radius: 4px;
  background: var(--p-yellow-50, #fffbeb);
  border: 1px solid var(--p-yellow-200, #fde68a);
  color: var(--p-yellow-800, #854d0e);
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
