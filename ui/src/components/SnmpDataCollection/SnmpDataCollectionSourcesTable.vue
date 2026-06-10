<template>
  <TableCard class="snmp-data-collection-source-table">
    <div class="header">
      <div class="section-left">
        <div class="search-container">
          <FeatherInput
            label="Search"
            type="search"
            data-test="search-input"
            v-model.trim="store.sourcesSearchTerm"
            :hint="'Search by Source, Vendor or Description'"
            @update:modelValue.self="((e: string) => onChangeSearchTerm(e))"
          >
            <template #pre>
              <FeatherIcon :icon="Search" />
            </template>
          </FeatherInput>
        </div>
      </div>
      <div class="section-right">
        <div class="add">
          <FeatherButton
            secondary
            @click="goToCreateSource"
          >
            Create New Data Collection Source
          </FeatherButton>
        </div>
      </div>
    </div>
    <div class="container">
      <table
        class="data-table"
        aria-label="Events Table"
        v-if="store.sources.length"
      >
        <thead>
          <tr>
            <FeatherSortHeader
              scope="col"
              property="name"
              :sort="(sort as any).name"
              v-on:sort-changed="sortChanged"
            >
              Source
            </FeatherSortHeader>
            <th scope="col">Profiles</th>
            <FeatherSortHeader
              scope="col"
              property="enabled"
              :sort="(sort as any).enabled"
              v-on:sort-changed="sortChanged"
            >
              Status
            </FeatherSortHeader>
            <th>Actions</th>
          </tr>
        </thead>
        <TransitionGroup
          name="data-table"
          tag="tbody"
        >
          <tr
            v-for="source in store.sources"
            :key="source.id"
          >
            <td>{{ source.name }}</td>
            <td>
              <div
                class="profile-chips"
                :data-test="`profiles-cell-${source.name}`"
              >
                <FeatherTooltip
                  v-for="profile in profilesForSource(source.name)"
                  :key="profile.id"
                  title="Click to edit profile"
                  v-slot="{ attrs, on }"
                >
                  <FeatherChip
                    class="profile-tag clickable-chip"
                    v-bind="attrs"
                    v-on="on"
                    @click="router.push({ name: 'SNMP Data Collection Profile Detail', params: { id: profile.id } })"
                  >
                    {{ profile.name }}
                  </FeatherChip>
                </FeatherTooltip>
                <span
                  v-if="profilesForSource(source.name).length === 0"
                  class="empty-profiles"
                >—</span>
              </div>
            </td>
            <td>
              <div class="tag">
                <FeatherChip
                  :class="source.enabled ? 'enabled-tag' : 'disabled-tag'"
                  data-test="status-tag"
                >
                  {{ source.enabled ? 'Enabled' : 'Disabled' }}
                </FeatherChip>
              </div>
            </td>
            <td>
              <div class="action-container">
                <FeatherButton
                  icon="View Details"
                  data-test="view-button"
                  @click="onSourceClick(source)"
                >
                  <FeatherIcon :icon="ViewDetails"> </FeatherIcon>
                </FeatherButton>
                <!-- Quick-download in XML (the round-trippable format the
                     /upload endpoint accepts). JSON is still reachable via
                     the dropdown below for users who want it. -->
                <FeatherButton
                  :icon="`Download ${source.name} XML`"
                  data-test="download-xml-button"
                  @click="downloadCollectionSource(source, 'xml')"
                >
                  <FeatherIcon :icon="DownloadIcon" />
                </FeatherButton>
                <FeatherDropdown>
                  <template v-slot:trigger="{ attrs, on }">
                    <FeatherButton
                      link
                      href="#"
                      v-bind="attrs"
                      v-on="on"
                      :icon="`More actions for ${source.name}`"
                    >
                      <FeatherIcon :icon="MenuIcon" />
                    </FeatherButton>
                  </template>
                  <FeatherDropdownItem
                    data-test="download-xml-dropdown-button"
                    @click="downloadCollectionSource(source, 'xml')"
                  >
                    Download XML
                  </FeatherDropdownItem>
                  <FeatherDropdownItem
                    data-test="download-json-dropdown-button"
                    @click="downloadCollectionSource(source, 'json')"
                  >
                    Download JSON
                  </FeatherDropdownItem>
                  <FeatherDropdownItem
                    data-test="change-status-source-button"
                    @click="openChangeStatusDialog(source)"
                  >
                    {{ source.enabled ? 'Disable' : 'Enable' }} Source
                  </FeatherDropdownItem>
                  <FeatherDropdownItem
                    v-if="!isPluginSourced(source)"
                    data-test="delete-source-button"
                    @click="openDeleteCollectionSourceDialog(source)"
                  >
                    Delete Source
                  </FeatherDropdownItem>
                </FeatherDropdown>
              </div>
            </td>
          </tr>
        </TransitionGroup>
      </table>
      <div
        class="alerts-pagination"
        v-if="store.sources.length"
      >
        <FeatherPagination
          :modelValue="store.sourcesPagination.page"
          :pageSize="store.sourcesPagination.pageSize"
          :total="store.sourcesPagination.total"
          :pageSizes="[10, 20, 50, 100, 200]"
          @update:modelValue="store.onSourcePageChange"
          @update:pageSize="store.onSourcePageSizeChange"
          data-test="FeatherPagination"
        />
      </div>
      <div v-if="!store.sources.length">
        <EmptyList
          :content="emptyListContent"
          data-test="empty-list"
        />
      </div>
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
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import { isPluginSourced } from '@/lib/snmpDataCollectionHelpers'
import { FeatherButton } from '@featherds/button'
import { FeatherChip } from '@featherds/chips'
import { FeatherTooltip } from '@featherds/tooltip'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherIcon } from '@featherds/icon'
import DownloadIcon from '@featherds/icon/action/DownloadFile'
import MenuIcon from '@featherds/icon/navigation/MoreHoriz'
import Search from '@featherds/icon/action/Search'
import ViewDetails from '@featherds/icon/action/ViewDetails'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSortHeader, SORT } from '@featherds/table'
import { debounce } from 'lodash'
import useSnackbar from '@/composables/useSnackbar'
import {
  deleteSnmpCollectionSources,
  downloadSnmpDataCollectionById,
  enableDisableSnmpDataCollectionSources,
  getAllSnmpCollectionProfiles
} from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { SnmpCollectionProfile } from '@/types/snmpDataCollection'
import EmptyList from '../Common/EmptyList.vue'
import TableCard from '../Common/TableCard.vue'
import DeleteConfirmationDialog from './Dialog/DeleteConfirmationDialog.vue'
import SnmpDataCollectionChangeStatusDialog from './Dialog/SnmpDataCollectionChangeStatusDialog.vue'

