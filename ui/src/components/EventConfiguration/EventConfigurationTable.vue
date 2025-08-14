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
        v-if="store.eventConfigs.length"
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
            v-for="config in store.eventConfigs"
            :key="config.filename"
          >
            <td>{{ config.filename }}</td>
            <td>{{ config.description }}</td>
            <td>{{ config.fileOrder }}</td>
            <td>{{ config.vendor }}</td>
            <td>{{ config.eventCount }}</td>
            <td>
              <FeatherButton
                primary
                icon="View Details"
                data-test="view-button"
                @click="onEventClick(config.fileOrder)"
              >
                <FeatherIcon :icon="ViewDetails"> </FeatherIcon>
              </FeatherButton>
            </td>
          </tr>
        </TransitionGroup>
      </table>
      <div class="alerts-pagination">
        <FeatherPagination
          :modelValue="store.eventConfigPagination.page"
          :pageSize="store.eventConfigPagination.pageSize"
          :total="store.eventConfigPagination.total"
          :pageSizes="[10, 20, 50]"
          @update:modelValue="store.onEventConfigPageChange"
          @update:pageSize="store.onEventConfigPageSizeChange"
          data-test="FeatherPagination"
          v-if="store.eventConfigs.length"
        />
      </div>
      <div v-if="!store.eventConfigs.length">
        <EmptyList
          :content="emptyListContent"
          data-test="empty-list"
        />
      </div>
    </div>
  </TableCard>
</template>

<script lang="ts" setup>
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import DownloadFile from '@featherds/icon/action/DownloadFile'
import Search from '@featherds/icon/action/Search'
import ViewDetails from '@featherds/icon/action/ViewDetails'
import Refresh from '@featherds/icon/navigation/Refresh'
import { FeatherInput } from '@featherds/input'
import { FeatherSortHeader, SORT } from '@featherds/table'
import TableCard from '../Common/TableCard.vue'
import { FeatherPagination } from '@featherds/pagination'

const router = useRouter() // <-- get router instance
const store = useEventConfigStore()
const emptyListContent = {
  msg: 'No results found.'
}

const columns = computed(() => [
  { id: 'fileName', label: 'Name' },
  { id: 'description', label: 'Description' },
  { id: 'fileOrder', label: 'File Order' },
  { id: 'vendor', label: 'Vendor' },
  { id: 'eventCount', label: 'Event Count' }
])
const onEventClick = (fileOrder: number) => {
  router.push({
    name: 'Event Configuration Details',
    params: { id: fileOrder }
  })
}
const sort = reactive({
  fileName: SORT.NONE,
  description: SORT.NONE,
  fileOrder: SORT.NONE,
  vendor: SORT.NONE,
  eventCount: SORT.NONE
}) as any

const sortChanged = (sortObj: { property: string; value: SORT }) => {
  for (const prop in sort) {
    sort[prop] = SORT.NONE
  }
  sort[sortObj.property] = sortObj.value
}

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

