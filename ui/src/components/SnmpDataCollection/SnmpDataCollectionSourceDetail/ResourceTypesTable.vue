<template>
  <div class="resource-types-table-container">
    <div class="header">
      <div class="section-left">
        <div class="search-container">
          <FormField>
            <IconField>
              <InputText
                :id="searchId"
                :modelValue="store.resourceTypesSearchTerm"
                @update:modelValue="onChangeSearchTerm"
                data-test="search-input"
                placeholder="Search by Name or Label"
                :aria-label="'Search by Name or Label'"
              />
              <InputIcon>
                <FeatherIcon :icon="Search" />
              </InputIcon>
            </IconField>
          </FormField>
        </div>
        <div class="refresh">
          <Button
            text
            title="Refresh"
            data-test="refresh-button"
            @click="store.resetResourceTypesFilters"
          >
            <FeatherIcon :icon="Refresh" />
          </Button>
        </div>
      </div>
      <div class="section-right">
        <div
          class="add"
          v-if="!isPluginSourced(store.selectedCollectionSource)"
        >
          <Button
            outlined
            label="Add Resource Type"
            data-test="add-resource-type-button"
            @click="store.openResourceTypeCreationDrawer(null, CreateEditMode.Create)"
          />
        </div>
      </div>
    </div>

    <DataTable
      v-if="store.resourceTypes.length"
      :value="store.resourceTypes"
      lazy
      paginator
      dataKey="id"
      :rows="store.resourceTypesPagination.pageSize"
      :totalRecords="store.resourceTypesPagination.total"
      :first="(store.resourceTypesPagination.page - 1) * store.resourceTypesPagination.pageSize"
      :rowsPerPageOptions="[10, 20, 30]"
      :sortField="store.resourceTypesSorting.sortKey"
      :sortOrder="store.resourceTypesSorting.sortOrder === 'asc' ? 1 : -1"
      v-model:expandedRows="expandedRows"
      @page="onPage"
      @sort="onSort"
      class="data-table"
      data-test="resource-types-table"
    >
      <Column
        expander
        style="width: 3rem"
      />
      <Column
        field="name"
        header="Name"
        sortable
      />
      <Column
        field="label"
        header="Label"
        sortable
      />
      <Column
        field="resourceLabel"
        header="Resource Label"
        sortable
      />
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
              v-if="!isPluginSourced(store.selectedCollectionSource)"
              text
              :title="`Edit ${data.name}`"
              data-test="edit-button"
              @click="onResourceTypeEditClicked(data)"
            >
              <FeatherIcon :icon="Edit" />
            </Button>
            <Button
              text
              aria-haspopup="true"
              aria-controls="resource-type-row-menu"
              data-test="row-menu-button"
              @click="toggleRowMenu($event, data)"
            >
              <FeatherIcon :icon="MenuIcon" />
            </Button>
          </div>
        </template>
      </Column>
      <template #expansion="{ data }">
        <div class="expanded-content">
          <h6>Storage Strategy:</h6>
          <p class="description">{{ data.storageStrategy }}</p>
          <h6>Persistence Selector Strategy:</h6>
          <p class="description">{{ data.persistenceSelectorStrategy }}</p>
        </div>
      </template>
    </DataTable>

    <Menu
      id="resource-type-row-menu"
      ref="rowMenu"
      :model="rowMenuItems"
      popup
    />

    <div v-if="!store.resourceTypes.length">
      <EmptyList :content="{ msg: 'No Resource Types found.' }" />
    </div>
    <DeleteConfirmationDialog
      :visible="isDeleteDialogVisible"
      :selected="selectedResourceType"
      type="resource-type"
      @close="closeDeleteResourceTypeDialog"
      @confirm="deleteResourceType"
    />
    <SnmpDataCollectionChangeStatusDialog
      :visible="isChangeStatusDialogVisible"
      :selected="selectedResourceType"
      type="resource-type"
      :status="selectedResourceType?.enabled ? 'Disable' : 'Enable'"
      @close="closeChangeStatusDialog"
      @confirm="changeResourceTypeStatus"
    />
    <ResourceTypeCreationDrawer />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, useId } from 'vue'

import useSnackbar from '@/composables/useSnackbar'
import { isPluginSourced } from '@/lib/snmpDataCollectionHelpers'
import { deleteResourceTypes, enableDisableSnmpResourceTypes } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { SnmpCollectionResourceType } from '@/types/snmpDataCollection'
import { FeatherIcon } from '@featherds/icon'
import Edit from '@featherds/icon/action/Edit'
import Search from '@featherds/icon/action/Search'
import MenuIcon from '@featherds/icon/navigation/MoreHoriz'
import Refresh from '@featherds/icon/navigation/Refresh'
import { debounce } from 'lodash'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import type { DataTablePageEvent, DataTableSortEvent } from 'primevue/datatable'
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import InputText from 'primevue/inputtext'
import Menu from 'primevue/menu'
import type { MenuItem } from 'primevue/menuitem'
import Tag from 'primevue/tag'
import EmptyList from '../../Common/EmptyList.vue'
import FormField from '@/components/Common/FormField.vue'
import DeleteConfirmationDialog from '../../SnmpDataCollection/Dialog/DeleteConfirmationDialog.vue'
import ResourceTypeCreationDrawer from './Drawer/ResourceTypeCreationDrawer.vue'
import SnmpDataCollectionChangeStatusDialog from '../../SnmpDataCollection/Dialog/SnmpDataCollectionChangeStatusDialog.vue'

