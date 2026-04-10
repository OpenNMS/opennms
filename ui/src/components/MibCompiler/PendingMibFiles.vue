<template>
  <TableCard class="pending-mib-files-container">
    <div class="header">
      <div class="section-left">
        <h3>Pending MIB Files</h3>
      </div>
      <div class="section-right">
        <FeatherInput
          label="Search MIBs"
          placeholder="Search Pending MIB Files"
        >
          <template #pre>
            <FeatherIcon :icon="Search" />
          </template>
        </FeatherInput>
      </div>
    </div>
    <div class="container">
      <table
        class="data-table"
        aria-label="Pending MIB Files Table"
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
            v-for="config in store.paginatedPendingMibFiles"
            :key="config.fileName"
          >
            <td>{{ config.fileName }}</td>
            <td>{{ config.location }}</td>
            <td>
              <div class="action-container">
                <FeatherButton
                  icon="View Details"
                  data-test="view-button"
                >
                  <FeatherIcon :icon="Generic"> </FeatherIcon>
                </FeatherButton>
                <FeatherButton
                  icon="View Details"
                  data-test="view-button"
                >
                  <FeatherIcon :icon="StackedBarChart"> </FeatherIcon>
                </FeatherButton>
                <FeatherButton
                  icon="Download XML"
                  data-test="download-button"
                >
                  <FeatherIcon :icon="Delete"> </FeatherIcon>
                </FeatherButton>
              </div>
            </td>
          </tr>
        </TransitionGroup>
      </table>
      <div
        class="alerts-pagination"
        v-if="store.filteredPendingMibFiles.length"
      >
        <FeatherPagination
          :modelValue="store.pendingMibFilesPagination.page"
          :pageSize="store.pendingMibFilesPagination.pageSize"
          :total="store.pendingMibFilesPagination.total"
          :pageSizes="[10, 20, 50, 100, 200]"
          @update:modelValue="store.onPendingMibFilesPageChange"
          @update:pageSize="store.onPendingMibFilesPageSizeChange"
          data-test="FeatherPagination"
        />
      </div>
      <div v-if="!store.filteredPendingMibFiles.length">
        <EmptyList
          :content="emptyListContent"
          data-test="empty-list"
        />
      </div>
    </div>
  </TableCard>
</template>

<script setup lang="ts">
import { useMibCompilerStore } from '@/stores/mibCompilerStore'
import { FeatherButton } from '@featherds/button'
import Delete from '@featherds/icon/action/Delete'
import Search from '@featherds/icon/action/Search'
import StackedBarChart from '@featherds/icon/datavis/StackedBarChart'
import Generic from '@featherds/icon/file/Generic'
import FeatherIcon from '@featherds/icon/src/components/FeatherIcon.vue'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSortHeader, SORT } from '@featherds/table'
import EmptyList from '../Common/EmptyList.vue'
import TableCard from '../Common/TableCard.vue'

const store = useMibCompilerStore()
const emptyListContent = {
  msg: 'No results found.'
}

const columns = computed(() => [
  { id: 'fileName', label: 'MIB File' }
])

const sort = reactive({
  fileName: SORT.NONE
}) as any

const sortChanged = (sortObj: { property: string; value: SORT }) => {
  for (const prop in sort) {
    sort[prop] = SORT.NONE
  }
  sort[sortObj.property] = sortObj.value
  store.onPendingMibFilesSortChange({
    property: sortObj.property as 'fileName' | 'location',
    value: sortObj.value
  })
}
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';

.pending-mib-files-container {
  margin-top: 10px;
  margin-bottom: 20px;
  padding: 25px;
  border: 1px solid var(--feather-border-on-surface);

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    div {
      flex: 1;
    }

    :deep(.section-right) {
      .feather-input-container {
        float: right;
        width: 50%;

        .feather-input-sub-text {
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

        .action-container {
          display: flex;
          align-items: center;
          gap: 5px;

          button {
            margin: 0px;
          }
        }
      }
    }
  }
}
</style>

