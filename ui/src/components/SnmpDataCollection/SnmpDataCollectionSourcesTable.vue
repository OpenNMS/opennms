<template>
  <TableCard class="snmp-data-collection-source-table">
    <div class="header">
      <div class="section-left">
        <div class="search-container">
          <FormField>
            <IconField>
              <InputText
                :id="searchId"
                :modelValue="store.sourcesSearchTerm"
                @update:modelValue="onChangeSearchTerm"
                data-test="search-input"
                placeholder="Search by Source, Vendor or Description"
                :aria-label="'Search by Source, Vendor or Description'"
              />
              <InputIcon>
                <FeatherIcon :icon="Search" />
              </InputIcon>
            </IconField>
          </FormField>
        </div>
      </div>
      <div class="section-right">
        <div class="add">
          <Button
            outlined
            label="Create New Data Collection Source"
            data-test="create-source-button"
            @click="goToCreateSource"
          />
        </div>
      </div>
    </div>

    <DataTable
      v-if="store.sources.length"
      :value="store.sources"
      lazy
      paginator
      dataKey="id"
      :rows="store.sourcesPagination.pageSize"
      :totalRecords="store.sourcesPagination.total"
      :first="(store.sourcesPagination.page - 1) * store.sourcesPagination.pageSize"
      :rowsPerPageOptions="[10, 20, 50, 100, 200]"
      :sortField="store.sourcesSorting.sortKey"
      :sortOrder="store.sourcesSorting.sortOrder === 'asc' ? 1 : -1"
      @page="onPage"
      @sort="onSort"
      class="data-table"
      data-test="sources-table"
    >
      <Column
        field="name"
        header="Source"
        sortable
      />
      <Column header="Profiles">
        <template #body="{ data }">
          <div
            class="profile-chips"
            :data-test="`profiles-cell-${data.name}`"
          >
            <Chip
              v-for="profile in profilesForSource(data.name)"
              :key="profile.id"
              :label="profile.name"
              class="profile-tag clickable-chip"
              v-tooltip="'Click to edit profile'"
              @click="router.push({ name: 'SNMP Data Collection Profile Detail', params: { id: profile.id } })"
            />
            <span
              v-if="profilesForSource(data.name).length === 0"
              class="empty-profiles"
            >—</span>
          </div>
        </template>
      </Column>
      <Column
        field="enabled"
        header="Status"
        sortable
      >
        <template #body="{ data }">
          <Tag
            :class="data.enabled ? 'enabled-tag' : 'disabled-tag'"
            :value="data.enabled ? 'Enabled' : 'Disabled'"
            data-test="status-tag"
          />
        </template>
      </Column>
      <Column header="Actions">
        <template #body="{ data }">
          <div class="action-container">
            <Button
              text
              :title="`View ${data.name}`"
              data-test="view-button"
              @click="onSourceClick(data)"
            >
              <FeatherIcon :icon="ViewDetails" />
            </Button>
            <!-- Quick-download in XML (the round-trippable format the
                 /upload endpoint accepts). JSON is still reachable via
                 the row menu below for users who want it. -->
            <Button
              text
              :title="`Download ${data.name} XML`"
              data-test="download-xml-button"
              @click="downloadCollectionSource(data, 'xml')"
            >
              <FeatherIcon :icon="DownloadIcon" />
            </Button>
            <Button
              text
              aria-haspopup="true"
              aria-controls="source-row-menu"
              :title="`More actions for ${data.name}`"
              data-test="row-menu-button"
              @click="toggleRowMenu($event, data)"
            >
              <FeatherIcon :icon="MenuIcon" />
            </Button>
          </div>
        </template>
      </Column>
    </DataTable>

    <Menu
      id="source-row-menu"
      ref="rowMenu"
      :model="rowMenuItems"
      popup
    />

    <div v-if="!store.sources.length">
      <EmptyList
        :content="emptyListContent"
        data-test="empty-list"
      />
    </div>
    <DeleteConfirmationDialog
      :visible="isDeleteDialogVisible"
      :selected="selectedCollectionSource"
      type="source"
      @close="closeDeleteCollectionSourceDialog"
      @confirm="deleteCollectionSource"
    />
    <SnmpDataCollectionChangeStatusDialog
      :visible="isChangeStatusDialogVisible"
      :selected="selectedCollectionSource"
      type="source"
      :status="selectedCollectionSource?.enabled ? 'Disable' : 'Enable'"
      @close="closeChangeStatusDialog"
      @confirm="changeCollectionSourceStatus"
    />
  </TableCard>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, useId, watch } from 'vue'
import { useRouter } from 'vue-router'

