<template>
  <TableCard class="snmp-config-profiles-table">
    <div class="header">
      <div class="action-container">
        <div class="search-container">
          <FeatherInput
            v-model="searchTerm"
            @update:modelValue="onSearchChange"
            label="Search label or filter"
          >
            <template #pre>
              <FeatherIcon :icon="IconSearch" />
            </template>
          </FeatherInput>
        </div>
        <div class="refresh">
          <FeatherButton
            primary
            @click="onCreateProfile"
          >
            <template v-slot:icon>
              <FeatherIcon :icon="IconAdd" aria-hidden="true" focusable="false" class="add-profile-icon" />
              New Profile
            </template>
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
              :key="col.id"
              scope="col"
              :property="col.id"
              :sort="(sortStates as any)[col.id]"
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
            :key="profile.label"
          >
            <td>{{ profile.label }}</td>
            <td>{{ profile.filter }}</td>
            <td>
              <div class="action-container">
                <FeatherButton
                  icon="Edit"
                  data-test="edit-button"
                  @click="onProfileEdit(profile.label)"
                >
                  <FeatherIcon :icon="IconEdit"> </FeatherIcon>
                </FeatherButton>
                <FeatherButton
                  icon="Delete"
                  data-test="delete-button"
                  @click="onConfirmProfileDelete(profile.label)"
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
      <div v-if="!profiles.length">
        <EmptyList
          :content="emptyListContent"
          data-test="empty-list"
        />
      </div>
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
import { debounce } from 'lodash'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import IconAdd from '@featherds/icon/action/Add'
import IconDelete from '@featherds/icon/action/Delete'
import IconEdit from '@featherds/icon/action/Edit'
import IconSearch from '@featherds/icon/action/Search'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSortHeader, SORT } from '@featherds/table'
import ConfirmationDialog from '../Common/ConfirmationDialog.vue'
import EmptyList from '../Common/EmptyList.vue'
import TableCard from '../Common/TableCard.vue'

import { useSnmpConfigStore, ActiveTabs, AdvancedSubtabs, SnmpConfigEditMode } from '@/stores/snmpConfigStore'
import { sortPredicate } from '@/lib/sorting'
import { FeatherSortObject } from '@/types'
import { SnmpProfile } from '@/types/snmpConfig'

const emit = defineEmits<{
  (e: 'delete-profile', label: string): void
}>()

const store = useSnmpConfigStore()
const displayDeleteDialog = ref(false)
const selectedProfileLabel = ref<string | null>(null)
const searchTerm = ref('')
const debouncedSearchTerm = ref('')
const currentPage = ref(1)
const pageSize = ref(50)

const deleteDialogLabels = {
  title: 'Delete SNMP Configuration Profile'
}

const emptyListContent = {
  msg: 'No profiles found.'
}

const columns = computed(() => [
  { id: 'label', label: 'Label' },
  { id: 'filter', label: 'Filter Expression' }
])

const currentSort = ref<FeatherSortObject>({ property: 'label', value: SORT.NONE })

const sortStates: Record<string, SORT> = reactive({
  label: SORT.NONE,
  filter: SORT.NONE
})

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

const pageTotal = computed(() => filteredProfiles.value.length)

const profiles = computed(() => {
  const items = filteredProfiles.value.map(profile => {
    return {
      label: profile.label ?? '--',
      filter: createFilterExpressionLabel(profile)
    }
  }).sort((a, b) => sortPredicate(a, b, currentSort.value))

  if (pageSize.value > 0) {
    const start = (currentPage.value - 1) * pageSize.value
    return items.slice(start, start + pageSize.value)
  }

  return items
})

const sortChanged = (sortObj: FeatherSortObject) => {
  for (const key in sortStates) {
    sortStates[key] = SORT.NONE
  }

  sortStates[sortObj.property] = sortObj.value
  currentSort.value = sortObj
}

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
  currentPage.value = 1 // Reset to first page when searching
}, 200)

const onSearchChange = (value: string | number | undefined) => {
  updateDebouncedSearchTerm(String(value ?? ''))
}
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';

.snmp-config-profiles-table {
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

    button.btn.btn-icon .add-profile-icon {
      font-size: 1.1rem;
    }
  }
}
</style>
