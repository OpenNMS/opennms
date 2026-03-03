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
        <div class="refresh">
          <FeatherButton
            primary
            icon="Refresh"
            data-test="refresh-button"
            @click="store.refreshSourcesfilters()"
          >
            <FeatherIcon :icon="Refresh"> </FeatherIcon>
          </FeatherButton>
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
          <tr
            v-for="source in store.sources"
            :key="source.id"
          >
            <td>{{ source.name }}</td>
            <td>{{ source.vendor }}</td>
            <td>{{ source.uploadedBy }}</td>
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
                <FeatherButton
                  icon="Edit Source"
                  data-test="edit-button"
                >
                  <FeatherIcon :icon="Edit"> </FeatherIcon>
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
  </TableCard>
</template>

<script lang="ts" setup>
import useSnackbar from '@/composables/useSnackbar'
import { deleteSnmpCollectionSources, downloadSnmpDataCollectionById } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { FeatherButton } from '@featherds/button'
import { FeatherChip } from '@featherds/chips'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherIcon } from '@featherds/icon'
import Edit from '@featherds/icon/action/Edit'
import Search from '@featherds/icon/action/Search'
import ViewDetails from '@featherds/icon/action/ViewDetails'
import MenuIcon from '@featherds/icon/navigation/MoreHoriz'
import Refresh from '@featherds/icon/navigation/Refresh'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSortHeader, SORT } from '@featherds/table'
import { debounce } from 'lodash'
import EmptyList from '../Common/EmptyList.vue'
import TableCard from '../Common/TableCard.vue'
import DeleteConfirmationDialog from '../SnmpDataCollectionDetail/Dialog/DeleteConfirmationDialog.vue'

const router = useRouter()
const store = useSnmpDataCollectionStore()
const isDeleteDialogVisible = ref(false)
const selectedCollectionSource = ref<{ id: number; name: string } | null>(null)
const snackbar = useSnackbar()
const emptyListContent = {
  msg: 'No results found.'
}

const columns = computed(() => [
  { id: 'name', label: 'Source' },
  { id: 'vendor', label: 'Vendor' },
  { id: 'uploadedBy', label: 'Uploaded By' },
  { id: 'enabled', label: 'Status' }
])

const sort = reactive({
  name: SORT.NONE,
  vendor: SORT.NONE,
  uploadedBy: SORT.NONE,
  enabled: SORT.NONE
}) as any

const goToCreateSource = () => {
  router.push({ name: 'SNMP Data Collection Create' })
}

const onSourceClick = (source: any) => {
  router.push({
    name: 'SNMP Data Collection Detail',
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

const openDeleteCollectionSourceDialog = (collectionSource: { id: number; name: string } | null) => {
  selectedCollectionSource.value = collectionSource
  isDeleteDialogVisible.value = true
}

const closeDeleteCollectionSourceDialog = () => {
  selectedCollectionSource.value = null
  isDeleteDialogVisible.value = false
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

const downloadCollectionSource = async (source: any, format: string) => {
  const response = await downloadSnmpDataCollectionById(source.id, format)

  if (response) {
    const blob = new Blob([response.data], { type: response.headers['content-type'] })
    const link = document.createElement('a')
    link.href = window.URL.createObjectURL(blob)
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

