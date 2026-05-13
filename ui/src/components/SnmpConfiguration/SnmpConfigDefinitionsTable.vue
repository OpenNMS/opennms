<template>
  <TableCard class="snmp-config-definitions-table">
    <div class="header">
      <div class="action-container">
        <div class="search-container">
          <FeatherInput
            v-model="searchTerm"
            @update:modelValue="onSearchChange"
            label="Search IP addresses or location"
          >
            <template #pre>
              <FeatherIcon :icon="IconSearch" />
            </template>
          </FeatherInput>
        </div>
        <div class="refresh">
          <FeatherButton
            primary
            @click="onCreateDefinition"
          >
            <template v-slot:icon>
              <FeatherIcon :icon="IconAdd" aria-hidden="true" focusable="false" class="add-definition-icon" />
              New Definition
            </template>
          </FeatherButton>
        </div>
       </div>
    </div>
    <div class="container">
      <table
        class="data-table"
        aria-label="SNMP Config Definition Table"
        v-if="definitionsView.length"
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
            v-for="definition of definitionsView"
            :key="`${definition.id ?? 0}-${definition.location ?? ''}`"
          >
            <td>{{ definition.location ?? DEFAULT_MONITORING_LOCATION }}</td>
            <td v-if="createIpAddressLabel(definition).length > 0">
              <div class="ip-address-badge-wrapper">
                <FeatherTextBadge
                  v-for="ipAddr of createIpAddressLabel(definition)" :key="ipAddr"
                  :type="BadgeTypes.info">
                  {{ ipAddr }}
                </FeatherTextBadge>
              </div>
            </td>
            <td v-else>--</td>
            <td>
              <div class="action-container">
                <FeatherButton
                  icon="Edit"
                  data-test="edit-button"
                  @click="onDefinitionEdit(definition)"
                >
                  <FeatherIcon :icon="IconEdit"> </FeatherIcon>
                </FeatherButton>
                <FeatherButton
                  v-if="definition.id !== 0"
                  icon="Delete"
                  data-test="delete-button"
                  @click="onDefinitionDelete(definition)"
                >
                  <FeatherIcon :icon="IconDelete"> </FeatherIcon>
                </FeatherButton>
              </div>
            </td>
          </tr>
        </TransitionGroup>
      </table>
      <div
        class="snmp-definitions-pagination"
        v-if="definitionsView.length"
      >
        <FeatherPagination
          :modelValue="currentPage"
          :pageSize="pageSize"
          :total="pageTotal"
          :pageSizes="[20, 50, 100, 200]"
          @update:modelValue="(val: any) => currentPage = Number(val)"
          @update:pageSize="(val: any) => pageSize = Number(val)"
          data-test="FeatherPagination"
        />
      </div>
       <div v-if="!definitionsView.length">
        <EmptyList
          :content="emptyListContent"
          data-test="empty-list"
        />
      </div>
    </div>
  </TableCard>
  <ConfirmationDialog
    :visible="showDeleteConfirmation"
    title="Delete SNMP Definition"
    actionButtonText="Delete"
    @ok="confirmDelete"
    @cancel="cancelDelete"
  >
    <template #content>
      <div class="confirmation-message">
        <p>Are you sure you want to delete this SNMP definition?</p>
        <div v-if="definitionToDelete" class="definition-details">
          <p v-if="definitionToDelete.location"><strong>Location:</strong> {{ definitionToDelete.location }}</p>
          <div v-if="definitionToDelete.range && definitionToDelete.range.length > 0">
            <strong>IP Ranges:</strong>
            <ul>
              <li v-for="(r, idx) in definitionToDelete.range" :key="idx">
                {{ r.begin }} - {{ r.end }}
              </li>
            </ul>
          </div>
          <div v-if="definitionToDelete.specific && definitionToDelete.specific.length > 0">
            <strong>Specific IPs:</strong>
            <ul>
              <li v-for="(ip, idx) in definitionToDelete.specific" :key="idx">
                {{ ip }}
              </li>
            </ul>
          </div>
          <div v-if="definitionToDelete.ipMatch && definitionToDelete.ipMatch.length > 0">
            <strong>IP Match:</strong>
            <ul>
              <li v-for="(match, idx) in definitionToDelete.ipMatch" :key="idx">
                {{ match }}
              </li>
            </ul>
          </div>
        </div>
      </div>
    </template>
  </ConfirmationDialog>
