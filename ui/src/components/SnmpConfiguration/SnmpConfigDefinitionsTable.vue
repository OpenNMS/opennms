<template>
  <TableCard class="snmp-config-definitions-table">
    <div class="header">
      <div class="title-container">
        <!-- <span class="title"> SNMP Interfaces </span> -->
      </div>
      <div class="action-container">
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
            v-for="(definition, index) of definitions"
            :key="`${definition.label ?? ''}-${definition.id}`"
          >
            <td>{{ definition.location ?? DEFAULT_MONITORING_LOCATION }}</td>
            <td>{{ definition.rangeType }}</td>
            <td v-if="definition.ipAddresses.length > 1">
              <div v-for="ipAddr of definition.ipAddresses" :key="ipAddr">
                {{ ipAddr }}
              </div>
            </td>
            <td v-else>{{ definition.ipAddresses?.[0] ?? '--' }}</td>
            <td>{{ definition.profileLabel }}</td>
            <td>
              <div class="action-container">
                <FeatherButton
                  icon="Edit"
                  data-test="edit-button"
                  @click="onDefinitionEdit(store.config.definitions?.[index])"
                >
                  <FeatherIcon :icon="IconEdit"> </FeatherIcon>
                </FeatherButton>
                <FeatherButton
                  v-if="definition.label !== 'Global'"
                  icon="Delete"
                  data-test="delete-button"
                  @click="onDefinitionDelete(store.config.definitions?.[index])"
                >
                  <FeatherIcon :icon="IconDelete"> </FeatherIcon>
                </FeatherButton>
              </div>
            </td>
          </tr>
        </TransitionGroup>
      </table>
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

import { SnmpConfigEditMode, useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { SnmpDefinition } from '@/types/snmpConfig'
import IconDelete from '@featherds/icon/action/Delete'
import IconEdit from '@featherds/icon/action/Edit'
import { DEFAULT_MONITORING_LOCATION } from '@/lib/constants'

const store = useSnmpConfigStore()

const emptyListContent = {
  msg: 'No results found.'
}

const columns = computed(() => [
  { id: 'location', label: 'Location' },
  { id: 'rangeType', label: 'Range Type' },
  { id: 'ipAddresses', label: 'IP Addresses' },
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
  const rangeTypes: string[] = []

  if (d.ranges?.length > 0) {
    rangeTypes.push('Range')
  }

  if (d.specifics?.length > 0) {
    rangeTypes.push('Specific')
  }

  if (d.ipMatches?.length > 0) {
    rangeTypes.push('IP Match')
  }

  if (rangeTypes.length > 0) {
    return rangeTypes.join(', ')
  }

  return '--'
}

const createIpAddressLabel = (d: SnmpDefinition) => {
  const items: string[] = []

  // IP Range
  if (d.ranges?.length) {
    const ranges = d.ranges.map(r => `${r.begin} - ${r.end}`)
    items.push(...ranges)
  }

  // Specific IPs
  if (d.specifics?.length) {
    items.push(...d.specifics)
  }

  // IP Match
  if (d.ipMatches?.length) {
    items.push(...d.ipMatches)
  }

  return items
}

const definitions = computed(() => {
  if (store.config.definitions) {
    return store.config.definitions?.map((d, index) => {
      return {
        id: d.id ?? index,
        label: d.id === 0 ? 'Global' : '--',
        rangeType: getRangeType(d),
        ipAddresses: createIpAddressLabel(d),
        location: d.location,
        profileLabel: d.profileLabel ?? '--'
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

const onDefinitionDelete = (definition?: SnmpDefinition) => {
  if (definition) {
    alert('Deleting definition: (not yet implemented)')
  }
}

const onDefinitionEdit = (definition?: SnmpDefinition) => {
  if (definition) {
    store.setCurrentDefinition(definition)
    store.setDefinitionCreateEditMode(SnmpConfigEditMode.Edit)
  }
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
