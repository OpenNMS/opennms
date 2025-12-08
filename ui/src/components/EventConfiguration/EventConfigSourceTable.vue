<template>
  <TableCard class="event-configuration-table">
    <div class="header">
      <div class="title-container">
        <!-- <span class="title"> SNMP Interfaces </span> -->
      </div>
      <div class="action-container">
        <div class="search-container">
          <FeatherInput
            label="Search"
            type="search"
            data-test="search-input"
            v-model.trim="store.sourcesSearchTerm"
            :hint="'Search by Source, Vendor, UEI or Label'"
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
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <TransitionGroup
          name="data-table"
          tag="tbody"
        >
          <tr
            v-for="config in store.sources"
            :key="config.id"
          >
            <td>{{ config.name }}</td>
            <td>{{ config.vendor }}</td>
            <td>{{ config.eventCount }}</td>
            <td>{{ config.enabled ? 'Enabled' : 'Disabled' }}</td>
            <td>
              <div class="action-container">
                <FeatherButton
                  icon="View Details"
                  data-test="view-button"
                  @click="onEventClick(config)"
                >
                  <FeatherIcon :icon="ViewDetails"> </FeatherIcon>
                </FeatherButton>
                <FeatherButton
                  icon="Download XML"
                  data-test="download-button"
                  @click="downloadEventConfXmlBySourceId(config.id)"
                >
                  <FeatherIcon :icon="Download"> </FeatherIcon>
                </FeatherButton>
                <FeatherDropdown>
                  <template v-slot:trigger="{ attrs, on }">
                    <FeatherButton
                      link
                      href="#"
                      v-bind="attrs"
                      v-on="on"
                      :icon="`More actions for ${config.name}`"
                    >
                      <FeatherIcon :icon="MenuIcon" />
                    </FeatherButton>
                  </template>
                  <FeatherDropdownItem
                    @click="store.showChangeEventConfigSourceStatusDialog(config)"
                    data-test="change-status-button"
                  >
                    {{ config.enabled ? 'Disable Source' : 'Enable Source' }}
                  </FeatherDropdownItem>
                  <FeatherDropdownItem
                    @click="store.showDeleteEventConfigSourceModal(config)"
                    data-test="delete-source-button"
                    v-if="config.vendor !== VENDOR_OPENNMS"
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
    <DeleteEventConfigSourceDialog />
    <ChangeEventConfigSourceStatusDialog />
  </TableCard>
</template>

<script lang="ts" setup>
import { VENDOR_OPENNMS } from '@/lib/utils'
import { downloadEventConfXmlBySourceId } from '@/services/eventConfigService'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { EventConfigSource } from '@/types/eventConfig'
import { FeatherButton } from '@featherds/button'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherIcon } from '@featherds/icon'
import Download from '@featherds/icon/action/DownloadFile'
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
import ChangeEventConfigSourceStatusDialog from './Dialog/ChangeEventConfigSourceStatusDialog.vue'
import DeleteEventConfigSourceDialog from './Dialog/DeleteEventConfigSourceDialog.vue'

const router = useRouter()
const store = useEventConfigStore()
const emptyListContent = {
  msg: 'No results found.'
}

const columns = computed(() => [
  { id: 'name', label: 'Source' },
  { id: 'vendor', label: 'Vendor' },
  { id: 'eventCount', label: 'Event Count' }
])

const sort = reactive({
  name: SORT.NONE,
  vendor: SORT.NONE,
  description: SORT.NONE,
  eventCount: SORT.NONE
}) as any

const onEventClick = (source: EventConfigSource) => {
  router.push({
    name: 'Event Configuration Detail',
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

onMounted(async () => {
  await store.fetchEventConfigs()
})
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';

.event-configuration-table {
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

