<template>
  <div class="system-definitions-table-container">
    <div class="header">
      <div class="section-left">
        <div class="search-container">
          <FeatherInput
            label="Search"
            type="search"
            data-test="search-input"
            v-model.trim="store.systemDefsSearchTerm"
            :hint="'Search by Name'"
            @update:modelValue.self="((e: string) => onChangeSearchTerm(e))"
          >
            <template #pre>
              <FeatherIcon :icon="Search" />
            </template>
          </FeatherInput>
        </div>
        <div class="refresh">
          <FeatherButton
            icon="Refresh"
            data-test="refresh-button"
            @click="store.resetSystemDefinitionsFilters"
          >
            <FeatherIcon :icon="Refresh"> </FeatherIcon>
          </FeatherButton>
        </div>
      </div>
      <div class="section-right">
        <div
          class="add"
          v-if="!isPluginSourced(store.selectedCollectionSource)"
        >
          <FeatherButton
            secondary
            data-test="add-system-definition-button"
            @click="store.openSystemDefCreationDrawer(null, CreateEditMode.Create)"
          >
            Add System Definition
          </FeatherButton>
        </div>
      </div>
    </div>
    <div class="container">
      <table
        class="data-table"
        aria-label="Events Table"
        v-if="store.systemDefinitions.length"
      >
        <thead>
          <tr>
            <FeatherSortHeader
              v-for="col of columns"
              :key="col.label"
              scope="col"
              :property="col.id"
              :sort="(sort as any)[col.id]"
              v-on:sort-changed="sortChanged"
            >
              {{ col.label }}
            </FeatherSortHeader>
            <th>Actions</th>
          </tr>
        </thead>
        <TransitionGroup
          name="data-table"
          tag="tbody"
        >
          <template
            v-for="systemDefinition in store.systemDefinitions"
            :key="systemDefinition.id"
          >
            <tr>
              <td>{{ systemDefinition.name }}</td>
              <td>{{ systemDefinition.sysoid }}</td>
              <td>{{ systemDefinition.sysoidMask }}</td>
              <td>
                <div class="tag">
                  <FeatherChip
                    :class="systemDefinition.enabled ? 'enabled-tag' : 'disabled-tag'"
                    data-test="status-tag"
                  >
                    {{ systemDefinition.enabled ? 'Enabled' : 'Disabled' }}
                  </FeatherChip>
                </div>
              </td>
              <td>
                <div class="action-container">
                  <FeatherButton
                    v-if="!isPluginSourced(store.selectedCollectionSource)"
                    icon="Edit"
                    :title="`Edit ${systemDefinition.name}`"
                    data-test="edit-button"
                    @click="onSystemDefEditClicked(systemDefinition)"
                  >
                    <FeatherIcon :icon="Edit" />
                  </FeatherButton>
                  <FeatherDropdown>
                    <template v-slot:trigger="{ attrs, on }">
                      <FeatherButton
                        link
                        href="#"
                        v-bind="attrs"
                        v-on="on"
                        :icon="`More Options`"
                      >
                        <FeatherIcon :icon="MenuIcon" />
                      </FeatherButton>
                    </template>
                    <FeatherDropdownItem
                      data-test="change-status-button"
                      @click="openChangeStatusDialog(systemDefinition)"
                    >
                      {{ systemDefinition.enabled ? 'Disable Definition' : 'Enable Definition' }}
                    </FeatherDropdownItem>
                    <FeatherDropdownItem
                      v-if="!isPluginSourced(store.selectedCollectionSource)"
                      data-test="delete-definition-button"
                      @click="openDeleteSystemDefDialog(systemDefinition)"
                    >
                      Delete Definition
                    </FeatherDropdownItem>
                  </FeatherDropdown>
                  <FeatherButton
                    primary
                    :icon="`${expandedRows.includes(systemDefinition.id)
                    ? 'Expand Less'
                    : 'Expand More'
                    }`"
                    @click="toggleExpand(systemDefinition.id)"
                  >
                    <FeatherIcon
                      :icon="ExpandLess"
                      v-if="expandedRows.includes(systemDefinition.id)"
                    />
                    <FeatherIcon
                      :icon="ExpandMore"
                      v-else
                    />
                  </FeatherButton>
                </div>
              </td>
            </tr>
            <tr
              v-if="expandedRows.includes(systemDefinition.id)"
              class="expanded-content"
            >
              <td :colspan="5">
                <h6>MIB Group Names:</h6>
                <p class="description">{{ systemDefinition.mibGroupNames?.join(', ') }}</p>
              </td>
            </tr>
          </template>
        </TransitionGroup>
      </table>
      <div
        class="alerts-pagination"
        v-if="store.systemDefinitions.length"
      >
        <FeatherPagination
          :modelValue="store.systemDefsPagination.page"
          :pageSize="store.systemDefsPagination.pageSize"
          :total="store.systemDefsPagination.total"
          :pageSizes="[10, 20, 30]"
          @update:modelValue="store.onSystemDefsPageChange"
          @update:pageSize="store.onSystemDefsPageSizeChange"
          data-test="FeatherPagination"
        />
      </div>
    </div>
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
import { computed, reactive, ref } from 'vue'

import useSnackbar from '@/composables/useSnackbar'
import { isPluginSourced } from '@/lib/snmpDataCollectionHelpers'
import { deleteSystemDefinitions, enableDisableSnmpSystemDefs } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { SnmpCollectionSystemDef } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherChip } from '@featherds/chips'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherIcon } from '@featherds/icon'
import Edit from '@featherds/icon/action/Edit'
import Search from '@featherds/icon/action/Search'
import ExpandLess from '@featherds/icon/navigation/ExpandLess'
import ExpandMore from '@featherds/icon/navigation/ExpandMore'
import MenuIcon from '@featherds/icon/navigation/MoreHoriz'
import Refresh from '@featherds/icon/navigation/Refresh'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSortHeader, SORT } from '@featherds/table'
import { debounce } from 'lodash'
import EmptyList from '../../Common/EmptyList.vue'
import DeleteConfirmationDialog from '../../SnmpDataCollection/Dialog/DeleteConfirmationDialog.vue'
import SnmpDataCollectionChangeStatusDialog from '../../SnmpDataCollection/Dialog/SnmpDataCollectionChangeStatusDialog.vue'
import SystemDefinitionCreationDrawer from './Drawer/SystemDefinitionCreationDrawer.vue'

const store = useSnmpDataCollectionDetailStore()
const expandedRows = ref<number[]>([])
const isDeleteDialogVisible = ref(false)
const isChangeStatusDialogVisible = ref(false)
const selectedSystemDef = ref<{ id: number; name: string, enabled: boolean } | null>(null)
const snackbar = useSnackbar()
const columns = computed(() => [
  { id: 'name', label: 'Name' },
  { id: 'sysoid', label: 'SysOID' },
  { id: 'sysoidMask', label: 'SysOID Mask' },
  { id: 'enabled', label: 'Status' }
])

const sort = reactive({
  name: SORT.NONE,
  sysoid: SORT.NONE,
  sysoidMask: SORT.NONE,
  enabled: SORT.NONE
}) as any

const onSystemDefEditClicked = (defs: SnmpCollectionSystemDef) => {
  store.openSystemDefCreationDrawer(defs, CreateEditMode.Edit)
}

const toggleExpand = (id: number) => {
  const index = expandedRows.value.indexOf(id)
  if (index === -1) {
    expandedRows.value.push(id)
  } else {
    expandedRows.value.splice(index, 1)
  }
}

const sortChanged = (sortObj: { property: string; value: SORT }) => {
  if (sortObj.value === 'asc' || sortObj.value === 'desc') {
    store.onSystemDefsSortChange(sortObj.property, sortObj.value)
  } else {
    store.onSystemDefsSortChange('createdTime', 'desc')
  }

  for (const prop in sort) {
    sort[prop] = SORT.NONE
  }
  sort[sortObj.property] = sortObj.value
}

const onChangeSearchTerm = debounce(async (value: string) => {
  await store.onChangeSystemDefsSearchTerm(value)
}, 500)

const openDeleteSystemDefDialog = (systemDef: { id: number; name: string, enabled: boolean } | null) => {
  selectedSystemDef.value = systemDef
  isDeleteDialogVisible.value = true
}

const closeDeleteSystemDefDialog = () => {
  selectedSystemDef.value = null
  isDeleteDialogVisible.value = false
}

const openChangeStatusDialog = (systemDef: { id: number; name: string, enabled: boolean } | null) => {
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
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';

.system-definitions-table-container {
  margin-top: 10px;

  .header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;

    .section-left {
      display: flex;
      gap: 10px;

      .search-container {
        width: 400px;

        :deep(.feather-input-sub-text) {
          display: none !important;
        }
      }
    }
  }

  .container {
    table {
      width: 100%;
      @include table.table;

      thead {
        background: var(variables.$background);
        text-transform: uppercase;
      }

      td {
        white-space: nowrap;
        box-shadow: none;
        border-bottom: 1px solid var(variables.$border-on-surface);

        .action-container {
          display: flex;
          align-items: center;
          gap: 5px;

          button {
            margin: 0px;
          }

          :deep(.feather-menu-dropdown) {
            .feather-dropdown {
              li {
                a {
                  padding: 8px 16px !important;
                }
              }
            }
          }
        }

        .tag {
          .enabled-tag {
            margin: 0 !important;
            border-radius: 4px;
            background-color: #0B720C1F;

            :deep(span) {
              color: #0B720C !important;
            }
          }

          .disabled-tag {
            margin: 0 !important;
            border-radius: 4px;
            background-color: #7575751F;

            :deep(span) {
              color: #757575 !important;
            }
          }
        }

        .description {
          margin: 0;
          white-space: normal;
        }
      }
    }

    .alerts-pagination {
      display: flex;
      justify-content: flex-end;
      padding: var(variables.$spacing-xxs);
      border-bottom: 1px solid var(--feather-border-on-surface);
      border-left: 1px solid var(--feather-border-on-surface);
      border-right: 1px solid var(--feather-border-on-surface);
    }

    .feather-pagination {
      border: none !important;
    }
  }
}
</style>
