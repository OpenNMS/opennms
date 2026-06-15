<template>
  <TableCard class="snmp-data-collection-profiles-table">
    <div class="header">
      <div class="section-left">
        <div class="search-container">
          <FeatherInput
            label="Search"
            type="search"
            data-test="search-input"
            v-model.trim="store.profilesSearchTerm"
            :hint="'Search by Profile Name'"
            @update:modelValue.self="((e: string) => onChangeSearchTerm(e))"
          >
            <template #pre>
              <FeatherIcon :icon="Search" />
            </template>
          </FeatherInput>
        </div>
      </div>
      <div class="section-right">
        <div class="add">
          <FeatherButton
            secondary
            @click="goToCreateProfile"
          >
            Create New Data Collection Profile
          </FeatherButton>
        </div>
      </div>
    </div>
    <div class="container">
      <table
        class="data-table"
        aria-label="Profiles Table"
        v-if="filteredSortedProfiles.length"
      >
        <thead>
          <tr>
            <FeatherSortHeader
              scope="col"
              property="name"
              :sort="(sort as any).name"
              v-on:sort-changed="sortChanged"
            >
              Profile Name
            </FeatherSortHeader>
            <FeatherSortHeader
              scope="col"
              property="enabled"
              :sort="(sort as any).enabled"
              v-on:sort-changed="sortChanged"
            >
              Status
            </FeatherSortHeader>
            <th>Actions</th>
          </tr>
        </thead>
        <TransitionGroup
          name="data-table"
          tag="tbody"
        >
          <tr
            v-for="profile in sortedFilteredProfiles"
            :key="profile.id"
          >
            <td>{{ profile.name }}</td>
            <td>
              <div class="tag">
                <FeatherChip
                  :class="profile.enabled ? 'enabled-tag' : 'disabled-tag'"
                  data-test="status-tag"
                >
                  {{ profile.enabled ? 'Enabled' : 'Disabled' }}
                </FeatherChip>
              </div>
            </td>
            <td>
              <div class="action-container">
                <FeatherButton
                  icon="View Details"
                  data-test="view-button"
                  @click="onProfileClick(profile)"
                >
                  <FeatherIcon :icon="ViewDetails"> </FeatherIcon>
                </FeatherButton>
                <FeatherDropdown>
                  <template v-slot:trigger="{ attrs, on }">
                    <FeatherButton
                      link
                      href="#"
                      v-bind="attrs"
                      v-on="on"
                      :icon="`More actions for ${profile.name}`"
                    >
                      <FeatherIcon :icon="MenuIcon" />
                    </FeatherButton>
                  </template>
                  <FeatherDropdownItem
                    data-test="change-status-profile-button"
                    @click="onChangeProfileStatus(profile)"
                  >
                    {{ profile.enabled ? 'Disable' : 'Enable' }} Profile
                  </FeatherDropdownItem>
                  <FeatherDropdownItem
                    data-test="delete-profile-button"
                    @click="openDeleteCollectionProfileDialog(profile)"
                  >
                    Delete Profile
                  </FeatherDropdownItem>
                </FeatherDropdown>
              </div>
            </td>
          </tr>
        </TransitionGroup>
      </table>
      <div
        class="alerts-pagination"
        v-if="filteredSortedProfiles.length"
      >
        <FeatherPagination
          :modelValue="store.profilesPagination.page"
          :pageSize="store.profilesPagination.pageSize"
          :total="filteredSortedProfiles.length"
          :pageSizes="[10, 20, 50, 100, 200]"
          @update:modelValue="store.onProfilePageChange"
          @update:pageSize="store.onProfilePageSizeChange"
          data-test="FeatherPagination"
        />
      </div>
      <div v-if="!filteredSortedProfiles.length">
        <EmptyList
          :content="emptyListContent"
          data-test="empty-list"
        />
      </div>
    </div>
  </TableCard>
  <ConfirmationDialog
    :visible="showDeleteConfirmation"
    title="Delete Profile"
    actionButtonText="Delete"
    @ok="confirmDelete"
    @cancel="cancelDelete"
  >
    <template #content>
      <p>Are you sure you want to delete the profile <strong>{{ profileToDelete?.name }}</strong>? This action cannot be undone.</p>
    </template>
  </ConfirmationDialog>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'

import { FeatherButton } from '@featherds/button'
import { FeatherChip } from '@featherds/chips'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherIcon } from '@featherds/icon'
import MenuIcon from '@featherds/icon/navigation/MoreHoriz'
import Search from '@featherds/icon/action/Search'
import ViewDetails from '@featherds/icon/action/ViewDetails'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { updateDataCollectionProfile } from '@/services/snmpDataCollectionService'
import useSnackbar from '@/composables/useSnackbar'
import { useRouter } from 'vue-router'

const router = useRouter()
import { FeatherSortHeader, SORT } from '@featherds/table'

