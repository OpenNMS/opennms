<template>
  <TableCard class="locations-table">
    <div class="header">
      <div class="card-title">Monitoring Locations</div>
      <div class="header-right">
        <OnmsSearchInput
          v-if="store.locations.length"
          v-model="search"
          placeholder="Search locations"
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
        <AboutDialogButton title="Monitoring Locations">
          <LocationsAbout />
        </AboutDialogButton>
      </div>
    </div>

    <p v-if="store.loadError && store.locations.length" class="reload-error" role="alert" data-test="reload-error">
      The list could not be reloaded and may be out of date. Refresh the page.
    </p>
    <p v-if="store.truncated" class="truncation-note" data-test="truncation-note">
      Showing the first {{ store.locations.length }} of {{ store.totalCount }} locations. Use search to narrow the list.
    </p>

    <OnmsTable
      :value="store.locations"
      v-model:filters="filters"
      :globalFilterFields="['location-name', 'monitoring-area', 'geolocation']"
      :loading="store.loading"
      :paginator="store.locations.length > 0"
      dataKey="location-name"
      sortField="location-name"
      :sortOrder="1"
      :rows="10"
      :rowsPerPageOptions="[10, 20, 50, 100]"
      class="data-table"
      data-test="locations-table"
    >
      <template #empty>
        <EmptyList
          :content="store.loadError ? errorListContent : emptyListContent"
          data-test="empty-list"
        />
      </template>
      <OnmsColumn field="location-name" header="Location Name" sortable />
      <OnmsColumn field="monitoring-area" header="Monitoring Area" sortable />
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
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <span
            v-if="!isPathAddressable(data['location-name'])"
            class="unaddressable"
            v-tooltip.top="'This location name contains / \\ or %, which the API cannot address; rename it through the database.'"
            data-test="unaddressable-note"
          >not editable here</span>
          <div v-else class="action-container">
            <OnmsIconButton
              :icon="Edit"
              :title="`Edit ${data['location-name']}`"
              :aria-label="`Edit ${data['location-name']}`"
              data-test="edit-location-button"
              @click="openEditor(data)"
            />
            <OnmsIconButton
              :icon="Delete"
              severity="danger"
              :disabled="data['location-name'] === DEFAULT_LOCATION"
              :title="data['location-name'] === DEFAULT_LOCATION ? 'The Default location cannot be deleted' : `Delete ${data['location-name']}`"
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
  <OnmsConfirmationDialog
    :visible="showDeleteConfirmation"
    title="Delete Monitoring Location"
    actionButtonText="Delete"
    @ok="confirmDelete"
    @cancel="cancelDelete"
  >
    <template #content>
      <p>
        Are you sure you want to delete the monitoring location
        <strong>{{ locationToDelete?.['location-name'] }}</strong>? The
        location must have no nodes assigned to it first (reassign them), and
        Minions still pointing at it keep the old location name until they are
        re-registered. This action cannot be undone.
      </p>
    </template>
  </OnmsConfirmationDialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

import { OnmsButton, OnmsColumn, OnmsConfirmationDialog, OnmsIconButton, OnmsSearchInput, OnmsTable, useOnmsToast } from '@opennms/onms-ui'

import AboutDialogButton from '@/components/Common/AboutDialogButton.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import Edit from '@opennms/onms-ui/icons/action/Edit.vue'
import TableCard from '@/components/Common/TableCard.vue'
import LocationEditorDialog from '@/components/ManageMonitoringLocations/LocationEditorDialog.vue'
import LocationsAbout from '@/components/ManageMonitoringLocations/LocationsAbout.vue'
import { isPathAddressable } from '@/lib/adminValidation'
import { useMonitoringLocationAdminStore } from '@/stores/monitoringLocationAdminStore'
import { MonitoringLocation } from '@/types'

// the built-in location nodes fall back to; the server would refuse the delete anyway
const DEFAULT_LOCATION = 'Default'

const store = useMonitoringLocationAdminStore()
const { showToast } = useOnmsToast()

const showEditor = ref(false)
const locationToEdit = ref<MonitoringLocation | null>(null)
const showDeleteConfirmation = ref(false)
const locationToDelete = ref<MonitoringLocation | null>(null)

const emptyListContent = { msg: 'No monitoring locations found.' }
const errorListContent = { msg: 'Could not load monitoring locations. Please retry.' }

const search = ref('')
const filters = ref({ global: { value: null as string | null, matchMode: 'contains' }})
watch(search, (value) => {
  filters.value.global.value = value || null
})

const openEditor = (location: MonitoringLocation | null) => {
  locationToEdit.value = location
  showEditor.value = true
}

const askDelete = (location: MonitoringLocation) => {
  locationToDelete.value = location
  showDeleteConfirmation.value = true
}

const confirmDelete = async () => {
  const location = locationToDelete.value
  showDeleteConfirmation.value = false
  if (!location) {
    return
  }
  const name = location['location-name']
  const result = await store.deleteLocation(name)
  // cleared after the dialog has closed, so the name does not blank out mid-animation
  locationToDelete.value = null
  if (result.success) {
    showToast({ message: `Monitoring location '${name}' deleted.`, severity: 'success' })
  } else {
    showToast({ message: result.message, severity: 'error' })
  }
}

const cancelDelete = () => {
  showDeleteConfirmation.value = false
  locationToDelete.value = null
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

.unaddressable {
  color: var(--p-text-muted-color);
  font-style: italic;
}

.action-container {
  display: flex;
  gap: 0.25rem;
  flex-wrap: wrap;
}
</style>
