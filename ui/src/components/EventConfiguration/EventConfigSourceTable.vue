<template>
  <TableCard class="event-configuration-table">
    <div class="header">
      <div class="title-container">
        <!-- <span class="title"> SNMP Interfaces </span> -->
      </div>
      <div class="header-content-container">
        <div class="search-container">
          <FormField class="search-field">
            <IconField>
              <InputText
                :id="searchId"
                :modelValue="store.sourcesSearchTerm"
                @update:modelValue="onChangeSearchTerm"
                data-test="search-input"
                placeholder="Search by Source, Vendor, UEI or Label"
                :aria-label="'Search by Source, Vendor, UEI or Label'"
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
            @click="store.refreshSourcesFilters()"
          >
            <FeatherIcon :icon="Refresh" />
          </Button>
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
      data-test="event-config-source-table"
    >
      <Column
        field="name"
        header="Source"
        sortable
      />
      <Column
        field="vendor"
        header="Vendor"
        sortable
      />
      <Column
        field="eventCount"
        header="Event Count"
        sortable
      />
      <Column header="Status">
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
              @click="onEventClick(data)"
            >
              <FeatherIcon :icon="ViewDetails" />
            </Button>
            <Button
              text
              :title="`Download ${data.name} XML`"
              data-test="download-button"
              @click="downloadEventConfXmlBySourceId(data.id)"
            >
              <FeatherIcon :icon="Download" />
            </Button>
            <Button
              text
              aria-haspopup="true"
              aria-controls="event-source-row-menu"
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
      id="event-source-row-menu"
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
    <DeleteEventConfigSourceDialog />
    <ChangeEventConfigSourceStatusDialog />
  </TableCard>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, useId } from 'vue'
import { useRouter } from 'vue-router'

import { VENDOR_OPENNMS } from '@/lib/utils'
import { downloadEventConfXmlBySourceId } from '@/services/eventConfigService'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { EventConfigSource } from '@/types/eventConfig'
import { FeatherIcon } from '@featherds/icon'
import Download from '@featherds/icon/action/DownloadFile'
import Search from '@featherds/icon/action/Search'
import ViewDetails from '@featherds/icon/action/ViewDetails'
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
import { debounce } from 'lodash'
import EmptyList from '../Common/EmptyList.vue'
import FormField from '@/components/Common/FormField.vue'
import TableCard from '../Common/TableCard.vue'
import ChangeEventConfigSourceStatusDialog from './Dialog/ChangeEventConfigSourceStatusDialog.vue'
import DeleteEventConfigSourceDialog from './Dialog/DeleteEventConfigSourceDialog.vue'

const router = useRouter()
const store = useEventConfigStore()
const searchId = useId()
const emptyListContent = {
  msg: 'No results found.'
}

const rowMenu = ref()
const rowMenuTarget = ref<EventConfigSource | null>(null)
const rowMenuItems = computed<MenuItem[]>(() => {
  const target = rowMenuTarget.value
  if (!target) {
    return []
  }
  const items: MenuItem[] = [
    {
      label: target.enabled ? 'Disable Source' : 'Enable Source',
      command: () => store.showChangeEventConfigSourceStatusDialog(target)
    }
  ]
  if (target.vendor !== VENDOR_OPENNMS) {
    items.push({
      label: 'Delete Source',
      command: () => store.showDeleteEventConfigSourceModal(target)
    })
  }
  return items
})

const toggleRowMenu = (event: Event, source: EventConfigSource) => {
  rowMenuTarget.value = source
  rowMenu.value?.toggle(event)
}

const onEventClick = (source: EventConfigSource) => {
  router.push({
    name: 'Event Configuration Detail',
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

onMounted(async () => {
  await store.fetchEventConfigs()
})
</script>

<style lang="scss" scoped>
.event-configuration-table {
  margin-top: 10px;
  padding: 25px;

  .header {
    display: flex;
    margin-bottom: 20px;

    .title-container {
      display: flex;
      align-items: center;
    }

    .header-content-container {
      display: flex;
      align-items: center;
      justify-content: flex-start;
      gap: 5px;
      width: 30%;

      .search-container {
        // width: 80%;

        flex: 0 0 auto;
        min-width: 30em;

        .search-field {
          width: 100%;

          // make the input (and its IconField wrapper) fill the field so the
          // search icon sits at the input's right edge rather than floating far
          // out in the container
          :deep(.p-iconfield) {
            display: block;
            width: 100%;
          }

          :deep(.p-inputtext) {
            width: 100%;
            padding-right: 2.75rem;
          }

          // enlarge the search glyph (FeatherIcon scales with font-size) and
          // keep it near the right edge, vertically centered
          :deep(.p-inputicon) {
            font-size: 1.75rem;
            right: 0.625rem;
            margin-top: -0.875rem;
          }
        }

        .refresh {
          display: flex;

          :deep(.p-inputicon) {
            font-size: 1.75rem;
            right: 0.625rem;
            margin-top: -0.875rem;
          }
        }
      }
    }
  }

  .action-container {
    display: flex;
    align-items: center;
    gap: 5px;
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
}
</style>
