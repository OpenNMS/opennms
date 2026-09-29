<template>
  <TableCard class="event-configuration-table">
    <div class="header">
      <div class="title-container">
        <!-- <span class="title"> SNMP Interfaces </span> -->
      </div>
      <div
        class="header-content-container"
        v-if="!store.sourcesReorderMode"
      >
        <div class="search-container">
          <FormField class="search-field">
            <OnmsSearchInput
              :input-id="searchId"
              :modelValue="store.sourcesSearchTerm"
              @update:modelValue="onChangeSearchTerm"
              data-test="search-input"
              placeholder="Search by Source, Vendor, UEI or Label"
              :aria-label="'Search by Source, Vendor, UEI or Label'"
            />
          </FormField>
        </div>
        <div class="refresh">
          <OnmsIconButton
            title="Refresh"
            data-test="refresh-button"
            :icon="Refresh"
            @click="store.refreshSourcesFilters()"
          />
        </div>
      </div>
    </div>

    <StagedReorderList
      v-if="store.sourcesReorderMode"
      ref="reorderList"
      :items="store.orderedSources"
      :item-label="sourceLabel"
      :item-search-text="sourceSearchText"
      item-noun="source"
      intro="Sources at the top are evaluated first when matching events. Drag rows or use the arrows; nothing changes until you save."
      filter-placeholder="Filter by name or vendor — several terms match any"
      :saving="store.isSavingSourceOrder"
      :save="store.saveSourcesOrder"
      :refetch="store.fetchOrderedSources"
      @close="store.stopSourcesReorder()"
    >
      <template #item="{ item }">
        <div class="reorder-item-info">
          <span class="reorder-item-title">{{ item.name }}</span>
          <span class="reorder-item-meta">{{ item.vendor }} &middot; {{ item.eventCount }} events</span>
        </div>
        <OnmsTag
          :class="item.enabled ? 'enabled-tag' : 'disabled-tag'"
          :value="item.enabled ? 'Enabled' : 'Disabled'"
        />
      </template>
      <template #pinned>
        <div
          v-if="store.catchAllSource"
          class="catch-all-row"
          data-test="catch-all-row"
        >
          <OnmsIcon
            :icon="Lock"
            aria-hidden="true"
            focusable="false"
          />
          <div class="reorder-item-info">
            <span class="reorder-item-title">{{ store.catchAllSource.name }}</span>
            <span class="reorder-item-meta">Always evaluated last (pinned)</span>
          </div>
          <OnmsTag
            :class="store.catchAllSource.enabled ? 'enabled-tag' : 'disabled-tag'"
            :value="store.catchAllSource.enabled ? 'Enabled' : 'Disabled'"
          />
        </div>
      </template>
    </StagedReorderList>

    <OnmsTable
      v-else-if="store.sources.length"
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
      <OnmsColumn
        field="evaluationOrder"
        sortable
        style="width: 6rem"
      >
        <template #header>
          <span
            class="order-header"
            data-test="order-header"
            v-onms-tooltip="'Order 1 is evaluated first when matching events; the catch-all source is always last.'"
          >
            Order
            <OnmsIcon
              :icon="Info"
              class="order-info-icon"
              data-test="order-info"
              aria-label="Order 1 is evaluated first when matching events; the catch-all source is always last."
              focusable="false"
            />
          </span>
        </template>
      </OnmsColumn>
      <OnmsColumn
        field="name"
        header="Source"
        sortable
      />
      <OnmsColumn
        field="vendor"
        header="Vendor"
        sortable
      />
      <OnmsColumn
        field="eventCount"
        header="Event Count"
        sortable
      />
      <OnmsColumn header="Status">
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
              :title="`View ${data.name}`"
              data-test="view-button"
              :icon="ViewDetails"
              @click="onEventClick(data)"
            />
            <OnmsIconButton
              :title="`Download ${data.name} XML`"
              data-test="download-button"
              :icon="Download"
              @click="downloadEventConfXmlBySourceId(data.id)"
            />
            <OnmsIconButton
              aria-haspopup="true"
              aria-controls="event-source-row-menu"
              :title="`More actions for ${data.name}`"
              data-test="row-menu-button"
              :icon="MenuIcon"
              @click="toggleRowMenu($event, data)"
            />
          </div>
        </template>
      </OnmsColumn>
    </OnmsTable>

    <OnmsMenu
      id="event-source-row-menu"
      ref="rowMenu"
      :items="rowMenuItems"
    />

    <div v-if="!store.sourcesReorderMode && !store.sources.length">
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
import { onBeforeRouteLeave, useRouter } from 'vue-router'

