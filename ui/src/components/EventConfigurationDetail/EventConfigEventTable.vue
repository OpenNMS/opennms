<template>
  <TableCard class="event-config-event-table">
    <div class="header">
      <div class="title-container">
        <span class="title"> Event Configurations </span>
      </div>
      <div class="action-container">
        <OnmsButton
          v-if="!store.eventsReorderMode"
          variant="outlined"
          class="reorder-button"
          data-test="reorder-events-button"
          @click="store.startEventsReorder()"
        >Reorder Events</OnmsButton>
        <div
          class="search-container"
          v-if="!store.eventsReorderMode"
        >
          <FormField>
            <OnmsSearchInput
              :input-id="searchId"
              :modelValue="store.eventsSearchTerm"
              @update:modelValue="onChangeSearchTerm"
              data-test="search-input"
              placeholder="Search by UEI or Label"
              :aria-label="'Search by Event UEI or Event Label'"
            />
          </FormField>
        </div>
        <div
          class="refresh"
          v-if="!store.eventsReorderMode"
        >
          <OnmsIconButton
            title="Refresh"
            data-test="refresh-button"
            :icon="Refresh"
            @click="store.refreshEventConfigEvents()"
          />
        </div>
      </div>
    </div>

    <StagedReorderList
      v-if="store.eventsReorderMode"
      ref="reorderList"
      :items="store.orderedEvents"
      :item-label="eventLabel"
      :item-search-text="eventSearchText"
      item-noun="event"
      intro="Events at the top are evaluated first within this source. Drag rows or use the arrows; nothing changes until you save."
      filter-placeholder="Filter by UEI or label — several terms match any"
      :saving="store.isSavingEventsOrder"
      :save="store.saveEventsOrder"
      :refetch="store.fetchOrderedEvents"
      @close="store.stopEventsReorder()"
    >
      <template #item="{ item }">
        <div class="reorder-item-info">
          <span class="reorder-item-title">{{ item.eventLabel }}</span>
          <span class="reorder-item-meta reorder-item-uei">{{ item.uei }}</span>
        </div>
        <OnmsTag
          v-if="item.severity"
          :class="`${item.severity.toLowerCase()}-color severity`"
          :value="item.severity"
        />
        <OnmsTag
          :class="item.enabled ? 'enabled-tag' : 'disabled-tag'"
          :value="item.enabled ? 'Enabled' : 'Disabled'"
        />
      </template>
    </StagedReorderList>

    <OnmsTable
      v-else-if="store.events.length"
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
      <OnmsColumn
        expander
        style="width: 3rem"
      />
      <OnmsColumn
        field="eventOrder"
        sortable
        style="width: 6rem"
      >
        <template #header>
          <span
            class="order-header"
            data-test="order-header"
            v-onms-tooltip="'Order 1 is evaluated first within this source; the first matching definition wins.'"
          >
            Order
            <OnmsIcon
              :icon="Info"
              class="order-info-icon"
              data-test="order-info"
              aria-label="Order 1 is evaluated first within this source; the first matching definition wins."
              focusable="false"
            />
          </span>
        </template>
      </OnmsColumn>
      <OnmsColumn
        field="uei"
        header="Event UEI"
        sortable
      />
      <OnmsColumn
        field="eventLabel"
        header="Event Label"
        sortable
      />
      <OnmsColumn
        field="severity"
        header="Severity"
        sortable
      >
        <template #body="{ data }">
          <OnmsTag
            :class="`${data.severity.toLowerCase()}-color severity`"
            :value="data.severity"
          />
        </template>
      </OnmsColumn>
      <OnmsColumn
        field="enabled"
        header="Status"
        sortable
      >
        <template #body="{ data }">
          <OnmsTag
            :class="data.enabled ? 'enabled-tag' : 'disabled-tag'"
            :value="data.enabled ? 'Enabled' : 'Disabled'"
            data-test="status-tag"
          />
        </template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <div class="action-container">
            <OnmsIconButton
              :title="`Edit ${data.eventLabel}`"
              data-test="edit-button"
              :icon="Edit"
              @click="onEditEvent(data)"
            />
            <OnmsIconButton
              aria-haspopup="true"
              aria-controls="event-row-menu"
              title="More Options"
              data-test="row-menu-button"
              :icon="MenuIcon"
              @click="toggleRowMenu($event, data)"
            />
          </div>
        </template>
      </OnmsColumn>
      <template #expansion="{ data }">
        <div class="expanded-content">
          <h6>Description:</h6>
          <p
            class="description"
            v-html="data.description"
          ></p>
        </div>
      </template>
    </OnmsTable>

    <OnmsMenu
      id="event-row-menu"
      ref="rowMenu"
      :items="rowMenuItems"
    />

    <div v-if="!store.eventsReorderMode && !store.events.length">
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
import { onBeforeRouteLeave, useRouter } from 'vue-router'

