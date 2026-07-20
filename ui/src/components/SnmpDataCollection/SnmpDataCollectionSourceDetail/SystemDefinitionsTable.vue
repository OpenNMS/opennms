<template>
  <div class="system-definitions-table-container">
    <div class="header">
      <div class="section-left">
        <div class="search-container">
          <FormField>
            <IconField>
              <InputText
                :id="searchId"
                :modelValue="store.systemDefsSearchTerm"
                @update:modelValue="onChangeSearchTerm"
                data-test="search-input"
                placeholder="Search by Name"
                :aria-label="'Search by Name'"
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
            @click="store.resetSystemDefinitionsFilters"
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
            label="Add System Definition"
            data-test="add-system-definition-button"
            @click="store.openSystemDefCreationDrawer(null, CreateEditMode.Create)"
          />
        </div>
      </div>
    </div>

    <DataTable
      v-if="store.systemDefinitions.length"
      :value="store.systemDefinitions"
      lazy
      paginator
      dataKey="id"
      :rows="store.systemDefsPagination.pageSize"
      :totalRecords="store.systemDefsPagination.total"
      :first="(store.systemDefsPagination.page - 1) * store.systemDefsPagination.pageSize"
      :rowsPerPageOptions="[10, 20, 30]"
      :sortField="store.systemDefsSorting.sortKey"
      :sortOrder="store.systemDefsSorting.sortOrder === 'asc' ? 1 : -1"
      v-model:expandedRows="expandedRows"
      @page="onPage"
      @sort="onSort"
      class="data-table"
      data-test="system-definitions-table"
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
        field="sysoid"
        header="SysOID"
        sortable
      />
      <Column
        field="sysoidMask"
        header="SysOID Mask"
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
              @click="onSystemDefEditClicked(data)"
            >
              <FeatherIcon :icon="Edit" />
            </Button>
            <Button
              text
              aria-haspopup="true"
              aria-controls="system-definition-row-menu"
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
          <h6>MIB Group Names:</h6>
          <p class="description">{{ data.mibGroupNames?.join(', ') }}</p>
        </div>
      </template>
    </DataTable>

    <Menu
      id="system-definition-row-menu"
      ref="rowMenu"
      :model="rowMenuItems"
      popup
    />

    <div v-if="!store.systemDefinitions.length">
      <EmptyList :content="{ msg: 'No System Definitions found.' }" />
    </div>
    <DeleteConfirmationDialog
      :visible="isDeleteDialogVisible"
      :selected="selectedSystemDef"
      type="system-def"
      @close="closeDeleteSystemDefDialog"
      @confirm="deleteSystemDef"
    />
    <SnmpDataCollectionChangeStatusDialog
      :visible="isChangeStatusDialogVisible"
      :selected="selectedSystemDef"
      type="system-def"
      :status="selectedSystemDef?.enabled ? 'Disable' : 'Enable'"
      @close="closeChangeStatusDialog"
      @confirm="changeSystemDefStatus"
    />
    <SystemDefinitionCreationDrawer />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, useId } from 'vue'

import useSnackbar from '@/composables/useSnackbar'
import { isPluginSourced } from '@/lib/snmpDataCollectionHelpers'
import { deleteSystemDefinitions, enableDisableSnmpSystemDefs } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { SnmpCollectionSystemDef } from '@/types/snmpDataCollection'
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
import SnmpDataCollectionChangeStatusDialog from '../../SnmpDataCollection/Dialog/SnmpDataCollectionChangeStatusDialog.vue'
import SystemDefinitionCreationDrawer from './Drawer/SystemDefinitionCreationDrawer.vue'

type SelectedSystemDef = { id: number; name: string, enabled: boolean }

const store = useSnmpDataCollectionDetailStore()
const searchId = useId()
const expandedRows = ref<Record<string | number, boolean>>({})
const isDeleteDialogVisible = ref(false)
const isChangeStatusDialogVisible = ref(false)
const selectedSystemDef = ref<SelectedSystemDef | null>(null)
const snackbar = useSnackbar()

const rowMenu = ref()
const rowMenuTarget = ref<SnmpCollectionSystemDef | null>(null)
const rowMenuItems = computed<MenuItem[]>(() => {
  const target = rowMenuTarget.value
  if (!target) {
    return []
  }
  const items: MenuItem[] = [
    {
      label: target.enabled ? 'Disable Definition' : 'Enable Definition',
      command: () => openChangeStatusDialog(target)
    }
  ]
  if (!isPluginSourced(store.selectedCollectionSource)) {
    items.push({
      label: 'Delete Definition',
      command: () => openDeleteSystemDefDialog(target)
    })
  }
  return items
})

