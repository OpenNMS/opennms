<template>
  <TableCard class="applications-table">
    <div class="toolbar">
      <OnmsSearchInput
        v-if="store.applications.length"
        v-model="search"
        placeholder="Applications"
        ariaLabel="Search applications"
        dataTest="application-search"
        class="search"
      />
      <OnmsButton
        variant="outlined"
        label="Add Application"
        icon="pi pi-plus"
        class="add-button"
        data-test="add-application-button"
        @click="showCreate = true"
      />
    </div>

    <p v-if="store.loadError && store.applications.length" class="reload-error" role="alert" data-test="reload-error">
      The list could not be reloaded and may be out of date. Refresh the page.
    </p>

    <OnmsTable
      :value="store.applications"
      v-model:filters="filters"
      :globalFilterFields="['name', 'perspectiveLocations']"
      :loading="store.loading && !store.applications.length"
      :paginator="store.applications.length > PAGE_SIZE"
      dataKey="id"
      sortField="name"
      :sortOrder="1"
      :rows="PAGE_SIZE"
      :rowsPerPageOptions="[PAGE_SIZE, 50, 100]"
      class="data-table"
      data-test="applications-table"
    >
      <template #empty>
        <EmptyList v-if="!store.loading" :content="emptyContent" data-test="empty-list" />
      </template>
      <OnmsColumn field="name" header="Application" sortable>
        <template #body="{ data }">
          <button
            type="button"
            class="link-button"
            :title="`Edit ${data.name}`"
            data-test="application-name"
            @click="openEditor(data)"
          >{{ data.name }}</button>
        </template>
      </OnmsColumn>
      <OnmsColumn field="serviceCount" header="Services" sortable>
        <template #body="{ data }">
          <span data-test="service-count">{{ data.serviceCount }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn field="perspectiveLocations" header="Perspective locations" sortable :sortField="locationsSortKey">
        <template #body="{ data }">
          <div v-if="data.perspectiveLocations.length" class="locations">
            <OnmsTag
              v-for="location in data.perspectiveLocations"
              :key="location"
              :value="location"
              severity="secondary"
              data-test="perspective-location"
            />
          </div>
          <span v-else class="muted" data-test="no-perspectives">None</span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <div class="action-container">
            <OnmsIconButton
              :icon="Edit"
              :title="`Edit ${data.name}`"
              :aria-label="`Edit ${data.name}`"
              data-test="edit-application-button"
              @click="openEditor(data)"
            />
            <OnmsIconButton
              :icon="Delete"
              severity="danger"
              :title="`Delete ${data.name}`"
              :aria-label="`Delete ${data.name}`"
              data-test="delete-application-button"
              @click="askDelete(data)"
            />
          </div>
        </template>
      </OnmsColumn>
    </OnmsTable>
  </TableCard>

  <ApplicationCreateDialog
    v-model:visible="showCreate"
    @created="onCreated"
  />
  <ApplicationEditorDialog
    v-model:visible="showEditor"
    :application="applicationToEdit"
  />
  <ApplicationDeleteDialog
    v-model:visible="showDelete"
    :application="applicationToDelete"
  />
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import { OnmsButton, OnmsColumn, OnmsIconButton, OnmsSearchInput, OnmsTable, OnmsTag, useOnmsToast } from '@opennms/onms-ui'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import Edit from '@opennms/onms-ui/icons/action/Edit.vue'

import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import ApplicationCreateDialog from '@/components/ManageApplications/ApplicationCreateDialog.vue'
import ApplicationDeleteDialog from '@/components/ManageApplications/ApplicationDeleteDialog.vue'
import ApplicationEditorDialog from '@/components/ManageApplications/ApplicationEditorDialog.vue'
import { useApplicationAdminStore } from '@/stores/applicationAdminStore'
import { ApplicationSummary } from '@/types/applicationAdmin'

// the paginator only appears once the rows fill more than one page
const PAGE_SIZE = 25

const store = useApplicationAdminStore()
const { showToast } = useOnmsToast()

const emptyListContent = { msg: 'No applications yet. Add one to group the services it depends on.' }
const filteredOutContent = { msg: 'No applications match the search.' }
const errorListContent = { msg: 'Could not load applications. Please retry.' }
const emptyContent = computed(() =>
  store.loadError ? errorListContent : store.applications.length ? filteredOutContent : emptyListContent)

const search = ref('')
const filters = ref({ global: { value: null as string | null, matchMode: 'contains' }})
watch(search, (value) => {
  filters.value.global.value = value || null
})

const locationsSortKey = (application: ApplicationSummary) => application.perspectiveLocations.join(', ')

const showCreate = ref(false)
const showEditor = ref(false)
const applicationToEdit = ref<ApplicationSummary | null>(null)
const showDelete = ref(false)
const applicationToDelete = ref<ApplicationSummary | null>(null)

const openEditor = (application: ApplicationSummary) => {
  applicationToEdit.value = application
  showEditor.value = true
}

const askDelete = (application: ApplicationSummary) => {
  applicationToDelete.value = application
  showDelete.value = true
}

// a new application has no members yet, so the editor opens straight away
const onCreated = (name: string, id: number | undefined) => {
  const created = store.applications.find(application => id !== undefined ? application.id === id : application.name === name)
  if (created) {
    openEditor(created)
  } else {
    showToast({ message: `Application '${name}' was created, but the list could not be reloaded. Refresh the page to edit it.`, severity: 'warn' })
  }
}
</script>

<style lang="scss" scoped>
.applications-table {
  padding: 25px;
}

.toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.search {
  width: 18rem;
}

.add-button {
  margin-left: auto;
}

.reload-error {
  margin: 0 0 0.75rem 0;
  color: var(--p-red-700, #b91c1c);
  font-size: 0.9rem;
}

.locations {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
}

.muted {
  color: var(--p-text-muted-color);
  font-style: italic;
}

.link-button {
  padding: 0;
  border: 0;
  background: none;
  font: inherit;
  color: var(--p-primary-color);
  cursor: pointer;
  text-decoration: underline;
  text-align: left;
}

.action-container {
  display: flex;
  gap: 0.25rem;
  flex-wrap: wrap;
}
</style>