const router = useRouter()
const store = useSnmpDataCollectionStore()
const isDeleteDialogVisible = ref(false)
const isChangeStatusDialogVisible = ref(false)
const selectedCollectionSource = ref<{ id: number; name: string, enabled: boolean } | null>(null)
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

const sort = reactive({
  name: SORT.NONE,
  enabled: SORT.NONE
}) as any

const goToCreateSource = () => {
  router.push({
    name: 'SNMP Data Collection Source Detail',
    params: { id: 'create' }
  })
}

const onSourceClick = (source: any) => {
  router.push({
    name: 'SNMP Data Collection Source Detail',
    params: { id: source.id }
  })
}

const sortChanged = (sortObj: { property: string; value: SORT }) => {
  if (sortObj.value === 'asc' || sortObj.value === 'desc') {
    store.onSourcesSortChange(sortObj.property, sortObj.value)
  } else {
    store.onSourcesSortChange('createdTime', 'desc')
  }

  for (const prop in sort) {
    sort[prop] = SORT.NONE
  }
  sort[sortObj.property] = sortObj.value
}

const onChangeSearchTerm = debounce(async (value: string) => {
  await store.onChangeSourcesSearchTerm(value)
}, 500)

const openDeleteCollectionSourceDialog = (collectionSource: { id: number; name: string, enabled: boolean } | null) => {
  selectedCollectionSource.value = collectionSource
  isDeleteDialogVisible.value = true
}

const openChangeStatusDialog = (collectionSource: { id: number; name: string, enabled: boolean } | null) => {
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

const downloadCollectionSource = async (source: any, format: string) => {
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
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';

.snmp-data-collection-source-table {
  margin-top: 10px;
  padding: 25px;
  border: 1px solid var(--feather-border-on-surface);

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

        div {
          border-radius: 5px;
          padding: 0px 5px 0px 5px;
        }

        .profile-chips {
          display: flex;
          flex-wrap: wrap;
          gap: 4px;
          align-items: center;

          .profile-tag {
            margin: 0 !important;
            border-radius: 4px;
          }

          .clickable-chip {
            cursor: pointer;

            &:hover {
              opacity: 0.8;
            }
          }

          .empty-profiles {
            color: var(--feather-secondary-text-on-surface);
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
      }
    }

    .alerts-pagination {
      display: flex;
      justify-content: center;
      padding: 30px 0px 0px 0px;
    }

    .feather-pagination {
      border: none !important;
    }
  }
}
</style>