import { VENDOR_OPENNMS } from '@/lib/utils'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { CreateEditMode } from '@/types'
import { EventConfigEvent, EventConfigEventSummary } from '@/types/eventConfig'
import {
  OnmsButton,
  OnmsColumn,
  OnmsIcon,
  OnmsIconButton,
  OnmsMenu,
  OnmsMenuItem,
  OnmsSearchInput,
  OnmsTable,
  OnmsTag,
  type OnmsTablePageEvent,
  type OnmsTableSortEvent
} from '@opennms/onms-ui'
import Edit from '@opennms/onms-ui/icons/action/Edit.vue'
import Info from '@opennms/onms-ui/icons/action/Info.vue'
import MenuIcon from '@opennms/onms-ui/icons/navigation/MoreHoriz.vue'
import Refresh from '@opennms/onms-ui/icons/navigation/Refresh.vue'
import { debounce } from 'lodash'
import EmptyList from '../Common/EmptyList.vue'
import FormField from '@/components/Common/FormField.vue'
import StagedReorderList from '@/components/Common/StagedReorderList.vue'
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

// Leaving the page (Go Back, breadcrumbs, menu) with unsaved reorder edits asks first,
// the same way the Cancel button does. An allowed leave also ends the reorder mode:
// the store outlives the page, so the mode must not still be on when the page is reopened.
const reorderList = ref<{ confirmLeave: () => Promise<boolean> } | null>(null)
onBeforeRouteLeave(async () => {
  if (store.eventsReorderMode && reorderList.value) {
    const leave = await reorderList.value.confirmLeave()
    if (leave) {
      store.stopEventsReorder()
    }
    return leave
  }
  return true
})

const eventLabel = (event: EventConfigEventSummary) => event.eventLabel
const eventSearchText = (event: EventConfigEventSummary) => `${event.uei} ${event.eventLabel}`

const rowMenu = ref()
const rowMenuTarget = ref<EventConfigEvent | null>(null)
const rowMenuItems = computed<OnmsMenuItem[]>(() => {
  const target = rowMenuTarget.value
  if (!target) {
    return []
  }
  const items: OnmsMenuItem[] = [
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

const onSort = (event: OnmsTableSortEvent) => {
  if (event.sortField) {
    store.onEventsSortChange(String(event.sortField), event.sortOrder === 1 ? 'asc' : 'desc')
  } else {
    store.onEventsSortChange('eventOrder', 'asc')
  }
}

const onPage = (event: OnmsTablePageEvent) => {
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
@use '@/styles/onms-typography' as *;
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
      flex: 1;
      min-width: 0;

      .title {
        @include onms-headline3;
        white-space: nowrap;
      }
    }

    .action-container {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      gap: 8px;
      flex: 0 0 auto;

      .reorder-button {
        white-space: nowrap;
      }

      .search-container {
        width: 24em;
      }
    }
  }

  .action-container {
    display: flex;
    align-items: center;
    gap: 5px;
  }

  .severity {
    @include onms-caption;
  }

  .order-header {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    cursor: help;
  }

  .order-info-icon {
    color: var(--p-text-muted-color);
  }

  .reorder-item-info {
    flex-grow: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;

    .reorder-item-title {
      font-weight: 600;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .reorder-item-meta {
      font-size: 0.8rem;
      color: var(--p-text-muted-color);
    }

    .reorder-item-uei {
      font-family: ui-monospace, Menlo, monospace;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
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
