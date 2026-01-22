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
            :key="`${profile.label ?? ''}-${profile.label}`"
          >
            <td>{{ profile.label }}</td>
            <td>{{ profile.filterExpression }}</td>
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
      <div v-if="!profiles.length">
        <EmptyList
          :content="emptyListContent"
          data-test="empty-list"
        />
      </div>
    </div>
  </TableCard>

  <FeatherDialog
    v-model="displayDeleteDialog"
    :labels="deleteDialogLabels"
    hide-close
  >
    <div class="modal-body">
      <p>
        Do you want to delete the SNMP configuration profile:
        <strong>{{ selectedProfileLabel }}</strong>
      </p>
    </div>
    <template v-slot:footer>
      <FeatherButton @click="onCancelProfileDelete"> Cancel </FeatherButton>
      <FeatherButton
        primary
        @click="onProfileDelete"
      >
        Delete
      </FeatherButton>
    </template>
  </FeatherDialog>
</template>

<script lang="ts" setup>
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'
import { FeatherIcon } from '@featherds/icon'
import IconDelete from '@featherds/icon/action/Delete'
import IconEdit from '@featherds/icon/action/Edit'
import { FeatherSortHeader, SORT } from '@featherds/table'
import EmptyList from '../Common/EmptyList.vue'
import TableCard from '../Common/TableCard.vue'

import { SnmpConfigEditMode, useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { sortPredicate } from '@/lib/sorting'
import { FeatherSortObject } from '@/types'
import { SnmpProfile } from '@/types/snmpConfig'

const emit = defineEmits<{
  (e: 'delete-profile', label: string): void
}>()

const store = useSnmpConfigStore()
const displayDeleteDialog = ref(false)
const selectedProfileLabel = ref<string | null>(null)

const deleteDialogLabels = {
  title: 'Delete SNMP Configuration Profile'
}

const emptyListContent = {
  msg: 'No profiles found.'
}

const columns = computed(() => [
  { id: 'label', label: 'Label' },
  { id: 'filterExpression', label: 'Filter Expression' }
])

const currentSort = ref<FeatherSortObject>({ property: 'label', value: SORT.NONE })

const sortStates: Record<string, SORT> = reactive({
  label: SORT.NONE,
  filterExpression: SORT.NONE
})

const createFilterExpressionLabel = (profile: SnmpProfile) => {
  return profile.filterExpression ?? '--'
}

const profiles = computed(() => {
  if (!store.config.snmpProfiles?.snmpProfiles) {
    return []
  }

  const items = store.config.snmpProfiles.snmpProfiles.map(profile => {
    return {
      label: profile.label ?? '--',
      filterExpression: createFilterExpressionLabel(profile)
    }
  }).sort((a, b) => sortPredicate(a, b, currentSort.value))

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
