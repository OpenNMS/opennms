<template>
  <TableCard class="snmp-config-profiles-table">
    <div class="header">
      <div class="action-container">
        <div class="search-container">
          <FormField class="search-field">
            <IconField>
              <PInputText
                id="snmp-profiles-search"
                placeholder="Search label or filter"
                aria-label="Search label or filter"
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
            data-test="new-profile-button"
            @click="onCreateProfile"
          >
            <FeatherIcon :icon="IconAdd" aria-hidden="true" focusable="false" class="add-profile-icon" />
            New Profile
          </PButton>
        </div>
      </div>
    </div>
    <div class="container">
      <PDataTable
        :value="profileRows"
        paginator
        :rows="50"
        :rowsPerPageOptions="[20, 50, 100, 200]"
        v-model:first="firstRow"
        aria-label="SNMP Config Profile Table"
      >
        <PColumn
          field="label"
          header="Label"
          sortable
        />
        <PColumn
          field="filter"
          header="Filter Expression"
          sortable
        />
        <PColumn header="Actions">
          <template #body="{ data }">
            <div class="action-container">
              <PButton
                text
                aria-label="Edit"
                data-test="edit-button"
                @click="onProfileEdit(data.label)"
              >
                <FeatherIcon :icon="IconEdit" />
              </PButton>
              <PButton
                text
                aria-label="Delete"
                data-test="delete-button"
                @click="onConfirmProfileDelete(data.label)"
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
    :visible="displayDeleteDialog"
    title="Delete SNMP Configuration Profile"
    action-button-text="Delete"
    @cancel="onCancelProfileDelete"
    @ok="onProfileDelete"
  >
    <template v-slot:content>
      <p>
        Do you want to delete the SNMP configuration profile:
        <strong>{{ selectedProfileLabel }}</strong>
      </p>
    </template>
  </ConfirmationDialog>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue'

import { debounce } from 'lodash'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import InputText from 'primevue/inputtext'
import { FeatherIcon } from '@featherds/icon'
import IconAdd from '@featherds/icon/action/Add'
import IconDelete from '@featherds/icon/action/Delete'
import IconEdit from '@featherds/icon/action/Edit'
import IconSearch from '@featherds/icon/action/Search'
import ConfirmationDialog from '../Common/ConfirmationDialog.vue'
import EmptyList from '../Common/EmptyList.vue'
import FormField from '@/components/Common/FormField.vue'
import TableCard from '../Common/TableCard.vue'

import { useSnmpConfigStore, ActiveTabs, AdvancedSubtabs, SnmpConfigEditMode } from '@/stores/snmpConfigStore'
import { SnmpProfile } from '@/types/snmpConfig'

const PButton = Button
const PColumn = Column
const PDataTable = DataTable
const PInputText = InputText

const emit = defineEmits<{
  (e: 'delete-profile', label: string): void
}>()

const store = useSnmpConfigStore()
const displayDeleteDialog = ref(false)
const selectedProfileLabel = ref<string | null>(null)
const searchTerm = ref('')
const debouncedSearchTerm = ref('')
const firstRow = ref(0)

const emptyListContent = {
  msg: 'No profiles found.'
}

const createFilterExpressionLabel = (profile: SnmpProfile) => {
  return profile.filter ?? '--'
}

const matchesSearchTerm = (profile: SnmpProfile, search: string) => {
  const lowerSearch = search.toLowerCase()

  // Check label
  if (profile.label?.toLowerCase().includes(lowerSearch)) {
    return true
  }

  // Check filter
  if (profile.filter?.toLowerCase().includes(lowerSearch)) {
    return true
  }

  return false
}

const filteredProfiles = computed<SnmpProfile[]>(() => {
  if (!store.config.profiles?.profile) {
    return []
  }

  if (!debouncedSearchTerm.value) {
    return store.config.profiles.profile
  }

  return store.config.profiles.profile.filter(profile =>
    matchesSearchTerm(profile, debouncedSearchTerm.value)
  )
})

// Rows for the DataTable; DataTable handles sort (label / filter) and
// pagination client-side.
const profileRows = computed(() => filteredProfiles.value.map(profile => ({
  label: profile.label ?? '--',
  filter: createFilterExpressionLabel(profile)
})))

const onConfirmProfileDelete = (label: string) => {
  selectedProfileLabel.value = label
  displayDeleteDialog.value = true
}

const onCancelProfileDelete = () => {
  displayDeleteDialog.value = false
  selectedProfileLabel.value = null
}

const onProfileDelete = () => {
  if (!selectedProfileLabel.value) {
    return
  }

  const label = selectedProfileLabel.value
  displayDeleteDialog.value = false
  selectedProfileLabel.value = null

  emit('delete-profile', label)
}

const onProfileEdit = (label: string) => {
  store.setProfileLabel(label)
  store.setSnmpProfileEditMode(SnmpConfigEditMode.Edit)
}

const onCreateProfile = () => {
  store.setSnmpProfileEditMode(SnmpConfigEditMode.Create)
  store.setActiveTab(ActiveTabs.Advanced)
  store.setActiveAdvancedSubtab(AdvancedSubtabs.Profiles)
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
.snmp-config-profiles-table {
  margin-top: 0;
  padding: 0;

  .header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 1rem;

    .action-container {
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

  .container {
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

  .add-profile-icon {
    font-size: 1.1rem;
    margin-right: 0.4em;
  }
}
</style>
