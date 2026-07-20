<template>
  <TableCard class="event-config-event-table">
    <div class="header">
      <div class="title-container">
        <span class="title"> Event Configurations </span>
      </div>
      <div class="action-container">
        <div class="search-container">
          <FormField>
            <IconField>
              <InputText
                :id="searchId"
                :modelValue="store.eventsSearchTerm"
                @update:modelValue="onChangeSearchTerm"
                data-test="search-input"
                placeholder="Search by Event UEI or Event Label"
                :aria-label="'Search by Event UEI or Event Label'"
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
            @click="store.refreshEventConfigEvents()"
          >
            <FeatherIcon :icon="Refresh" />
          </Button>
        </div>
      </div>
    </div>

    <DataTable
      v-if="store.events.length"
      :value="store.events"
      lazy
      paginator
      dataKey="id"
      :rows="store.eventsPagination.pageSize"
      :totalRecords="store.eventsPagination.total"
      :first="(store.eventsPagination.page - 1) * store.eventsPagination.pageSize"
      :rowsPerPageOptions="[10, 20, 50]"
      :sortField="store.eventsSorting.sortKey"
      :sortOrder="store.eventsSorting.sortOrder === 'asc' ? 1 : -1"
      v-model:expandedRows="expandedRows"
      @page="onPage"
      @sort="onSort"
      class="data-table"
      data-test="event-config-event-table"
    >
      <Column
        expander
        style="width: 3rem"
      />
      <Column
        field="uei"
        header="Event UEI"
        sortable
      />
      <Column
        field="eventLabel"
        header="Event Label"
        sortable
      />
      <Column
        field="severity"
        header="Severity"
        sortable
      >
        <template #body="{ data }">
          <Tag
            :class="`${data.severity.toLowerCase()}-color severity`"
            :value="data.severity"
          />
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
              :title="`Edit ${data.eventLabel}`"
              data-test="edit-button"
              @click="onEditEvent(data)"
            >
              <FeatherIcon :icon="Edit" />
            </Button>
            <Button
              text
              aria-haspopup="true"
              aria-controls="event-row-menu"
              title="More Options"
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
          <h6>Description:</h6>
          <p
            class="description"
            v-html="data.description"
          ></p>
        </div>
      </template>
    </DataTable>

    <Menu
      id="event-row-menu"
      ref="rowMenu"
      :model="rowMenuItems"
      popup
    />

    <div v-if="!store.events.length">
      <EmptyList
        :content="emptyListContent"
        data-test="empty-list"
      />
    </div>
    <DeleteEventConfigEventDialog />
    <ChangeEventConfigEventStatusDialog />
  </TableCard>
</template>

<script setup lang="ts">
import { computed, ref, useId } from 'vue'
import { useRouter } from 'vue-router'

import { VENDOR_OPENNMS } from '@/lib/utils'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { CreateEditMode } from '@/types'
import { EventConfigEvent } from '@/types/eventConfig'
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
import { debounce } from 'lodash'
import EmptyList from '../Common/EmptyList.vue'
import FormField from '@/components/Common/FormField.vue'
import TableCard from '../Common/TableCard.vue'
import ChangeEventConfigEventStatusDialog from './Dialog/ChangeEventConfigEventStatusDialog.vue'
import DeleteEventConfigEventDialog from './Dialog/DeleteEventConfigEventDialog.vue'

const store = useEventConfigDetailStore()
const router = useRouter()
const searchId = useId()
const emptyListContent = {
  msg: 'No results found.'
}

const expandedRows = ref<Record<string | number, boolean>>({})

const rowMenu = ref()
const rowMenuTarget = ref<EventConfigEvent | null>(null)
const rowMenuItems = computed<MenuItem[]>(() => {
  const target = rowMenuTarget.value
  if (!target) {
    return []
  }
  const items: MenuItem[] = [
    {
      label: target.enabled ? 'Disable Event' : 'Enable Event',
      command: () => store.showChangeEventConfigEventStatusDialog(target)
    }
  ]
  if (store.selectedSource?.vendor !== VENDOR_OPENNMS) {
    items.push({
      label: 'Delete Event',
      command: () => store.showDeleteEventConfigEventDialog(target)
    })
  }
  return items
})

const toggleRowMenu = (event: Event, eventConfig: EventConfigEvent) => {
  rowMenuTarget.value = eventConfig
  rowMenu.value?.toggle(event)
}

const onSort = (event: DataTableSortEvent) => {
  if (event.sortField) {
    store.onEventsSortChange(String(event.sortField), event.sortOrder === 1 ? 'asc' : 'desc')
  } else {
    store.onEventsSortChange('createdTime', 'desc')
  }
}

const onPage = (event: DataTablePageEvent) => {
  if (event.rows !== store.eventsPagination.pageSize) {
    store.onEventsPageSizeChange(event.rows)
  } else {
    store.onEventsPageChange(event.page + 1)
  }
}

const onEditEvent = (event: EventConfigEvent) => {
  if (store.selectedSource) {
    const modificationStore = useEventModificationStore()
    modificationStore.setSelectedEventConfigSource(store.selectedSource, CreateEditMode.Edit, event)
    router.push({
      name: 'Event Configuration Create'
    })
  }
}

const debouncedSearch = debounce((value: string) => {
  store.onChangeEventsSearchTerm(value)
}, 500)

const onChangeSearchTerm = (value: string | undefined) => {
  const term = value ?? ''
  store.eventsSearchTerm = term
  debouncedSearch(term.trim())
}
</script>

<style lang="scss" scoped>
@use '@featherds/styles/mixins/typography';
@use '@/styles/_severities';

.event-config-event-table {
  margin-top: 10px;
  padding: 25px;

  .header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;

    .title-container {
      display: flex;
      align-items: center;

      .title {
        @include typography.headline3;
      }
    }

    .action-container {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      gap: 5px;
      width: 30%;

      .search-container {
        width: 80%;
      }
    }
  }

  .action-container {
    display: flex;
    align-items: center;
    gap: 5px;
  }

  .severity {
    @include typography.caption;
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
