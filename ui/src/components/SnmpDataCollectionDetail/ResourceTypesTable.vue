<template>
  <TableCard class="resource-types-table-container">
    <div class="header">
      <div class="title-container">
        <h2 class="title">Resource Types</h2>
      </div>
      <div class="action-container">
        <div class="search-container">
          <FeatherInput
            label="Search"
            type="search"
            data-test="search-input"
            v-model.trim="store.resourceTypesSearchTerm"
            :hint="'Search by Name or Label'"
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
            @click="store.resetResourceTypesFilters"
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
        v-if="store.resourceTypes.length"
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
            v-for="resourceType in store.resourceTypes"
            :key="resourceType.id"
          >
            <tr>
              <td>{{ resourceType.name }}</td>
              <td>{{ resourceType.label }}</td>
              <td>{{ resourceType.resourceLabel }}</td>
              <td>{{ resourceType.enabled ? 'Enabled' : 'Disabled' }}</td>
              <td>
                <div class="action-container">
                  <FeatherButton
                    icon="Edit"
                    :title="`Edit ${resourceType.name}`"
                    data-test="edit-button"
                    @click="onResourceTypeEditClicked(resourceType)"
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
                      {{ resourceType.enabled ? 'Disable Resource Type' : 'Enable Resource Type' }}
                    </FeatherDropdownItem>
                    <FeatherDropdownItem data-test="delete-resource-type-button">
                      Delete Resource Type
                    </FeatherDropdownItem>
                  </FeatherDropdown>
                  <FeatherButton
                    primary
                    :icon="`${expandedRows.includes(resourceType.id)
                    ? 'Expand Less'
                    : 'Expand More'
                    }`"
                    @click="toggleExpand(resourceType.id)"
                  >
                    <FeatherIcon
                      :icon="ExpandLess"
                      v-if="expandedRows.includes(resourceType.id)"
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
              v-if="expandedRows.includes(resourceType.id)"
              class="expanded-content"
            >
              <td :colspan="5">
                <h6>Storage Strategy:</h6>
                <p class="description">{{ resourceType.storageStrategy }}</p>
                <h6>Persistence Selector Strategy:</h6>
                <p class="description">{{ resourceType.persistenceSelectorStrategy }}</p>
              </td>
            </tr>
          </template>
        </TransitionGroup>
      </table>
      <div
        class="alerts-pagination"
        v-if="store.resourceTypes.length"
      >
        <FeatherPagination
          :modelValue="store.resourceTypesPagination.page"
          :pageSize="store.resourceTypesPagination.pageSize"
          :total="store.resourceTypesPagination.total"
          :pageSizes="[10, 20, 30]"
          @update:modelValue="store.onResourceTypesPageChange"
          @update:pageSize="store.onResourceTypesPageSizeChange"
          data-test="FeatherPagination"
        />
      </div>
    </div>
  </TableCard>
</template>

<script setup lang="ts">
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { SnmpCollectionResourceType } from '@/types/snmpDataCollection'
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
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSortHeader, SORT } from '@featherds/table'
import { debounce } from 'lodash'
import TableCard from '../Common/TableCard.vue'

const store = useSnmpDataCollectionDetailStore()
const expandedRows = ref<number[]>([])

const columns = computed(() => [
  { id: 'name', label: 'Name' },
  { id: 'label', label: 'Label' },
  { id: 'resourceLabel', label: 'Resource Label' },
  { id: 'enabled', label: 'Status' }
])

const sort = reactive({
  name: SORT.NONE,
  label: SORT.NONE,
  resourceLabel: SORT.NONE,
  enabled: SORT.NONE
}) as any

const onResourceTypeEditClicked = (resourceType: SnmpCollectionResourceType) => {
  // Placeholder for future action when a resource type is clicked
  console.log('Resource Type clicked:', resourceType)
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
    store.onResourceTypesSortChange(sortObj.property, sortObj.value)
  } else {
    store.onResourceTypesSortChange('createdTime', 'desc')
  }

  for (const prop in sort) {
    sort[prop] = SORT.NONE
  }
  sort[sortObj.property] = sortObj.value
}

const onChangeSearchTerm = debounce(async (value: string) => {
  await store.onChangeResourceTypesSearchTerm(value)
}, 500)

onMounted(async () => {
  await store.fetchResourceTypes()
})
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';

.resource-types-table-container {
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

