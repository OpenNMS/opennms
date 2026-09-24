<template>
  <TableCard class="locations-table">
    <div class="header">
      <div class="card-title">Monitoring Locations</div>
      <div class="header-right">
        <OnmsSearchInput
          v-if="store.locations.length"
          v-model="search"
          placeholder="Search locations"
          ariaLabel="Search locations"
          dataTest="location-search"
          class="search"
        />
        <OnmsButton
          variant="outlined"
          label="Add New Location"
          icon="pi pi-plus"
          data-test="add-location-button"
          @click="openEditor(null)"
        />
      </div>
    </div>

    <p v-if="store.loadError && store.locations.length" class="reload-error" role="alert" data-test="reload-error">
      The list could not be reloaded and may be out of date. Refresh the page.
    </p>
    <p v-if="store.truncated" class="truncation-note" data-test="truncation-note">
      Showing the first {{ store.locations.length }} of {{ store.totalCount }} locations. Use search to narrow the list.
    </p>

    <!-- the chip's own remove icon is not focusable, so a real button clears it -->
    <div v-if="nameFilter" class="filters">
      <OnmsChip
        :label="`Location: ${nameFilter}`"
        data-test="name-filter-chip"
      />
      <OnmsButton
        variant="text"
        size="small"
        label="Clear"
        aria-label="Clear name filter"
        data-test="clear-name-filter"
        @click="nameFilter = null"
      />
    </div>

    <OnmsTable
      :value="visibleLocations"
      v-model:filters="filters"
      :globalFilterFields="['location-name', 'monitoring-area']"
      :loading="store.loading && !store.locations.length"
      :paginator="visibleLocations.length > 0"
      :rowClass="rowClass"
      dataKey="location-name"
      sortField="location-name"
      :sortOrder="1"
      :rows="10"
      :rowsPerPageOptions="[10, 20, 50, 100]"
      class="data-table"
      data-test="locations-table"
    >
      <template #empty>
        <EmptyList :content="emptyContent" data-test="empty-list" />
      </template>
      <OnmsColumn field="location-name" header="Location Name" sortable />
      <OnmsColumn field="monitoring-area" header="Description" sortable />
      <!-- geolocation, coordinates and priority are hidden for now (NMS-20364)
      <OnmsColumn field="geolocation" header="Geolocation" sortable>
        <template #body="{ data }">{{ data.geolocation ?? '-' }}</template>
      </OnmsColumn>
      <OnmsColumn field="latitude" header="Latitude" sortable>
        <template #body="{ data }">{{ data.latitude ?? '-' }}</template>
      </OnmsColumn>
      <OnmsColumn field="longitude" header="Longitude" sortable>
        <template #body="{ data }">{{ data.longitude ?? '-' }}</template>
      </OnmsColumn>
      <OnmsColumn field="priority" header="Priority" sortable />
      -->
      <OnmsColumn header="Minions">
        <template #body="{ data }">
          <div v-if="minionSummary(data['location-name']).total" class="minion-summary">
            <button
              type="button"
              class="link-button"
              :title="`Show the Minions in ${data['location-name']}`"
              data-test="location-minions-link"
              @click="emit('showMinions', data['location-name'])"
            >{{ minionSummary(data['location-name']).total }} {{ minionSummary(data['location-name']).total === 1 ? 'Minion' : 'Minions' }}</button>
            <OnmsTag
              v-if="minionSummary(data['location-name']).down"
              :value="`${minionSummary(data['location-name']).down} down`"
              severity="danger"
              data-test="minions-down-tag"
            />
            <OnmsTag
              v-if="minionSummary(data['location-name']).unknown"
              :value="`${minionSummary(data['location-name']).unknown} unknown`"
              severity="warn"
              data-test="minions-unknown-tag"
            />
          </div>
          <span v-else class="muted" data-test="no-minions">None deployed</span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Nodes">
        <template #body="{ data }">
          <span
            :title="store.nodeCounts[data['location-name']] === null ? 'Node count is not available for this name' : undefined"
            data-test="node-count"
          >{{ store.nodeCounts[data['location-name']] ?? '—' }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <span
            v-if="!isPathAddressable(data['location-name'])"
            class="unaddressable"
            title="This location name cannot be addressed by the API; rename it through the database."
            data-test="unaddressable-note"
          >not editable here</span>
          <div v-else class="action-container">
            <OnmsIconButton
              :icon="Edit"
              :disabled="data['location-name'] === DEFAULT_LOCATION"
              :title="data['location-name'] === DEFAULT_LOCATION ? DEFAULT_LOCATION_NOTE : `Edit ${data['location-name']}`"
              :aria-label="`Edit ${data['location-name']}`"
              data-test="edit-location-button"
              @click="openEditor(data)"
            />
            <OnmsIconButton
              :icon="Delete"
              severity="danger"
              :disabled="data['location-name'] === DEFAULT_LOCATION"
              :title="data['location-name'] === DEFAULT_LOCATION ? DEFAULT_LOCATION_NOTE : `Delete ${data['location-name']}`"
              :aria-label="`Delete ${data['location-name']}`"
              data-test="delete-location-button"
              @click="askDelete(data)"
            />
          </div>
        </template>
      </OnmsColumn>
    </OnmsTable>
  </TableCard>

  <LocationEditorDialog
    v-model:visible="showEditor"
    :location="locationToEdit"
  />
  <LocationDeleteDialog
    v-model:visible="showDeleteDialog"
    :location="locationToDelete"
    @deleted="onDeleted"
  />
  <LocationDeletedDialog
    v-model:visible="showDeletedDialog"
    :summary="deletedSummary"
  />
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import { OnmsButton, OnmsChip, OnmsColumn, OnmsIconButton, OnmsSearchInput, OnmsTable, OnmsTag } from '@opennms/onms-ui'

import EmptyList from '@/components/Common/EmptyList.vue'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import Edit from '@opennms/onms-ui/icons/action/Edit.vue'
import TableCard from '@/components/Common/TableCard.vue'
import LocationDeleteDialog, { LocationDeletedSummary } from '@/components/ManageMonitoringLocations/LocationDeleteDialog.vue'
import LocationDeletedDialog from '@/components/ManageMonitoringLocations/LocationDeletedDialog.vue'
import LocationEditorDialog from '@/components/ManageMonitoringLocations/LocationEditorDialog.vue'
import { isPathAddressable } from '@/lib/adminValidation'
import { minionState } from '@/lib/minionStatus'
import { useMinionAdminStore } from '@/stores/minionAdminStore'
import { useMonitoringLocationAdminStore } from '@/stores/monitoringLocationAdminStore'
import { MonitoringLocation } from '@/types'

// the built-in location the core itself runs from and nodes fall back to
const DEFAULT_LOCATION = 'Default'
const DEFAULT_LOCATION_NOTE = 'Default is the core location'

const emit = defineEmits<{
  showMinions: [name: string]
  dialogOpen: [open: boolean]
}>()

const store = useMonitoringLocationAdminStore()
const minionStore = useMinionAdminStore()

const showEditor = ref(false)
const locationToEdit = ref<MonitoringLocation | null>(null)
const showDeleteDialog = ref(false)
const locationToDelete = ref<MonitoringLocation | null>(null)
const showDeletedDialog = ref(false)
const deletedSummary = ref<LocationDeletedSummary | null>(null)

watch(() => showEditor.value || showDeleteDialog.value || showDeletedDialog.value, open => emit('dialogOpen', open))

const emptyListContent = { msg: 'No monitoring locations found.' }
const filteredOutContent = { msg: 'No monitoring locations match the current filter.' }
const errorListContent = { msg: 'Could not load monitoring locations. Please retry.' }
const emptyContent = computed(() =>
  store.loadError ? errorListContent : store.locations.length ? filteredOutContent : emptyListContent)

const search = ref('')
const filters = ref({ global: { value: null as string | null, matchMode: 'contains' }})
watch(search, (value) => {
  filters.value.global.value = value || null
})

// an exact-name filter set by the Minions tab, shown as a dismissible chip;
// the search box is cleared so its substring cannot hide the requested row
const nameFilter = ref<string | null>(null)
const searchFor = (name: string) => {
  search.value = ''
  nameFilter.value = name
}
defineExpose({ searchFor })

const visibleLocations = computed(() =>
  nameFilter.value ? store.locations.filter(location => location['location-name'] === nameFilter.value) : store.locations)

const rowClass = (data: MonitoringLocation) => data['location-name'] === DEFAULT_LOCATION ? 'default-location-row' : undefined

const minionSummary = (name: string) => {
  const minions = minionStore.byLocation[name] ?? []
  let down = 0
  let unknown = 0
  for (const minion of minions) {
    const state = minionState(minion.status)
    if (state === 'down') {
      down++
    } else if (state === 'unknown') {
      unknown++
    }
  }
  return { total: minions.length, down, unknown }
}

const openEditor = (location: MonitoringLocation | null) => {
  locationToEdit.value = location
  showEditor.value = true
}

const askDelete = (location: MonitoringLocation) => {
  locationToDelete.value = location
  showDeleteDialog.value = true
}

// the result dialog is the confirmation, so there is no toast here
const onDeleted = (summary: LocationDeletedSummary) => {
  deletedSummary.value = summary
  showDeletedDialog.value = true
}
</script>

<style lang="scss" scoped>
.locations-table {
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

.reload-error {
  margin: 0 0 0.75rem 0;
  color: var(--p-red-700, #b91c1c);
  font-size: 0.9rem;
}

.truncation-note {
  margin: 0 0 0.75rem 0;
  font-size: 0.85rem;
  color: var(--p-text-muted-color);
}

.filters {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.unaddressable,
.muted {
  color: var(--p-text-muted-color);
  font-style: italic;
}

.minion-summary {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.4rem;
}

.link-button {
  padding: 0;
  border: 0;
  background: none;
  font: inherit;
  color: var(--p-primary-color);
  cursor: pointer;
  text-decoration: underline;
}

:deep(.default-location-row) > td {
  color: var(--p-text-muted-color);
}

.action-container {
  display: flex;
  gap: 0.25rem;
  flex-wrap: wrap;
}
</style>
