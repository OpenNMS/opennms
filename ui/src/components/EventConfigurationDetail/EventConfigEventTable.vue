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
          >
            <template #pre>
              <FeatherIcon :icon="Search" />
            </template>
          </FeatherInput>
        </div>
        <div class="download-csv">
          <FeatherButton
            primary
            icon="Download"
            data-test="download-button"
          >
            <FeatherIcon :icon="DownloadFile"> </FeatherIcon>
          </FeatherButton>
        </div>
        <div class="refresh">
          <FeatherButton
            primary
            icon="Refresh"
          >
            <FeatherIcon :icon="Refresh"> </FeatherIcon>
          </FeatherButton>
        </div>
      </div>
    </div>
    <div class="container">
      <table
        class="data-table"
        aria-label="SNMP Interfaces Table"
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
            <th>Actions</th>
          </tr>
        </thead>
        <TransitionGroup
          name="data-table"
          tag="tbody"
        >
          <tr
            v-for="event in store.events"
            :key="event.id"
          >
            <td>{{ event.uei }}</td>
            <td>{{ event.eventLabel }}</td>
            <td>{{ event.description }}</td>
            <td>{{ event.enabled ? 'Enabled' : 'Disabled' }}</td>
            <td>
              <div class="action-container">
                <FeatherButton
                  icon="Edit"
                  :title="`Edit ${event.eventLabel}`"
                  data-test="edit-button"
                  @click="openEventDrawer(event)"
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
                      :icon="`More actions for ${event.eventLabel}`"
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
                  >
                    Delete Event
                  </FeatherDropdownItem>
                </FeatherDropdown>
              </div>
            </td>
          </tr>
        </TransitionGroup>
      </table>
      <div class="alerts-pagination">
        <FeatherPagination
          :modelValue="store.eventsPagination.page"
          :pageSize="store.eventsPagination.pageSize"
          :total="store.eventsPagination.total"
          :pageSizes="[10, 20, 50]"
          @update:modelValue="store.onEventsPageChange"
          @update:pageSize="store.onEventsPageSizeChange"
          data-test="FeatherPagination"
          v-if="store.events.length"
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
    <ChangeEventConfEventStatusDialog />
  </TableCard>
  <EventConfigDetailsDrawer  :event="selectedEvent" />
</template>

<script setup lang="ts">
import { useEventConfigDetailStore } from '@/stores/eventConfigDetailStore'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import DownloadFile from '@featherds/icon/action/DownloadFile'
import Edit from '@featherds/icon/action/Edit'
import Search from '@featherds/icon/action/Search'
import MenuIcon from '@featherds/icon/navigation/MoreHoriz'
import Refresh from '@featherds/icon/navigation/Refresh'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSortHeader, SORT } from '@featherds/table'
import TableCard from '../Common/TableCard.vue'
import ChangeEventConfEventStatusDialog from './Dialog/ChangeEventConfEventStatusDialog.vue'
import DeleteEventConfigEventDialog from './Dialog/DeleteEventConfigEventDialog.vue'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import EventConfigDetailsDrawer from './Drawer/EventConfigDetailsDrawer.vue'
import { EventConfigEvent } from '@/types/eventConfig'

const store = useEventConfigDetailStore()
const emptyListContent = {
  msg: 'No results found.'
}
const selectedEvent = ref<EventConfigEvent | null>(null)
const columns = computed(() => [
  { id: 'uei', label: 'UEI' },
  { id: 'eventLabel', label: 'Event Label' },
  { id: 'description', label: 'Description' },
  { id: 'enabled', label: 'Status' }
])

const sort = reactive({
  fileName: SORT.NONE,
  description: SORT.NONE,
  fileOrder: SORT.NONE,
  vendor: SORT.NONE,
  eventCount: SORT.NONE
}) as any

const openEventDrawer = (event: EventConfigEvent) => {
  selectedEvent.value = event
  store.openEventDrawerModal()
}

const sortChanged = (sortObj: { property: string; value: SORT }) => {
  for (const prop in sort) {
    sort[prop] = SORT.NONE
  }
  sort[sortObj.property] = sortObj.value
}
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';

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

        .feather-input-container {
          :deep(.feather-input-sub-text) {
            display: none !important;
          }
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

        .action-container {
          display: flex;
          align-items: center;
          gap: 5px;

          button {
            margin: 0px;
          }

          :deep(.feather-menu-dropdown) {
            .feather-dropdown{
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