import { VENDOR_OPENNMS } from '@/lib/utils'
import { downloadEventConfXmlBySourceId } from '@/services/eventConfigService'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { EventConfigSource } from '@/types/eventConfig'
import {
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
import Download from '@opennms/onms-ui/icons/action/DownloadFile.vue'
import Info from '@opennms/onms-ui/icons/action/Info.vue'
import Lock from '@opennms/onms-ui/icons/action/Lock.vue'
import ViewDetails from '@opennms/onms-ui/icons/action/ViewDetails.vue'
import MenuIcon from '@opennms/onms-ui/icons/navigation/MoreHoriz.vue'
import Refresh from '@opennms/onms-ui/icons/navigation/Refresh.vue'
import { debounce } from 'lodash'
import EmptyList from '../Common/EmptyList.vue'
import FormField from '@/components/Common/FormField.vue'
import StagedReorderList from '@/components/Common/StagedReorderList.vue'
import TableCard from '../Common/TableCard.vue'
import ChangeEventConfigSourceStatusDialog from './Dialog/ChangeEventConfigSourceStatusDialog.vue'
import DeleteEventConfigSourceDialog from './Dialog/DeleteEventConfigSourceDialog.vue'

const router = useRouter()
const store = useEventConfigStore()
const searchId = useId()
const emptyListContent = {
  msg: 'No results found.'
}

const sourceLabel = (source: EventConfigSource) => source.name
const sourceSearchText = (source: EventConfigSource) => `${source.name} ${source.vendor}`

// Leaving the page (source detail link, breadcrumbs, menu) with unsaved reorder edits asks
// first, the same way the Cancel button does.
const reorderList = ref<{ confirmLeave: () => Promise<boolean> } | null>(null)
onBeforeRouteLeave(() => {
  if (store.sourcesReorderMode && reorderList.value) {
    return reorderList.value.confirmLeave()
  }
  return true
})

const rowMenu = ref()
const rowMenuTarget = ref<EventConfigSource | null>(null)
const rowMenuItems = computed<OnmsMenuItem[]>(() => {
  const target = rowMenuTarget.value
  if (!target) {
    return []
  }
  const items: OnmsMenuItem[] = [
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

const onSort = (event: OnmsTableSortEvent) => {
  if (event.sortField) {
    store.onSourcesSortChange(String(event.sortField), event.sortOrder === 1 ? 'asc' : 'desc')
  } else {
    store.onSourcesSortChange('evaluationOrder', 'asc')
  }
}

const onPage = (event: OnmsTablePageEvent) => {
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

  .order-header {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    cursor: help;
  }

  .order-info-icon {
    color: var(--p-text-muted-color);
  }

  .header {
    display: flex;
    justify-content: flex-end;
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
      flex: 0 0 auto;

      .search-container {
        // width: 80%;

        flex: 0 0 auto;
        min-width: 30em;

        .search-field {
          width: 100%;
        }

        .refresh {
          display: flex;
        }
      }
    }
  }

  .action-container {
    display: flex;
    align-items: center;
    gap: 5px;
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
  }

  .catch-all-row {
    display: flex;
    gap: 0.6rem;
    margin-bottom: 0.5rem;
    border: 1px dashed var(--p-content-border-color);
    padding: 4px 10px;
    border-radius: 5px;
    align-items: center;
    background: var(--p-content-hover-background);
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