const toggleRowMenu = (event: Event, systemDef: SnmpCollectionSystemDef) => {
  rowMenuTarget.value = systemDef
  rowMenu.value?.toggle(event)
}

const onSystemDefEditClicked = (defs: SnmpCollectionSystemDef) => {
  store.openSystemDefCreationDrawer(defs, CreateEditMode.Edit)
}

const onSort = (event: DataTableSortEvent) => {
  if (event.sortField) {
    store.onSystemDefsSortChange(String(event.sortField), event.sortOrder === 1 ? 'asc' : 'desc')
  } else {
    store.onSystemDefsSortChange('createdTime', 'desc')
  }
}

const onPage = (event: DataTablePageEvent) => {
  if (event.rows !== store.systemDefsPagination.pageSize) {
    store.onSystemDefsPageSizeChange(event.rows)
  } else {
    store.onSystemDefsPageChange(event.page + 1)
  }
}

const debouncedSearch = debounce((value: string) => {
  store.onChangeSystemDefsSearchTerm(value)
}, 500)

const onChangeSearchTerm = (value: string | undefined) => {
  const term = value ?? ''
  store.systemDefsSearchTerm = term
  debouncedSearch(term.trim())
}

const openDeleteSystemDefDialog = (systemDef: SelectedSystemDef | null) => {
  selectedSystemDef.value = systemDef
  isDeleteDialogVisible.value = true
}

const closeDeleteSystemDefDialog = () => {
  selectedSystemDef.value = null
  isDeleteDialogVisible.value = false
}

const openChangeStatusDialog = (systemDef: SelectedSystemDef | null) => {
  selectedSystemDef.value = systemDef
  isChangeStatusDialogVisible.value = true
}

const closeChangeStatusDialog = () => {
  selectedSystemDef.value = null
  isChangeStatusDialogVisible.value = false
}

const deleteSystemDef = async (selected: { id: number; name: string } | null, type: string) => {
  if (
    type === 'system-def' &&
    selected?.id &&
    selected?.id === selectedSystemDef.value?.id &&
    selected?.name === selectedSystemDef.value?.name &&
    store.selectedCollectionSource?.id
  ) {
    const success = await deleteSystemDefinitions(store.selectedCollectionSource.id, [selected.id])
    if (success) {
      snackbar.showSnackBar({
        msg: `System Definition '${selected.name}' deleted successfully.`
      })
      await store.fetchSystemDefinitions()
      selectedSystemDef.value = null
      isDeleteDialogVisible.value = false
    } else {
      snackbar.showSnackBar({
        msg: `Failed to delete System Definition '${selected.name}'.`,
        error: true
      })
    }
  } else {
    snackbar.showSnackBar({
      msg: `Failed to delete System Definition '${selected?.name}'.`,
      error: true
    })
  }
}

const changeSystemDefStatus = async (selected: { id: number; name: string } | null, type: string) => {
  if (
    type === 'system-def' &&
    selected?.id &&
    selected?.id === selectedSystemDef.value?.id &&
    selected?.name === selectedSystemDef.value?.name &&
    store.selectedCollectionSource?.id
  ) {
    const updatedStatus = !selectedSystemDef.value?.enabled
    const success = await enableDisableSnmpSystemDefs(store.selectedCollectionSource.id, updatedStatus, [selectedSystemDef.value?.id])
    if (success) {
      snackbar.showSnackBar({
        msg: `System Definition '${selectedSystemDef.value?.name}' ${updatedStatus ? 'enabled' : 'disabled'} successfully.`
      })
      await store.fetchSystemDefinitions()
      selectedSystemDef.value = null
      isChangeStatusDialogVisible.value = false
    } else {
      snackbar.showSnackBar({
        msg: `Failed to ${updatedStatus ? 'enable' : 'disable'} System Definition '${selectedSystemDef.value?.name}'.`,
        error: true
      })
    }
  } else {
    snackbar.showSnackBar({
      msg: `Failed to change status for System Definition '${selected?.name}'.`,
      error: true
    })
  }
}
</script>

<style scoped lang="scss">
.system-definitions-table-container {
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
