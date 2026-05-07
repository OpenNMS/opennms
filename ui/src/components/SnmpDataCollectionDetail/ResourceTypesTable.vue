<template>
  <div class="resource-types-table-container">
    <div class="header">
      <div class="section-left">
        <div class="search-container">
          <FeatherInput
            label="Search"
            type="search"
            data-test="search-input"
            v-model.trim="store.resourceTypesSearchTerm"
            :hint="'Search by Name or Label'"
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
            @click="store.resetResourceTypesFilters"
          >
            <FeatherIcon :icon="Refresh"> </FeatherIcon>
          </FeatherButton>
        </div>
      </div>
      <div class="section-right">
        <div class="add">
          <FeatherButton
            secondary
            data-test="add-resource-type-button"
            @click="store.openResourceTypeCreationDrawer(null, CreateEditMode.Create)"
          >
            Add Resource Type
          </FeatherButton>
        </div>
      </div>
    </div>
    <div class="container">
      <table
        class="data-table"
        aria-label="Events Table"
        v-if="store.resourceTypes.length"
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
            v-for="resourceType in store.resourceTypes"
            :key="resourceType.id"
          >
            <tr>
              <td>{{ resourceType.name }}</td>
              <td>{{ resourceType.label }}</td>
              <td>{{ resourceType.resourceLabel }}</td>
              <td>
                <div class="tag">
                  <FeatherChip
                    :class="resourceType.enabled ? 'enabled-tag' : 'disabled-tag'"
                    data-test="status-tag"
                  >
                    {{ resourceType.enabled ? 'Enabled' : 'Disabled' }}
                  </FeatherChip>
                </div>
              </td>
              <td>
                <div class="action-container">
                  <FeatherButton
                    icon="Edit"
                    :title="`Edit ${resourceType.name}`"
                    data-test="edit-button"
                    @click="onResourceTypeEditClicked(resourceType)"
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
                      @click="openChangeStatusDialog(resourceType)"
                    >
                      {{ resourceType.enabled ? 'Disable Resource Type' : 'Enable Resource Type' }}
                    </FeatherDropdownItem>
                    <FeatherDropdownItem
                      data-test="delete-resource-type-button"
                      @click="openResourceTypeDeleteDialog(resourceType)"
                    >
                      Delete Resource Type
                    </FeatherDropdownItem>
                  </FeatherDropdown>
                  <FeatherButton
                    primary
                    :icon="`${expandedRows.includes(resourceType.id)
                    ? 'Expand Less'
                    : 'Expand More'
                    }`"
                    @click="toggleExpand(resourceType.id)"
                  >
                    <FeatherIcon
                      :icon="ExpandLess"
                      v-if="expandedRows.includes(resourceType.id)"
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
              v-if="expandedRows.includes(resourceType.id)"
              class="expanded-content"
            >
              <td :colspan="5">
                <h6>Storage Strategy:</h6>
                <p class="description">{{ resourceType.storageStrategy }}</p>
                <h6>Persistence Selector Strategy:</h6>
                <p class="description">{{ resourceType.persistenceSelectorStrategy }}</p>
              </td>
            </tr>
          </template>
        </TransitionGroup>
      </table>
      <div
        class="alerts-pagination"
        v-if="store.resourceTypes.length"
      >
        <FeatherPagination
          :modelValue="store.resourceTypesPagination.page"
          :pageSize="store.resourceTypesPagination.pageSize"
          :total="store.resourceTypesPagination.total"
          :pageSizes="[10, 20, 30]"
          @update:modelValue="store.onResourceTypesPageChange"
          @update:pageSize="store.onResourceTypesPageSizeChange"
          data-test="FeatherPagination"
        />
      </div>
    </div>
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
import useSnackbar from '@/composables/useSnackbar'
import { deleteResourceTypes, enableDisableSnmpResourceTypes } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { SnmpCollectionResourceType } from '@/types/snmpDataCollection'
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
import EmptyList from '../Common/EmptyList.vue'
import DeleteConfirmationDialog from '../SnmpDataCollection/Dialog/DeleteConfirmationDialog.vue'
import ResourceTypeCreationDrawer from './Drawer/ResourceTypeCreationDrawer.vue'
import SnmpDataCollectionChangeStatusDialog from '../SnmpDataCollection/Dialog/SnmpDataCollectionChangeStatusDialog.vue'

const store = useSnmpDataCollectionDetailStore()
const expandedRows = ref<number[]>([])
const isDeleteDialogVisible = ref(false)
const isChangeStatusDialogVisible = ref(false)
const selectedResourceType = ref<{ id: number; name: string, enabled: boolean } | null>(null)
const snackbar = useSnackbar()
const columns = computed(() => [
  { id: 'name', label: 'Name' },
  { id: 'label', label: 'Label' },
  { id: 'resourceLabel', label: 'Resource Label' },
  { id: 'enabled', label: 'Status' }
])

const sort = reactive({
  name: SORT.NONE,
  label: SORT.NONE,
  resourceLabel: SORT.NONE,
  enabled: SORT.NONE
}) as any

const onResourceTypeEditClicked = (resourceType: SnmpCollectionResourceType) => {
  store.openResourceTypeCreationDrawer(resourceType, CreateEditMode.Edit)
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
    store.onResourceTypesSortChange(sortObj.property, sortObj.value)
  } else {
    store.onResourceTypesSortChange('createdTime', 'desc')
  }

  for (const prop in sort) {
    sort[prop] = SORT.NONE
  }
  sort[sortObj.property] = sortObj.value
}

const onChangeSearchTerm = debounce(async (value: string) => {
  await store.onChangeResourceTypesSearchTerm(value)
}, 500)

const openResourceTypeDeleteDialog = (resourceType: { id: number; name: string, enabled: boolean } | null) => {
  selectedResourceType.value = resourceType
  isDeleteDialogVisible.value = true
}

const closeDeleteResourceTypeDialog = () => {
  selectedResourceType.value = null
  isDeleteDialogVisible.value = false
}

const openChangeStatusDialog = (resourceType: { id: number; name: string, enabled: boolean } | null) => {
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
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';

.resource-types-table-container {
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

