<template>
  <div class="mib-groups-table-container">
    <div class="header">
      <div class="section-left">
        <div class="search-container">
          <FormField>
            <IconField>
              <InputText
                :id="searchId"
                :modelValue="store.mibGroupsSearchTerm"
                @update:modelValue="onChangeSearchTerm"
                data-test="search-input"
                placeholder="Search by Name or Interface Type"
                :aria-label="'Search by Name or Interface Type'"
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
            @click="store.resetMibGroupsFilters"
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
            label="Add MIB Group"
            data-test="add-mib-group-button"
            @click="store.openMibGroupCreationDrawer(null, CreateEditMode.Create)"
          />
        </div>
      </div>
    </div>

    <DataTable
      v-if="store.mibGroups.length"
      :value="store.mibGroups"
      lazy
      paginator
      dataKey="id"
      :rows="store.mibGroupsPagination.pageSize"
      :totalRecords="store.mibGroupsPagination.total"
      :first="(store.mibGroupsPagination.page - 1) * store.mibGroupsPagination.pageSize"
      :rowsPerPageOptions="[10, 20, 30]"
      :sortField="store.mibGroupsSorting.sortKey"
      :sortOrder="store.mibGroupsSorting.sortOrder === 'asc' ? 1 : -1"
      v-model:expandedRows="expandedRows"
      @page="onPage"
      @sort="onSort"
      class="data-table"
      data-test="mib-groups-table"
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
        field="ifType"
        header="Interface Type"
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
              @click="onMibGroupEditClicked(data)"
            >
              <FeatherIcon :icon="Edit" />
            </Button>
            <Button
              text
              aria-haspopup="true"
              :aria-controls="`mib-group-row-menu`"
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
          <h5>MIB Group Names</h5>
          <p class="description">{{ data.mibGroupNames?.join(', ') }}</p>
          <div v-if="JSON.parse(data.mibObjects).length > 0">
            <h5>MIB Objects:</h5>
            <div
              v-for="(value, index) in JSON.parse(data.mibObjects)"
              :key="value.alias"
            >
              <h6>Object {{ Number(index) + 1 }}</h6>
              <div>
                <strong>Alias:</strong> {{ value.alias }} <br />
                <strong>OID:</strong> {{ value.oid }} <br />
                <strong>Instance:</strong> {{ value.instance }} <br />
                <strong>Data Type:</strong> {{ value.type }}
              </div>
            </div>
          </div>
        </div>
      </template>
    </DataTable>

    <Menu
      id="mib-group-row-menu"
      ref="rowMenu"
      :model="rowMenuItems"
      popup
    />

    <div v-if="!store.mibGroups.length">
      <EmptyList :content="{ msg: 'No MIB Groups found.' }" />
    </div>
    <DeleteConfirmationDialog
      :visible="isDeleteDialogVisible"
      :selected="selectedMibGroup"
      type="mib-group"
      @close="closeDeleteMibGroupDialog"
      @confirm="deleteMibGroup"
    />
    <SnmpDataCollectionChangeStatusDialog
      :visible="isChangeStatusDialogVisible"
      :selected="selectedMibGroup"
      type="mib-group"
      :status="selectedMibGroup?.enabled ? 'Disable' : 'Enable'"
      @close="closeChangeStatusDialog"
      @confirm="changeMibGroupStatus"
    />
    <MibGroupCreationDrawer />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, useId } from 'vue'

import { debounce } from 'lodash'
import { isPluginSourced } from '@/lib/snmpDataCollectionHelpers'
import { FeatherIcon } from '@featherds/icon'
import Edit from '@featherds/icon/action/Edit'
import Search from '@featherds/icon/action/Search'
import MenuIcon from '@featherds/icon/navigation/MoreHoriz'
import Refresh from '@featherds/icon/navigation/Refresh'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import type { DataTablePageEvent, DataTableSortEvent } from 'primevue/datatable'
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import InputText from 'primevue/inputtext'
import type { MenuItem } from 'primevue/menuitem'
import Menu from 'primevue/menu'
import Tag from 'primevue/tag'
import useSnackbar from '@/composables/useSnackbar'
import { deleteMibGroups, enableDisableSnmpMibGroups } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { SnmpCollectionMibGroup } from '@/types/snmpDataCollection'
import EmptyList from '../../Common/EmptyList.vue'
import FormField from '@/components/Common/FormField.vue'
import DeleteConfirmationDialog from '../../SnmpDataCollection/Dialog/DeleteConfirmationDialog.vue'
import SnmpDataCollectionChangeStatusDialog from '../../SnmpDataCollection/Dialog/SnmpDataCollectionChangeStatusDialog.vue'
import MibGroupCreationDrawer from './Drawer/MibGroupCreationDrawer.vue'

type SelectedMibGroup = { id: number; name: string, enabled: boolean }

