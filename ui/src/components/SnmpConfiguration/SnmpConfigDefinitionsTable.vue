<template>
  <TableCard class="snmp-config-definitions-table">
    <div class="header">
      <div class="title-container">
        <!-- <span class="title"> SNMP Interfaces </span> -->
      </div>
      <div class="action-container">
        <div class="search-container">
          <!-- <FeatherInput
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
          </FeatherInput> -->
        </div>
        <div class="refresh">
          <FeatherButton
            primary
            icon="Refresh"
            data-test="refresh-button"
            @click="store.refresh()"
          >
            <FeatherIcon :icon="IconRefresh"> </FeatherIcon>
          </FeatherButton>
        </div>
      </div>
    </div>
    <div class="container">
      <table
        class="data-table"
        aria-label="SNMP Config Definition Table"
        v-if="definitions.length"
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
            v-for="definition of definitions"
            :key="`${definition.label ?? ''}-${definition.id}`"
          >
            <td>{{ definition.id }}</td>
            <td>{{ definition.label }}</td>
            <td>{{ definition.rangeType }}</td>
            <td>{{ definition.ipAddresses }}</td>
            <td>{{ definition.location }}</td>
            <td>{{ definition.profileLabel }}</td>
            <td>
              <div class="action-container">
                <FeatherButton
                  icon="Edit"
                  data-test="edit-button"
                  @click="onDefinitionEdit(definition.id)"
                >
                  <FeatherIcon :icon="IconEdit"> </FeatherIcon>
                </FeatherButton>
                <FeatherButton
                  v-if="definition.label !== 'Global'"
                  icon="Delete"
                  data-test="delete-button"
                  @click="onDefinitionDelete(definition.id)"
                >
                  <FeatherIcon :icon="IconDelete"> </FeatherIcon>
                </FeatherButton>
              </div>
            </td>
          </tr>
        </TransitionGroup>
      </table>

      <div
        class="snmp-config-definitions-pagination"
        v-if="definitions.length"
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
      <div v-if="!definitions.length">
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
import { SnmpDefinition } from '@/types/snmpConfig'
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
  { id: 'rangeType', label: 'Range Type' },
  { id: 'ipAddresses', label: 'IP Addresses' },
  { id: 'location', label: 'Location' },
  { id: 'profileLabel', label: 'Profile Label' }
])

const sort = reactive({
  label: SORT.NONE,
  rangeType: SORT.NONE,
  ipAddresses: SORT.NONE,
  location: SORT.NONE,
  profileLabel: SORT.NONE
}) as any

const getRangeType = (d: SnmpDefinition) => {
  if (d.ranges?.length > 0) {
    return 'Range'
  }

  if (d.specifics?.length > 0) {
    return 'Specific'
  }

  if (d.ipMatches?.length > 0) {
    return 'IP Match'
  }

  return '--'
}

const createIpAddressLabel = (d: SnmpDefinition) => {
  // IP Range
  if (d.ranges?.length) {
    let s = `${d.ranges[0].begin} - ${d.ranges[0].end}`

    if (d.ranges.length > 1) {
      s += '...'
    }

    return s
  }

  // Specific IPs
  if (d.specifics?.length) {
    let s = d.specifics[0]

    if (d.specifics.length > 1) {
      s += '...'
    }

    return s
  }

  // IP Match
  if (d.ipMatches?.length) {
    let s = d.ipMatches[0]

    if (d.ipMatches.length > 1) {
      s += '...'
    }

    return s
  }

  return '--'
}

const definitions = computed(() => {
  return store.config.definitions?.map(d => {
    return {
      id: d.id ?? -1,
      label: d.id === 0 ? 'Global' : '--',
      rangeType: getRangeType(d),
      ipAddresses: createIpAddressLabel(d),
      location: d.location,
      profileLabel: d.profileLabel ?? '--'
    }
  })
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

const onDefinitionDelete = (index: number) => {
  alert(`Deleting index: ${index} (not yet implemented)`)
}

const onDefinitionEdit = (index: number) => {
  router.push({
    name: 'SNMP Config Definition',
    params: { id: String(index) }
  })
}
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';

.snmp-config-definitions-table {
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

    .snmp-config-definitions-pagination {
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
