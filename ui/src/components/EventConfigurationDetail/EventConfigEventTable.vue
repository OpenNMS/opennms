<template>
  <TableCard class="event-config-event-table">
    <div class="header">
      <div class="title-container">
        <span class="title"> Events </span>
      </div>
      <div class="action-container">
        <div class="search-container">
          <FeatherInput
            label="Search"
            type="search"
            data-test="search-input"
            v-model.trim="store.eventsSearchTerm"
            :hint="'Search by Event UEI or Event Label'"
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
            @click="store.refreshEventConfigEvents()"
          >
            <FeatherIcon :icon="Refresh"> </FeatherIcon>
          </FeatherButton>
        </div>
      </div>
    </div>
    <div class="container">
      <table
        class="data-table"
        aria-label="Events Table"
        v-if="store.events.length"
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
            <th>Severity</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <TransitionGroup
          name="data-table"
          tag="tbody"
        >
          <template
            v-for="event in store.events"
            :key="event.id"
          >
            <tr>
              <td>{{ event.uei }}</td>
              <td>{{ event.eventLabel }}</td>
              <td>
                <FeatherChip :class="`${event.severity.toLowerCase()}-color severity`">
                  {{ event.severity }}
                </FeatherChip>
              </td>
              <td>{{ event.enabled ? 'Enabled' : 'Disabled' }}</td>
              <td>
                <div class="action-container">
                  <FeatherButton
                    icon="Edit"
                    :title="`Edit ${event.eventLabel}`"
                    data-test="edit-button"
                    @click="onEditEvent(event)"
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
                      @click="store.showChangeEventConfigEventStatusDialog(event)"
                      data-test="change-status-button"
                    >
                      {{ event.enabled ? 'Disable Event' : 'Enable Event' }}
                    </FeatherDropdownItem>
                    <FeatherDropdownItem
                      @click="store.showDeleteEventConfigEventDialog(event)"
                      data-test="delete-event-button"
                      v-if="store.selectedSource?.vendor !== VENDOR_OPENNMS"
                    >
                      Delete Event
                    </FeatherDropdownItem>
                  </FeatherDropdown>
                  <FeatherButton
                    primary
                    :icon="`${expandedRows.includes(event.id)
                    ? 'Expand Less'
                    : 'Expand More'
                    }`"
                    @click="toggleExpand(event.id)"
                  >
                    <FeatherIcon
                      :icon="ExpandLess"
                      v-if="expandedRows.includes(event.id)"
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
              v-if="expandedRows.includes(event.id)"
              class="expanded-content"
            >
              <td :colspan="5">
                <h6>Description:</h6>
                <p
                  class="description"
                  v-html="event.description"
                ></p>
              </td>
            </tr>
          </template>
        </TransitionGroup>
      </table>
      <div
        class="alerts-pagination"
        v-if="store.events.length"
      >
        <FeatherPagination
          :modelValue="store.eventsPagination.page"
          :pageSize="store.eventsPagination.pageSize"
          :total="store.eventsPagination.total"
          :pageSizes="[10, 20, 50]"
          @update:modelValue="store.onEventsPageChange"
          @update:pageSize="store.onEventsPageSizeChange"
          data-test="FeatherPagination"
        />
      </div>
      <div v-if="!store.events.length">
        <EmptyList
          :content="emptyListContent"
          data-test="empty-list"
        />
      </div>
    </div>
    <DeleteEventConfigEventDialog />
    <ChangeEventConfigEventStatusDialog />
  </TableCard>
</template>

<script setup lang="ts">
import { VENDOR_OPENNMS } from '@/lib/utils'
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { CreateEditMode } from '@/types'
import { EventConfigEvent } from '@/types/eventConfig'
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
import TableCard from '../Common/TableCard.vue'
import ChangeEventConfigEventStatusDialog from './Dialog/ChangeEventConfigEventStatusDialog.vue'
import DeleteEventConfigEventDialog from './Dialog/DeleteEventConfigEventDialog.vue'

const store = useEventConfigDetailStore()
const router = useRouter()
const emptyListContent = {
  msg: 'No results found.'
}

const expandedRows = ref<number[]>([])
const columns = computed(() => [
  { id: 'uei', label: 'Event UEI' },
  { id: 'eventLabel', label: 'Event Label' }
])

const sort = reactive({
  uei: SORT.NONE,
  eventLabel: SORT.NONE
}) as any

const sortChanged = (sortObj: { property: string; value: SORT }) => {
  if (sortObj.value === 'asc' || sortObj.value === 'desc') {
    store.onEventsSortChange(sortObj.property, sortObj.value)
  } else {
    store.onEventsSortChange('createdTime', 'desc')
  }

  for (const prop in sort) {
    sort[prop] = SORT.NONE
  }
  sort[sortObj.property] = sortObj.value
}

const toggleExpand = (id: number) => {
  const index = expandedRows.value.indexOf(id)
  if (index === -1) {
    expandedRows.value.push(id)
  } else {
    expandedRows.value.splice(index, 1)
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

const onChangeSearchTerm = debounce(async (value: string) => {
  await store.onChangeEventsSearchTerm(value)
}, 500)
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';
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
      align-items: flex-start;
      justify-content: flex-end;
      gap: 5px;
      width: 30%;

      .search-container {
        width: 80%;
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

        .severity {
          @include typography.caption;
          margin: 0 !important;
        }

        div {
          border-radius: 5px;
          padding: 0px 5px 0px 5px;
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

