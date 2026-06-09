<template>
  <div class="mib-groups-table-container">
    <div class="header">
      <div class="section-left">
        <div class="search-container">
          <FeatherInput
            label="Search"
            type="search"
            data-test="search-input"
            v-model.trim="store.mibGroupsSearchTerm"
            :hint="'Search by Name or Interface Type'"
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
            @click="store.resetMibGroupsFilters"
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
            data-test="add-mib-group-button"
            @click="store.openMibGroupCreationDrawer(null, CreateEditMode.Create)"
          >
            Add MIB Group
          </FeatherButton>
        </div>
      </div>
    </div>
    <div class="container">
      <table
        class="data-table"
        aria-label="Events Table"
        v-if="store.mibGroups.length"
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
            v-for="mibGroup in store.mibGroups"
            :key="mibGroup.id"
          >
            <tr>
              <td>{{ mibGroup.name }}</td>
              <td>{{ mibGroup.ifType }}</td>
              <td>
                <div class="tag">
                  <FeatherChip
                    :class="mibGroup.enabled ? 'enabled-tag' : 'disabled-tag'"
                    data-test="status-tag"
                  >
                    {{ mibGroup.enabled ? 'Enabled' : 'Disabled' }}
                  </FeatherChip>
                </div>
              </td>
              <td>
                <div class="action-container">
                  <FeatherButton
                    v-if="!isPluginSourced(store.selectedCollectionSource)"
                    icon="Edit"
                    :title="`Edit ${mibGroup.name}`"
                    data-test="edit-button"
                    @click="onMibGroupEditClicked(mibGroup)"
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
                      @click="openChangeStatusDialog(mibGroup)"
                    >
                      {{ mibGroup.enabled ? 'Disable MIB Group' : 'Enable MIB Group' }}
                    </FeatherDropdownItem>
                    <FeatherDropdownItem
                      v-if="!isPluginSourced(store.selectedCollectionSource)"
                      data-test="delete-mib-group-button"
                      @click="openDeleteMibGroupDialog(mibGroup)"
                    >
                      Delete MIB Group
                    </FeatherDropdownItem>
                  </FeatherDropdown>
                  <FeatherButton
                    primary
                    :icon="`${expandedRows.includes(mibGroup.id)
                    ? 'Expand Less'
                    : 'Expand More'
                    }`"
                    @click="toggleExpand(mibGroup.id)"
                  >
                    <FeatherIcon
                      :icon="ExpandLess"
                      v-if="expandedRows.includes(mibGroup.id)"
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
              v-if="expandedRows.includes(mibGroup.id)"
              class="expanded-content"
            >
              <td :colspan="5">
                <h5>MIB Group Names</h5>
                <p class="description">{{ mibGroup.mibGroupNames?.join(', ') }}</p>
                <div v-if="JSON.parse(mibGroup.mibObjects).length > 0">
                  <h5>MIB Objects:</h5>
                  <div
                    v-for="(value, index) in JSON.parse(mibGroup.mibObjects)"
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
              </td>
            </tr>
          </template>
        </TransitionGroup>
      </table>
      <div
        class="alerts-pagination"
        v-if="store.mibGroups.length"
      >
        <FeatherPagination
          :modelValue="store.mibGroupsPagination.page"
          :pageSize="store.mibGroupsPagination.pageSize"
          :total="store.mibGroupsPagination.total"
          :pageSizes="[10, 20, 30]"
          @update:modelValue="store.onMibGroupsPageChange"
          @update:pageSize="store.onMibGroupsPageSizeChange"
          data-test="FeatherPagination"
        />
      </div>
    </div>
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
import { computed, reactive, ref } from 'vue'

import { debounce } from 'lodash'
import { isPluginSourced } from '@/lib/snmpDataCollectionHelpers'
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
import useSnackbar from '@/composables/useSnackbar'
import { deleteMibGroups, enableDisableSnmpMibGroups } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { SnmpCollectionMibGroup } from '@/types/snmpDataCollection'
import EmptyList from '../../Common/EmptyList.vue'
import DeleteConfirmationDialog from '../../SnmpDataCollection/Dialog/DeleteConfirmationDialog.vue'
import SnmpDataCollectionChangeStatusDialog from '../../SnmpDataCollection/Dialog/SnmpDataCollectionChangeStatusDialog.vue'
import MibGroupCreationDrawer from './Drawer/MibGroupCreationDrawer.vue'

const store = useSnmpDataCollectionDetailStore()
const expandedRows = ref<number[]>([])
const isDeleteDialogVisible = ref(false)
const isChangeStatusDialogVisible = ref(false)
const selectedMibGroup = ref<{ id: number; name: string, enabled: boolean } | null>(null)
const snackbar = useSnackbar()
const columns = computed(() => [
  { id: 'name', label: 'Name' },
  { id: 'ifType', label: 'Interface Type' },
  { id: 'enabled', label: 'Status' }
])

const sort = reactive({
  name: SORT.NONE,
  ifType: SORT.NONE,
  enabled: SORT.NONE
}) as any

const onMibGroupEditClicked = (mibGroup: SnmpCollectionMibGroup) => {
  store.openMibGroupCreationDrawer(mibGroup, CreateEditMode.Edit)
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
    store.onMibGroupsSortChange(sortObj.property, sortObj.value)
  } else {
    store.onMibGroupsSortChange('createdTime', 'desc')
  }

  for (const prop in sort) {
    sort[prop] = SORT.NONE
  }
  sort[sortObj.property] = sortObj.value
}

const onChangeSearchTerm = debounce(async (value: string) => {
  await store.onChangeMibGroupsSearchTerm(value)
}, 500)

const openDeleteMibGroupDialog = (mibGroup: { id: number; name: string, enabled: boolean } | null) => {
  selectedMibGroup.value = mibGroup
  isDeleteDialogVisible.value = true
}

const closeDeleteMibGroupDialog = () => {
  selectedMibGroup.value = null
  isDeleteDialogVisible.value = false
}

const openChangeStatusDialog = (mibGroup: { id: number; name: string, enabled: boolean } | null) => {
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
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';

.mib-groups-table-container {
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