type SelectedResourceType = { id: number; name: string, enabled: boolean }

const store = useSnmpDataCollectionDetailStore()
const searchId = useId()
const expandedRows = ref<Record<string | number, boolean>>({})
const isDeleteDialogVisible = ref(false)
const isChangeStatusDialogVisible = ref(false)
const selectedResourceType = ref<SelectedResourceType | null>(null)
const snackbar = useSnackbar()

const rowMenu = ref()
const rowMenuTarget = ref<SnmpCollectionResourceType | null>(null)
const rowMenuItems = computed<MenuItem[]>(() => {
  const target = rowMenuTarget.value
  if (!target) {
    return []
  }
  const items: MenuItem[] = [
    {
      label: target.enabled ? 'Disable Resource Type' : 'Enable Resource Type',
      command: () => openChangeStatusDialog(target)
    }
  ]
  if (!isPluginSourced(store.selectedCollectionSource)) {
    items.push({
      label: 'Delete Resource Type',
      command: () => openResourceTypeDeleteDialog(target)
    })
  }
  return items
})

const toggleRowMenu = (event: Event, resourceType: SnmpCollectionResourceType) => {
  rowMenuTarget.value = resourceType
  rowMenu.value?.toggle(event)
}

const onResourceTypeEditClicked = (resourceType: SnmpCollectionResourceType) => {
  store.openResourceTypeCreationDrawer(resourceType, CreateEditMode.Edit)
}

const onSort = (event: DataTableSortEvent) => {
  if (event.sortField) {
    store.onResourceTypesSortChange(String(event.sortField), event.sortOrder === 1 ? 'asc' : 'desc')
  } else {
    store.onResourceTypesSortChange('createdTime', 'desc')
  }
}

const onPage = (event: DataTablePageEvent) => {
  if (event.rows !== store.resourceTypesPagination.pageSize) {
    store.onResourceTypesPageSizeChange(event.rows)
  } else {
    store.onResourceTypesPageChange(event.page + 1)
  }
}

const debouncedSearch = debounce((value: string) => {
  store.onChangeResourceTypesSearchTerm(value)
}, 500)

const onChangeSearchTerm = (value: string | undefined) => {
  const term = value ?? ''
  store.resourceTypesSearchTerm = term
  debouncedSearch(term.trim())
}

const openResourceTypeDeleteDialog = (resourceType: SelectedResourceType | null) => {
  selectedResourceType.value = resourceType
  isDeleteDialogVisible.value = true
}

const closeDeleteResourceTypeDialog = () => {
  selectedResourceType.value = null
  isDeleteDialogVisible.value = false
}

const openChangeStatusDialog = (resourceType: SelectedResourceType | null) => {
  selectedResourceType.value = resourceType
  isChangeStatusDialogVisible.value = true
}

const closeChangeStatusDialog = () => {
  selectedResourceType.value = null
  isChangeStatusDialogVisible.value = false
}

const deleteResourceType = async (selected: { id: number; name: string } | null, type: string) => {
  if (
    type === 'resource-type' &&
    selected?.id &&
    selected?.id === selectedResourceType.value?.id &&
    selected?.name === selectedResourceType.value?.name &&
    store.selectedCollectionSource?.id
  ) {
    const success = await deleteResourceTypes(store.selectedCollectionSource.id, [selected.id])
    if (success) {
      snackbar.showSnackBar({
        msg: `Resource Type '${selected.name}' deleted successfully.`
      })
      await store.fetchResourceTypes()
      isDeleteDialogVisible.value = false
      selectedResourceType.value = null
    } else {
      snackbar.showSnackBar({
        msg: `Failed to delete Resource Type '${selected.name}'.`,
        error: true
      })
    }
  } else {
    snackbar.showSnackBar({
      msg: `Failed to delete Resource Type '${selected?.name}'.`,
      error: true
    })
  }
}

const changeResourceTypeStatus = async (selected: { id: number; name: string } | null, type: string) => {
  if (
    type === 'resource-type' &&
    selected?.id &&
    selected?.id === selectedResourceType.value?.id &&
    selected?.name === selectedResourceType.value?.name &&
    store.selectedCollectionSource?.id
  ) {
    const updatedStatus = !selectedResourceType.value?.enabled
    const success = await enableDisableSnmpResourceTypes(store.selectedCollectionSource.id, updatedStatus, [selectedResourceType.value?.id])
    if (success) {
      snackbar.showSnackBar({
        msg: `Resource Type '${selectedResourceType.value?.name}' ${updatedStatus ? 'enabled' : 'disabled'} successfully.`
      })
      await store.fetchResourceTypes()
      selectedResourceType.value = null
      isChangeStatusDialogVisible.value = false
    } else {
      snackbar.showSnackBar({
        msg: `Failed to ${updatedStatus ? 'enable' : 'disable'} Resource Type '${selectedResourceType.value?.name}'.`,
        error: true
      })
    }
  } else {
    snackbar.showSnackBar({
      msg: `Failed to change status for Resource Type '${selected?.name}'.`,
      error: true
    })
  }
}
</script>

<style scoped lang="scss">
.resource-types-table-container {
  margin-top: 10px;

  .header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;

    .section-left {
      display: flex;
      align-items: center;
      gap: 10px;

      .search-container {
        width: 400px;
      }
    }
  }

  .action-container {
    display: flex;
    align-items: center;
    gap: 5px;
  }

  .tag {
    display: inline-block;
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

  .expanded-content {
    .description {
      margin: 0;
      white-space: normal;
    }
  }
}
</style>
