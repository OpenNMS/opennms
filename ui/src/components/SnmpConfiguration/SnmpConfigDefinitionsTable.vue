<template>
  <TableCard class="snmp-config-definitions-table">
    <div class="header">
      <div class="header-content-container">
        <div class="search-container">
          <FormField class="search-field">
            <IconField>
              <PInputText
                id="snmp-definitions-search"
                placeholder="Search IP addresses or location"
                aria-label="Search IP addresses or location"
                v-model="searchTerm"
                @update:modelValue="(val) => onSearchChange(val as string)"
              />
              <InputIcon>
                <FeatherIcon :icon="IconSearch" />
              </InputIcon>
            </IconField>
          </FormField>
        </div>
        <div class="refresh">
          <PButton
            data-test="new-definition-button"
            @click="onCreateDefinition"
          >
            <FeatherIcon :icon="IconAdd" aria-hidden="true" focusable="false" class="add-definition-icon" />
            New Definition
          </PButton>
        </div>
      </div>
    </div>
    <div class="table-container">
      <PDataTable
        :value="definitionRows"
        paginator
        :rows="50"
        :rowsPerPageOptions="[20, 50, 100, 200]"
        v-model:first="firstRow"
        aria-label="SNMP Config Definition Table"
      >
        <PColumn
          field="location"
          header="Location"
          sortable
        />
        <PColumn
          field="ipSortKey"
          header="IP Addresses"
          sortable
        >
          <template #body="{ data }">
            <div
              v-if="data.ipLabels.length"
              class="ip-address-badge-wrapper"
            >
              <PTag
                v-for="ipAddr of data.ipLabels"
                :key="ipAddr"
                :value="ipAddr"
                severity="info"
              />
            </div>
            <span v-else>--</span>
          </template>
        </PColumn>
        <PColumn header="Actions">
          <template #body="{ data }">
            <div class="action-container">
              <PButton
                text
                aria-label="Edit"
                data-test="edit-button"
                @click="onDefinitionEdit(data.original)"
              >
                <FeatherIcon :icon="IconEdit" />
              </PButton>
              <PButton
                v-if="data.original.id !== 0"
                text
                aria-label="Delete"
                data-test="delete-button"
                @click="onDefinitionDelete(data.original)"
              >
                <FeatherIcon :icon="IconDelete" />
              </PButton>
            </div>
          </template>
        </PColumn>
        <template #empty>
          <EmptyList
            :content="emptyListContent"
            data-test="empty-list"
          />
        </template>
      </PDataTable>
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
import { computed, ref } from 'vue'

import { cloneDeep, debounce } from 'lodash'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import InputText from 'primevue/inputtext'
import Tag from 'primevue/tag'
import { FeatherIcon } from '@featherds/icon'
import IconAdd from '@featherds/icon/action/Add'
import IconDelete from '@featherds/icon/action/Delete'
import IconEdit from '@featherds/icon/action/Edit'
import IconSearch from '@featherds/icon/action/Search'

import useSnackbar from '@/composables/useSnackbar'
import { DEFAULT_MONITORING_LOCATION } from '@/lib/constants'
import { ActiveTabs, SnmpConfigEditMode, useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { SnmpDefinition } from '@/types/snmpConfig'
import ConfirmationDialog from '../Common/ConfirmationDialog.vue'
import EmptyList from '../Common/EmptyList.vue'
import FormField from '@/components/Common/FormField.vue'
import TableCard from '../Common/TableCard.vue'

const PButton = Button
const PColumn = Column
const PDataTable = DataTable
const PInputText = InputText
const PTag = Tag

const store = useSnmpConfigStore()
const snackbar = useSnackbar()
const firstRow = ref(0)
const showDeleteConfirmation = ref(false)
const definitionToDelete = ref<SnmpDefinition | null>(null)
const searchTerm = ref('')
const debouncedSearchTerm = ref('')

const emptyListContent = {
  msg: 'No results found.'
}

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

// Rows for the DataTable: includes display labels plus a sortable IP string
// and a reference back to the original definition for edit/delete actions.
// DataTable handles sorting (location / ipSortKey) and pagination client-side.
const definitionRows = computed(() => filteredDefinitions.value.map(definition => ({
  original: definition,
  location: definition.location ?? DEFAULT_MONITORING_LOCATION,
  ipLabels: createIpAddressLabel(definition),
  ipSortKey: createIpAddressLabel(definition).join(', ')
})))

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
  firstRow.value = 0 // Reset to first page when searching
}, 200)

const onSearchChange = (value: string | number | undefined) => {
  updateDebouncedSearchTerm(String(value ?? ''))
}
</script>

<style lang="scss" scoped>
.snmp-config-definitions-table {
  margin-top: 0;
  padding: 0;

  .header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 1rem;

    .header-content-container {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 5px;
      width: 100%;

      .search-container {
        flex: 0 0 auto;
        min-width: 30em;

        .search-field {
          width: 100%;

          // make the input (and its IconField wrapper) fill the field so the
          // search icon sits at the input's right edge rather than floating far
          // out in the container
          :deep(.p-iconfield) {
            display: block;
            width: 100%;
          }

          :deep(.p-inputtext) {
            width: 100%;
            padding-right: 2.75rem;
          }

          // enlarge the search glyph (FeatherIcon scales with font-size) and
          // keep it near the right edge, vertically centered
          :deep(.p-inputicon) {
            font-size: 1.75rem;
            right: 0.625rem;
            margin-top: -0.875rem;
          }
        }
      }
    }
  }

  .table-container {
    .ip-address-badge-wrapper {
      display: flex;
      flex-wrap: wrap;
      gap: 4px;
    }

    .action-container {
      display: flex;
      align-items: center;
      gap: 5px;

      // enlarge the edit/delete icons (FeatherIcon scales with font-size)
      :deep(.p-button) {
        font-size: 1.3rem;
      }
    }
  }

  .add-definition-icon {
    font-size: 1.1rem;
    margin-right: 0.4em;
  }
}

.confirmation-message {
  .definition-details {
    margin-top: 15px;
    padding: 10px;
    background-color: var(--p-content-background);
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