import { isPluginSourced } from '@/lib/snmpDataCollectionHelpers'
import { FeatherIcon } from '@featherds/icon'
import DownloadIcon from '@featherds/icon/action/DownloadFile'
import MenuIcon from '@featherds/icon/navigation/MoreHoriz'
import Search from '@featherds/icon/action/Search'
import ViewDetails from '@featherds/icon/action/ViewDetails'
import Button from 'primevue/button'
import Chip from 'primevue/chip'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import type { DataTablePageEvent, DataTableSortEvent } from 'primevue/datatable'
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import InputText from 'primevue/inputtext'
import type { MenuItem } from 'primevue/menuitem'
import Menu from 'primevue/menu'
import Tag from 'primevue/tag'
import { debounce } from 'lodash'
import useSnackbar from '@/composables/useSnackbar'
import {
  deleteSnmpCollectionSources,
  downloadSnmpDataCollectionById,
  enableDisableSnmpDataCollectionSources,
  getAllSnmpCollectionProfiles
} from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { SnmpCollectionProfile, SnmpCollectionSource } from '@/types/snmpDataCollection'
import EmptyList from '../Common/EmptyList.vue'
import FormField from '@/components/Common/FormField.vue'
import TableCard from '../Common/TableCard.vue'
import DeleteConfirmationDialog from './Dialog/DeleteConfirmationDialog.vue'
import SnmpDataCollectionChangeStatusDialog from './Dialog/SnmpDataCollectionChangeStatusDialog.vue'

type SelectedCollectionSource = { id: number; name: string, enabled: boolean }

const router = useRouter()
const store = useSnmpDataCollectionStore()
const searchId = useId()
const isDeleteDialogVisible = ref(false)
const isChangeStatusDialogVisible = ref(false)
const selectedCollectionSource = ref<SelectedCollectionSource | null>(null)
const snackbar = useSnackbar()
const emptyListContent = {
  msg: 'No results found.'
}

const availableProfiles = ref<SnmpCollectionProfile[]>([])

// Profiles are derived (a source is "in" a profile if its name appears in
// that profile's source_names JSON). Comparison is case-insensitive to match
// the upload contract.
const profilesForSource = (sourceName: string): { id: number; name: string }[] => {
  const target = sourceName.toLowerCase()
  return availableProfiles.value
    .filter(p => p.sourceNames?.some(n => n.toLowerCase() === target))
    .map(p => ({ id: p.id, name: p.name }))
}

const refreshAvailableProfiles = async () => {
  availableProfiles.value = await getAllSnmpCollectionProfiles()
}

onMounted(refreshAvailableProfiles)

// Tabs in this page (Sources / Import) are kept-alive, so onMounted only
// fires once. After the user uploads in the Import tab, profile.source_names
// has changed but our snapshot is stale — re-fetch every time this tab
// becomes active so the Profiles column reflects the latest attachments.
watch(
  () => store.activeTab,
  (tab) => {
    // Tab index 0 = the Sources table (this component).
    if (tab === 0) {
      refreshAvailableProfiles()
    }
  }
)

const rowMenu = ref()
const rowMenuTarget = ref<SnmpCollectionSource | null>(null)
const rowMenuItems = computed<MenuItem[]>(() => {
  const target = rowMenuTarget.value
  if (!target) {
    return []
  }
  const items: MenuItem[] = [
    {
      label: 'Download XML',
      command: () => downloadCollectionSource(target, 'xml')
    },
    {
      label: 'Download JSON',
      command: () => downloadCollectionSource(target, 'json')
    },
    {
      label: `${target.enabled ? 'Disable' : 'Enable'} Source`,
      command: () => openChangeStatusDialog(target)
    }
  ]
  if (!isPluginSourced(target)) {
    items.push({
      label: 'Delete Source',
      command: () => openDeleteCollectionSourceDialog(target)
    })
  }
  return items
})

const toggleRowMenu = (event: Event, source: SnmpCollectionSource) => {
  rowMenuTarget.value = source
  rowMenu.value?.toggle(event)
}

const goToCreateSource = () => {
  router.push({
    name: 'SNMP Data Collection Source Detail',
    params: { id: 'create' }
  })
}

const onSourceClick = (source: SnmpCollectionSource) => {
  router.push({
    name: 'SNMP Data Collection Source Detail',
    params: { id: source.id }
  })
}

const onSort = (event: DataTableSortEvent) => {
  if (event.sortField) {
    store.onSourcesSortChange(String(event.sortField), event.sortOrder === 1 ? 'asc' : 'desc')
  } else {
    store.onSourcesSortChange('createdTime', 'desc')
  }
}

const onPage = (event: DataTablePageEvent) => {
  if (event.rows !== store.sourcesPagination.pageSize) {
    store.onSourcePageSizeChange(event.rows)
  } else {
    store.onSourcePageChange(event.page + 1)
  }
}

const debouncedSearch = debounce((value: string) => {
  store.onChangeSourcesSearchTerm(value)
}, 500)

