<template>
  <TableCard class="snmp-config-profiles-table">
    <div class="header">
      <div class="title-container">
        <!-- <span class="title"> SNMP Profiles </span> -->
      </div>
      <div class="action-container">
        <div class="search-container">
        </div>
        <div class="refresh">
          <FeatherButton
            primary
            icon="Refresh"
            data-test="refresh-button"
            @click="store.populateSnmpConfig()"
          >
            <FeatherIcon :icon="IconRefresh"> </FeatherIcon>
          </FeatherButton>
        </div>
      </div>
    </div>
    <div class="container">
      <table
        class="data-table"
        aria-label="SNMP Config Profile Table"
        v-if="profiles.length"
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
            v-for="profile of profiles"
            :key="`${profile.label ?? ''}-${profile.id}`"
          >
            <td>{{ profile.id }}</td>
            <td>{{ profile.label }}</td>
            <td>{{ profile.filterExpression }}</td>
            <td>
              <div class="action-container">
                <FeatherButton
                  icon="Edit"
                  data-test="edit-button"
                  @click="onProfileEdit(profile.id)"
                >
                  <FeatherIcon :icon="IconEdit"> </FeatherIcon>
                </FeatherButton>
                <FeatherButton
                  icon="Delete"
                  data-test="delete-button"
                  @click="onProfileDelete(profile.id)"
                >
                  <FeatherIcon :icon="IconDelete"> </FeatherIcon>
                </FeatherButton>
              </div>
            </td>
          </tr>
        </TransitionGroup>
      </table>
      <div
        class="snmp-profiles-pagination"
        v-if="profiles.length"
      >
        <!-- <FeatherPagination
          :modelValue="store.sourcesPagination.page"
          :pageSize="store.sourcesPagination.pageSize"
          :total="store.sourcesPagination.total"
          :pageSizes="[10, 20, 50, 100, 200]"
          @update:modelValue="store.onSourcePageChange"
          @update:pageSize="store.onSourcePageSizeChange"
          data-test="FeatherPagination"
        /> -->
      </div>
      <div v-if="!profiles.length">
        <EmptyList
          :content="emptyListContent"
          data-test="empty-list"
        />
      </div>
    </div>
  </TableCard>
</template>

<script lang="ts" setup>
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import { FeatherSortHeader, SORT } from '@featherds/table'
import EmptyList from '../Common/EmptyList.vue'
import TableCard from '../Common/TableCard.vue'

import { useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { SnmpProfile } from '@/types/snmpConfig'
import IconDelete from '@featherds/icon/action/Delete'
import IconEdit from '@featherds/icon/action/Edit'
import IconRefresh from '@featherds/icon/navigation/Refresh'

const store = useSnmpConfigStore()
const router = useRouter()

const emptyListContent = {
  msg: 'No results found.'
}

const columns = computed(() => [
  { id: 'id', label: 'ID' },
  { id: 'label', label: 'Label' },
  { id: 'filterExpression', label: 'Filter Expression' }
])

const sort = reactive({
  label: SORT.NONE,
  filterExpression: SORT.NONE
}) as any

const createFilterExpressionLabel = (profile: SnmpProfile) => {
  return profile.filterExpression ?? '--'
}

const profiles = computed(() => {
  if (store.config.profiles?.profile) {
    return store.config.profiles.profile.map(profile => {
      return {
        id: profile.id ?? -1,
        label: profile.label ?? '--',
        filterExpression: createFilterExpressionLabel(profile)
      }
    })
  }

  return []
})

const sortChanged = (sortObj: { property: string; value: SORT }) => {
  if (sortObj.value === 'asc' || sortObj.value === 'desc') {
    // store.onSourcesSortChange(sortObj.property, sortObj.value)
  } else {
    // store.onSourcesSortChange('createdTime', 'desc')
  }

  for (const prop in sort) {
    sort[prop] = SORT.NONE
  }
  sort[sortObj.property] = sortObj.value
}

const onProfileDelete = (id: number) => {
  alert(`Deleting profile with id: ${id}`)
}

const onProfileEdit = (id: number) => {
  router.push({
    name: 'SNMP Config Profile',
    params: { id: String(id) }
  })
}
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';

.snmp-config-profiles-table {
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

    .snmp-profiles-pagination {
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
