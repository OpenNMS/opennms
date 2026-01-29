<template>
  <TableCard class="mib-groups-table-container">
    <div class="header">
      <div class="title-container">
        <h2 class="title">MIB Groups</h2>
      </div>
      <div class="action-container">
        <div class="search-container">
          <FeatherInput
            label="Search"
            type="search"
            data-test="search-input"
            v-model.trim="store.mibGroupsSearchTerm"
            :hint="'Search by Name or Interface Type'"
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
            @click="store.resetMibGroupsFilters"
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
        v-if="store.mibGroups.length"
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
          <template
            v-for="mibGroup in store.mibGroups"
            :key="mibGroup.id"
          >
            <tr>
              <td>{{ mibGroup.name }}</td>
              <td>{{ mibGroup.ifType }}</td>
              <td>{{ mibGroup.enabled ? 'Enabled' : 'Disabled' }}</td>
              <td>
                <div class="action-container">
                  <FeatherButton
                    icon="Edit"
                    :title="`Edit ${mibGroup.name}`"
                    data-test="edit-button"
                    @click="onMibGroupEditClicked(mibGroup)"
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
                    <FeatherDropdownItem data-test="change-status-button">
                      {{ mibGroup.enabled ? 'Disable MIB Group' : 'Enable MIB Group' }}
                    </FeatherDropdownItem>
                    <FeatherDropdownItem data-test="delete-mib-group-button"> Delete MIB Group </FeatherDropdownItem>
                  </FeatherDropdown>
                  <FeatherButton
                    primary
                    :icon="`${expandedRows.includes(mibGroup.id)
                    ? 'Expand Less'
                    : 'Expand More'
                    }`"
                    @click="toggleExpand(mibGroup.id)"
                  >
                    <FeatherIcon
                      :icon="ExpandLess"
                      v-if="expandedRows.includes(mibGroup.id)"
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
              v-if="expandedRows.includes(mibGroup.id)"
              class="expanded-content"
            >
              <td :colspan="5">
                <h5>Mib Group Names</h5>
                <p class="description">{{ JSON.parse(mibGroup.mibGroupNames).join(', ') }}</p>
                <div v-if="JSON.parse(mibGroup.mibObjects).length > 0">
                  <h5>Mib Objects:</h5>
                  <div
                    v-for="(value, index) in JSON.parse(mibGroup.mibObjects)"
                    :key="value.alias"
                  >
                    <h6>Object {{ Number(index) + 1 }}</h6>
                    <div>
                      <strong>Alias:</strong> {{ value.alias }} <br />
                      <strong>OID:</strong> {{ value.oid }} <br />
                      <strong>Instance:</strong> {{ value.instance }} <br />
                      <strong>Data Type:</strong> {{ value.type }}
                    </div>
                  </div>
                </div>
              </td>
            </tr>
          </template>
        </TransitionGroup>
      </table>
      <div
        class="alerts-pagination"
        v-if="store.mibGroups.length"
      >
        <FeatherPagination
          :modelValue="store.mibGroupsPagination.page"
          :pageSize="store.mibGroupsPagination.pageSize"
          :total="store.mibGroupsPagination.total"
          :pageSizes="[10, 20, 30]"
          @update:modelValue="store.onMibGroupsPageChange"
          @update:pageSize="store.onMibGroupsPageSizeChange"
          data-test="FeatherPagination"
        />
      </div>
    </div>
  </TableCard>
</template>

<script setup lang="ts">
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { SnmpCollectionMibGroup } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherIcon } from '@featherds/icon'
import Edit from '@featherds/icon/action/Edit'
import Search from '@featherds/icon/action/Search'
import ExpandLess from '@featherds/icon/navigation/ExpandLess'
import ExpandMore from '@featherds/icon/navigation/ExpandMore'
import MenuIcon from '@featherds/icon/navigation/MoreHoriz'
import Refresh from '@featherds/icon/navigation/Refresh'
import { FeatherInput } from '@featherds/input'
import { FeatherSortHeader, SORT } from '@featherds/table'
import { debounce } from 'lodash'
import TableCard from '../Common/TableCard.vue'
import { FeatherPagination } from '@featherds/pagination'

const store = useSnmpDataCollectionDetailStore()
const expandedRows = ref<number[]>([])

const columns = computed(() => [
  { id: 'name', label: 'Name' },
  { id: 'ifType', label: 'Interface Type' },
  { id: 'enabled', label: 'Status' }
])

const sort = reactive({
  name: SORT.NONE,
  ifType: SORT.NONE,
  enabled: SORT.NONE
}) as any

const onMibGroupEditClicked = (mibGroup: SnmpCollectionMibGroup) => {
  // Placeholder for future action when a resource type is clicked
  console.log('MIB Group clicked:', mibGroup)
}

const toggleExpand = (id: number) => {
  const index = expandedRows.value.indexOf(id)
  if (index === -1) {
    expandedRows.value.push(id)
  } else {
    expandedRows.value.splice(index, 1)
  }
}

const sortChanged = (sortObj: { property: string; value: SORT }) => {
  if (sortObj.value === 'asc' || sortObj.value === 'desc') {
    store.onMibGroupsSortChange(sortObj.property, sortObj.value)
  } else {
    store.onMibGroupsSortChange('createdTime', 'desc')
  }

  for (const prop in sort) {
    sort[prop] = SORT.NONE
  }
  sort[sortObj.property] = sortObj.value
}

const onChangeSearchTerm = debounce(async (value: string) => {
  await store.onChangeMibGroupsSearchTerm(value)
}, 500)

onMounted(async () => {
  await store.fetchMibGroups()
})
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';

.mib-groups-table-container {
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