const store = useSnmpDataCollectionDetailStore()
const searchId = useId()
const expandedRows = ref<Record<string | number, boolean>>({})
const isDeleteDialogVisible = ref(false)
const isChangeStatusDialogVisible = ref(false)
const selectedMibGroup = ref<SelectedMibGroup | null>(null)
const snackbar = useSnackbar()

const rowMenu = ref()
const rowMenuTarget = ref<SnmpCollectionMibGroup | null>(null)
const rowMenuItems = computed<MenuItem[]>(() => {
  const target = rowMenuTarget.value
  if (!target) {
    return []
  }
  const items: MenuItem[] = [
    {
      label: target.enabled ? 'Disable MIB Group' : 'Enable MIB Group',
      command: () => openChangeStatusDialog(target)
    }
  ]
  if (!isPluginSourced(store.selectedCollectionSource)) {
    items.push({
      label: 'Delete MIB Group',
      command: () => openDeleteMibGroupDialog(target)
    })
  }
  return items
})

const toggleRowMenu = (event: Event, mibGroup: SnmpCollectionMibGroup) => {
  rowMenuTarget.value = mibGroup
  rowMenu.value?.toggle(event)
}

const onMibGroupEditClicked = (mibGroup: SnmpCollectionMibGroup) => {
  store.openMibGroupCreationDrawer(mibGroup, CreateEditMode.Edit)
}

const onSort = (event: DataTableSortEvent) => {
  if (event.sortField) {
    store.onMibGroupsSortChange(String(event.sortField), event.sortOrder === 1 ? 'asc' : 'desc')
  } else {
    store.onMibGroupsSortChange('createdTime', 'desc')
  }
}

const onPage = (event: DataTablePageEvent) => {
  if (event.rows !== store.mibGroupsPagination.pageSize) {
    store.onMibGroupsPageSizeChange(event.rows)
  } else {
    store.onMibGroupsPageChange(event.page + 1)
  }
}

const debouncedSearch = debounce((value: string) => {
  store.onChangeMibGroupsSearchTerm(value)
}, 500)

const onChangeSearchTerm = (value: string | undefined) => {
  const term = value ?? ''
  store.mibGroupsSearchTerm = term
  debouncedSearch(term.trim())
}

const openDeleteMibGroupDialog = (mibGroup: SelectedMibGroup | null) => {
  selectedMibGroup.value = mibGroup
  isDeleteDialogVisible.value = true
}

const closeDeleteMibGroupDialog = () => {
  selectedMibGroup.value = null
  isDeleteDialogVisible.value = false
}

const openChangeStatusDialog = (mibGroup: SelectedMibGroup | null) => {
  selectedMibGroup.value = mibGroup
  isChangeStatusDialogVisible.value = true
}

const closeChangeStatusDialog = () => {
  selectedMibGroup.value = null
  isChangeStatusDialogVisible.value = false
}

const deleteMibGroup = async (selected: { id: number; name: string } | null, type: string) => {
  if (
    type === 'mib-group' &&
    selected?.id &&
    selected?.id === selectedMibGroup.value?.id &&
    selected?.name === selectedMibGroup.value?.name &&
    store.selectedCollectionSource?.id
  ) {
    const success = await deleteMibGroups(store.selectedCollectionSource.id, [selected.id])
    if (success) {
      snackbar.showSnackBar({
        msg: `MIB Group '${selected.name}' deleted successfully.`
      })
      await store.fetchMibGroups()
      selectedMibGroup.value = null
      isDeleteDialogVisible.value = false
    } else {
      snackbar.showSnackBar({
        msg: `Failed to delete MIB Group '${selected.name}'.`,
        error: true
      })
    }
  } else {
    snackbar.showSnackBar({
      msg: `Failed to delete MIB Group '${selected?.name ?? ''}'.`,
      error: true
    })
  }
}

const changeMibGroupStatus = async (selected: { id: number; name: string } | null, type: string) => {
  if (
    type === 'mib-group' &&
    selected?.id &&
    selected?.id === selectedMibGroup.value?.id &&
    selected?.name === selectedMibGroup.value?.name &&
    store.selectedCollectionSource?.id
  ) {
    const updatedStatus = !selectedMibGroup.value?.enabled
    const success = await enableDisableSnmpMibGroups(store.selectedCollectionSource.id, updatedStatus, [selectedMibGroup.value?.id])
    if (success) {
      snackbar.showSnackBar({
        msg: `MIB Group '${selectedMibGroup.value?.name}' ${updatedStatus ? 'enabled' : 'disabled'} successfully.`
      })
      await store.fetchMibGroups()
      selectedMibGroup.value = null
      isChangeStatusDialogVisible.value = false
    } else {
      snackbar.showSnackBar({
        msg: `Failed to ${updatedStatus ? 'enable' : 'disable'} MIB Group '${selectedMibGroup.value?.name}'.`,
        error: true
      })
    }
  } else {
    snackbar.showSnackBar({
      msg: `Failed to change status for MIB Group '${selected?.name}'.`,
      error: true
    })
  }
}
</script>

<style scoped lang="scss">
.mib-groups-table-container {
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