const onChangeSearchTerm = (value: string | undefined) => {
  const term = value ?? ''
  store.sourcesSearchTerm = term
  debouncedSearch(term.trim())
}

const openDeleteCollectionSourceDialog = (collectionSource: SelectedCollectionSource | null) => {
  selectedCollectionSource.value = collectionSource
  isDeleteDialogVisible.value = true
}

const openChangeStatusDialog = (collectionSource: SelectedCollectionSource | null) => {
  selectedCollectionSource.value = collectionSource
  isChangeStatusDialogVisible.value = true
}

const closeDeleteCollectionSourceDialog = () => {
  selectedCollectionSource.value = null
  isDeleteDialogVisible.value = false
}

const closeChangeStatusDialog = () => {
  selectedCollectionSource.value = null
  isChangeStatusDialogVisible.value = false
}

const deleteCollectionSource = async (selected: { id: number; name: string } | null, type: string) => {
  if (
    type === 'source' &&
    selected?.id &&
    selected?.id === selectedCollectionSource.value?.id &&
    selected?.name === selectedCollectionSource.value?.name
  ) {
    const success = await deleteSnmpCollectionSources([selectedCollectionSource.value?.id])
    if (success) {
      snackbar.showSnackBar({
        msg: `Collection Source '${selectedCollectionSource.value?.name}' deleted successfully.`
      })
      await Promise.all([
        store.fetchSnmpCollectionSources(),
        // Refresh the all-source-names cache so the Import tab's
        // duplicate-detection no longer treats the just-deleted source
        // as a "will update" row.
        store.fetchAllSourcesNames(),
        // Profile.source_names lists shrink when a source is deleted (the
        // backend removes it from every profile). Refresh so the Profiles
        // column reflects that.
        refreshAvailableProfiles()
      ])
      selectedCollectionSource.value = null
      isDeleteDialogVisible.value = false
      router.push({ name: 'SNMP Data Collection' })
    } else {
      snackbar.showSnackBar({
        msg: `Failed to delete Collection Source '${selectedCollectionSource.value?.name}'.`,
        error: true
      })
    }
  } else {
    snackbar.showSnackBar({
      msg: `Failed to delete Collection Source '${selected?.name}'.`,
      error: true
    })
  }
}

const changeCollectionSourceStatus = async (selected: { id: number; name: string } | null, type: string) => {
  if (
    type === 'source' &&
    selected?.id &&
    selected?.id === selectedCollectionSource.value?.id &&
    selected?.name === selectedCollectionSource.value?.name
  ) {
    const updatedStatus = !selectedCollectionSource.value?.enabled
    const success = await enableDisableSnmpDataCollectionSources(updatedStatus, [selectedCollectionSource.value?.id])
    if (success) {
      snackbar.showSnackBar({
        msg: `Collection Source '${selectedCollectionSource.value?.name}' ${updatedStatus ? 'enabled' : 'disabled'} successfully.`
      })
      await Promise.all([
        store.fetchSnmpCollectionSources(),
        store.fetchAllSourcesNames()
      ])
      selectedCollectionSource.value = null
      isChangeStatusDialogVisible.value = false
    } else {
      snackbar.showSnackBar({
        msg: `Failed to ${updatedStatus ? 'enable' : 'disable'} Collection Source '${selectedCollectionSource.value?.name}'.`,
        error: true
      })
    }
  } else {
    snackbar.showSnackBar({
      msg: `Failed to change status for Collection Source '${selected?.name}'.`,
      error: true
    })
  }
}

const downloadCollectionSource = async (source: { id: number; name: string }, format: string) => {
  const response = await downloadSnmpDataCollectionById(source.id, format)

  if (response) {
    const link = document.createElement('a')
    link.href = window.URL.createObjectURL(response)
    link.download = `${source.name}.${format}`
    link.click()
    window.URL.revokeObjectURL(link.href)
  } else {
    snackbar.showSnackBar({
      msg: `Failed to download Collection Source '${source.name}'.`,
      error: true
    })
  }
}

onMounted(async () => {
  await store.fetchSnmpCollectionSources()
})
</script>

<style lang="scss" scoped>
.snmp-data-collection-source-table {
  margin-top: 10px;
  padding: 25px;
  border: 1px solid var(--p-content-border-color);

  .header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;

    .section-left {
      display: flex;
      gap: 10px;

      .search-container {
        width: 400px;
      }
    }
  }

  .profile-chips {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    align-items: center;

    .profile-tag {
      border-radius: 4px;
    }

    .clickable-chip {
      cursor: pointer;

      &:hover {
        opacity: 0.8;
      }
    }

    .empty-profiles {
      color: var(--p-text-muted-color);
    }
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

  .action-container {
    display: flex;
    align-items: center;
    gap: 5px;

    // enlarge the button icons (FeatherIcon scales with font-size)
    :deep(.p-button) {
      font-size: 1.3rem;
    }
  }
}
</style>