import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { SnmpCollectionProfile } from '@/types/snmpDataCollection'
import ConfirmationDialog from '../Common/ConfirmationDialog.vue'
import EmptyList from '../Common/EmptyList.vue'
import TableCard from '../Common/TableCard.vue'

const store = useSnmpDataCollectionStore()
const snackbar = useSnackbar()

const showDeleteConfirmation = ref(false)
const profileToDelete = ref<SnmpCollectionProfile | null>(null)

const emptyListContent = {
  msg: 'No results found.'
}

const sort = reactive({
  name: SORT.NONE,
  enabled: SORT.NONE
}) as any

const filteredSortedProfiles = computed(() => {
  let list = [...store.profiles]

  const term = store.profilesSearchTerm.trim().toLowerCase()
  if (term) {
    list = list.filter(p => p.name.toLowerCase().includes(term))
  }

  const { sortKey, sortOrder } = store.profilesSorting
  if (sortKey === 'name') {
    list.sort((a, b) => {
      const cmp = a.name.localeCompare(b.name)
      return sortOrder === 'asc' ? cmp : -cmp
    })
  } else if (sortKey === 'enabled') {
    list.sort((a, b) => {
      const cmp = (a.enabled ? 1 : 0) - (b.enabled ? 1 : 0)
      return sortOrder === 'asc' ? cmp : -cmp
    })
  }

  return list
})

const sortedFilteredProfiles = computed(() => {
  const { page, pageSize } = store.profilesPagination
  const start = (page - 1) * pageSize
  return filteredSortedProfiles.value.slice(start, start + pageSize)
})

const sortChanged = (sortObj: { property: string; value: SORT }) => {
  if (sortObj.value === 'asc' || sortObj.value === 'desc') {
    store.onProfilesSortChange(sortObj.property, sortObj.value)
  } else {
    store.onProfilesSortChange('createdTime', 'desc')
  }

  for (const prop in sort) {
    sort[prop] = SORT.NONE
  }
  sort[sortObj.property] = sortObj.value
}

const onChangeSearchTerm = (value: string) => {
  store.onChangeProfilesSearchTerm(value)
}

const goToCreateProfile = () => {
  router.push({ name: 'SNMP Data Collection Profile Detail', params: { id: 'create' }})
}

const onProfileClick = (profile: SnmpCollectionProfile) => {
  router.push({ name: 'SNMP Data Collection Profile Detail', params: { id: profile.id }})
}

const onChangeProfileStatus = async (profile: SnmpCollectionProfile) => {
  const updatedProfile = {
    ...profile,
    enabled: !profile.enabled
  }

  const success = await updateDataCollectionProfile(updatedProfile)

  if (success) {
    await store.fetchSnmpCollectionProfiles()
    snackbar.showSnackBar({
      msg: `Profile '${profile.name}' updated successfully.`
    })
  } else {
    snackbar.showSnackBar({
      msg: `Failed to update profile '${profile.name}'.`,
      error: true
    })
  }
}

const openDeleteCollectionProfileDialog = (profile: SnmpCollectionProfile) => {
  profileToDelete.value = profile
  showDeleteConfirmation.value = true
}

const confirmDelete = async () => {
  showDeleteConfirmation.value = false
  const profile = profileToDelete.value
  if (!profile) {
    return
  }

  const success = await store.removeSnmpCollectionProfiles([profile.id])

  if (success) {
    await store.fetchSnmpCollectionProfiles()
    snackbar.showSnackBar({ msg: `Profile '${profile.name}' deleted successfully.` })
  } else {
    snackbar.showSnackBar({ msg: `Failed to delete profile '${profile.name}'.`, error: true })
  }

  profileToDelete.value = null
}

const cancelDelete = () => {
  showDeleteConfirmation.value = false
  profileToDelete.value = null
}

onMounted(() => {
  store.fetchSnmpCollectionProfiles()
})
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';

.snmp-data-collection-profiles-table {
  margin-top: 10px;
  padding: 25px;
  border: 1px solid var(--feather-border-on-surface);

  .header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;

    .section-left {
      display: flex;
      gap: 10px;

      .search-container {
        width: 400px;

        :deep(.feather-input-sub-text) {
          display: none !important;
        }
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

        .profile-chips {
          display: flex;
          flex-wrap: wrap;
          gap: 4px;
          align-items: center;

          .profile-tag {
            margin: 0 !important;
            border-radius: 4px;
          }

          .empty-profiles {
            color: var(--feather-secondary-text-on-surface);
          }
        }

        .tag {
          .enabled-tag {
            margin: 0 !important;
            border-radius: 4px;
            background-color: #0B720C1F;

            :deep(span) {
              color: #0B720C !important;
            }
          }

          .disabled-tag {
            margin: 0 !important;
            border-radius: 4px;
            background-color: #7575751F;

            :deep(span) {
              color: #757575 !important;
            }
          }
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

    .alerts-pagination {
      display: flex;
      justify-content: center;
      padding: 30px 0px 0px 0px;
    }

    .feather-pagination {
      border: none !important;
    }
  }
}
</style>