</template>

<script lang="ts" setup>
import { cloneDeep, debounce } from 'lodash'
import { FeatherTextBadge, BadgeTypes } from '@featherds/badge'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import IconAdd from '@featherds/icon/action/Add'
import IconDelete from '@featherds/icon/action/Delete'
import IconEdit from '@featherds/icon/action/Edit'
import IconSearch from '@featherds/icon/action/Search'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSortHeader, SORT } from '@featherds/table'

import useSnackbar from '@/composables/useSnackbar'
import { DEFAULT_MONITORING_LOCATION } from '@/lib/constants'
import { ActiveTabs, AdvancedSubtabs, SnmpConfigEditMode, useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { SnmpDefinition } from '@/types/snmpConfig'
import ConfirmationDialog from '../Common/ConfirmationDialog.vue'
import EmptyList from '../Common/EmptyList.vue'
import TableCard from '../Common/TableCard.vue'

const store = useSnmpConfigStore()
const snackbar = useSnackbar()
const currentPage = ref(1)
const pageSize = ref(50)
const showDeleteConfirmation = ref(false)
const definitionToDelete = ref<SnmpDefinition | null>(null)
const searchTerm = ref('')
const debouncedSearchTerm = ref('')

const emptyListContent = {
  msg: 'No results found.'
}

const columns = computed(() => [
  { id: 'location', label: 'Location' },
  { id: 'ipAddresses', label: 'IP Addresses' }
])

const sort = reactive({
  label: SORT.NONE,
  ipAddresses: SORT.NONE,
  location: SORT.NONE
}) as any

const createIpAddressLabel = (d: SnmpDefinition) => {
  const items: string[] = []

  // IP Range
  if (d.range?.length) {
    const ranges = d.range.map(r => `${r.begin} - ${r.end}`)
    items.push(...ranges)
  }

  // Specific IPs
  if (d.specific?.length) {
    items.push(...d.specific)
  }

  // IP Match
  if (d.ipMatch?.length) {
    const ipMatches = d.ipMatch.map(m => `IPLIKE: ${m}`)
    items.push(...ipMatches)
  }

  return items
}

const matchesSearchTerm = (def: SnmpDefinition, search: string) => {
  const lowerSearch = search.toLowerCase()

  // Check location
  if ((def.location ?? 'default').toLowerCase().includes(lowerSearch)) {
    return true
  }

  // Check range (begin and end)
  if (def.range?.some(r => 
    r.begin.toLowerCase().includes(lowerSearch) || 
    r.end.toLowerCase().includes(lowerSearch)
  )) {
    return true
  }

  // Check specific IPs
  if (def.specific?.some(ip => ip.toLowerCase().includes(lowerSearch))) {
    return true
  }

  // Check ipMatch
  if (def.ipMatch?.some(match => match.toLowerCase().includes(lowerSearch))) {
    return true
  }

  return false
}

const filteredDefinitions = computed<SnmpDefinition[]>(() => {
  if (!store.config.definition) {
    return []
  }

  if (!debouncedSearchTerm.value) {
    return store.config.definition
  }

  return store.config.definition.filter(def => matchesSearchTerm(def, debouncedSearchTerm.value))
})

const pageTotal = computed(() => filteredDefinitions.value.length)

const definitionsView = computed<SnmpDefinition[]>(() => {
  // Copy the filtered definitions array
  let items: SnmpDefinition[] = [...filteredDefinitions.value]

  // Sort by the active sort property
  const sortProperty = Object.keys(sort).find(key => sort[key] !== SORT.NONE)

  if (sortProperty) {
    const sortDirection = sort[sortProperty]

    items.sort((a, b) => {
      let aVal: string
      let bVal: string

      if (sortProperty === 'ipAddresses') {
        aVal = createIpAddressLabel(a).join(', ')
        bVal = createIpAddressLabel(b).join(', ')
      } else if (sortProperty === 'location') {
        aVal = a.location ?? ''
        bVal = b.location ?? ''
      } else {
        aVal = ''
        bVal = ''
      }

      const cmp = aVal.localeCompare(bVal)
      return sortDirection === SORT.ASCENDING ? cmp : -cmp
    })
  }

  // Paginate
  if (pageSize.value > 0) {
    const start = (currentPage.value - 1) * pageSize.value
    items = items.slice(start, start + pageSize.value)
  }

  return items
})

const sortChanged = (sortObj: { property: string; value: SORT }) => {
  for (const key of Object.keys(sort)) {
    sort[key] = SORT.NONE
  }

  sort[sortObj.property] = sortObj.value
}

const onCreateDefinition = () => {
  store.setDefinitionCreateEditMode(SnmpConfigEditMode.Create)
  store.resetCurrentDefinition()
  store.setActiveTab(ActiveTabs.BrowseDefinitions)
}

const onDefinitionEdit = (definition?: SnmpDefinition) => {
  if (definition) {
    store.setCurrentDefinition(cloneDeep(definition))
    store.setDefinitionCreateEditMode(SnmpConfigEditMode.Edit)
  }
}

const onDefinitionDelete = (definition?: SnmpDefinition) => {
  if (definition) {
    definitionToDelete.value = cloneDeep(definition)
    showDeleteConfirmation.value = true
  }
}

const confirmDelete = async () => {
  let success = false

  if (definitionToDelete.value) {
    const result = await store.removeDefinition(
      definitionToDelete.value.range ?? null,
      definitionToDelete.value.specific ?? null,
      definitionToDelete.value.ipMatch ?? null,
      definitionToDelete.value.location ?? DEFAULT_MONITORING_LOCATION
    )

    success = result.success

    if (!result.success) {
      snackbar.showSnackBar({
        msg: 'Failed to delete definition: ' + result.message,
        error: true
      })
    } else {
      snackbar.showSnackBar({
        msg: 'Definition deleted successfully'
      })
    }
  }

  showDeleteConfirmation.value = false
  definitionToDelete.value = null

  if (success) {
    await store.populateSnmpConfig()
  }
}

const cancelDelete = () => {
  showDeleteConfirmation.value = false
  definitionToDelete.value = null
}

const updateDebouncedSearchTerm = debounce((value: string) => {
  debouncedSearchTerm.value = value
  currentPage.value = 1 // Reset to first page when searching
}, 200)

const onSearchChange = (value: string | number | undefined) => {
  updateDebouncedSearchTerm(String(value ?? ''))
}
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table' as table;
@use '@/styles/_transitionDataTable';

.snmp-config-definitions-table {
  margin-top: 0;
  padding: 0;

  .header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 0;

    .action-container {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 5px;
      width: 100%;

      .search-container {
        flex: 0 0 auto;
        min-width: 30em;
      }
    }
  }

  .container {
    table {
      width: 100%;
      @include table.table;
      @include table.table-condensed;

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

        .ip-address-badge-wrapper {
          text-wrap: auto;
          padding: 2px;
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

  button.btn.btn-icon .add-definition-icon {
    font-size: 1.1rem;
  }
}

.confirmation-message {
  .definition-details {
    margin-top: 15px;
    padding: 10px;
    background-color: var(variables.$surface);
    border-radius: 4px;

    p {
      margin: 5px 0;
    }

    ul {
      margin: 5px 0;
      padding-left: 20px;

      li {
        margin: 3px 0;
      }
    }
  }
}
</style>
