<template>
  <TableCard class="snmp-data-collection-profiles-table">
    <div class="header">
      <div class="section-left">
        <div class="search-container">
          <FormField>
            <IconField>
              <InputText
                :id="searchId"
                :modelValue="store.profilesSearchTerm"
                @update:modelValue="onChangeSearchTerm"
                data-test="search-input"
                placeholder="Search by Profile Name"
                :aria-label="'Search by Profile Name'"
              />
              <InputIcon>
                <FeatherIcon :icon="Search" />
              </InputIcon>
            </IconField>
          </FormField>
        </div>
      </div>
      <div class="section-right">
        <div class="add">
          <Button
            outlined
            label="Create New Data Collection Profile"
            data-test="create-profile-button"
            @click="goToCreateProfile"
          />
        </div>
      </div>
    </div>

    <DataTable
      v-if="searchedProfiles.length"
      v-model:first="firstRow"
      :value="searchedProfiles"
      paginator
      dataKey="id"
      :rows="10"
      :rowsPerPageOptions="[10, 20, 50, 100, 200]"
      class="data-table"
      data-test="profiles-table"
    >
      <Column
        field="name"
        header="Profile Name"
        sortable
      />
      <Column
        field="enabled"
        header="Status"
        sortable
      >
        <template #body="{ data }">
          <Tag
            :class="data.enabled ? 'enabled-tag' : 'disabled-tag'"
            :value="data.enabled ? 'Enabled' : 'Disabled'"
            data-test="status-tag"
          />
        </template>
      </Column>
      <Column header="Actions">
        <template #body="{ data }">
          <div class="action-container">
            <Button
              text
              :title="`View ${data.name}`"
              data-test="view-button"
              @click="onProfileClick(data)"
            >
              <FeatherIcon :icon="ViewDetails" />
            </Button>
            <Button
              text
              aria-haspopup="true"
              aria-controls="profile-row-menu"
              :title="`More actions for ${data.name}`"
              data-test="row-menu-button"
              @click="toggleRowMenu($event, data)"
            >
              <FeatherIcon :icon="MenuIcon" />
            </Button>
          </div>
        </template>
      </Column>
    </DataTable>

    <Menu
      id="profile-row-menu"
      ref="rowMenu"
      :model="rowMenuItems"
      popup
    />

    <div v-if="!searchedProfiles.length">
      <EmptyList
        :content="emptyListContent"
        data-test="empty-list"
      />
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
import { computed, onMounted, ref, useId } from 'vue'

import { FeatherIcon } from '@featherds/icon'
import MenuIcon from '@featherds/icon/navigation/MoreHoriz'
import Search from '@featherds/icon/action/Search'
import ViewDetails from '@featherds/icon/action/ViewDetails'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import InputText from 'primevue/inputtext'
import type { MenuItem } from 'primevue/menuitem'
import Menu from 'primevue/menu'
import Tag from 'primevue/tag'
import { updateDataCollectionProfile } from '@/services/snmpDataCollectionService'
import useSnackbar from '@/composables/useSnackbar'
import { useRouter } from 'vue-router'

import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { SnmpCollectionProfile } from '@/types/snmpDataCollection'
import ConfirmationDialog from '../Common/ConfirmationDialog.vue'
import FormField from '@/components/Common/FormField.vue'
import EmptyList from '../Common/EmptyList.vue'
import TableCard from '../Common/TableCard.vue'

const router = useRouter()
const store = useSnmpDataCollectionStore()
const snackbar = useSnackbar()
const searchId = useId()

const showDeleteConfirmation = ref(false)
const profileToDelete = ref<SnmpCollectionProfile | null>(null)
// First-row index of the client-side paginator; reset on search so a query
// that shrinks the result set can't leave the user stranded on an empty page.
const firstRow = ref(0)

const emptyListContent = {
  msg: 'No results found.'
}

// Profiles are fetched in full; DataTable handles sorting + pagination
// client-side, so this component only filters by the search term.
const searchedProfiles = computed(() => {
  const term = store.profilesSearchTerm.trim().toLowerCase()
  if (!term) {
    return store.profiles
  }
  return store.profiles.filter(p => p.name.toLowerCase().includes(term))
})

const rowMenu = ref()
const rowMenuTarget = ref<SnmpCollectionProfile | null>(null)
const rowMenuItems = computed<MenuItem[]>(() => {
  const target = rowMenuTarget.value
  if (!target) {
    return []
  }
  return [
    {
      label: target.enabled ? 'Disable Profile' : 'Enable Profile',
      command: () => onChangeProfileStatus(target)
    },
    {
      label: 'Delete Profile',
      command: () => openDeleteCollectionProfileDialog(target)
    }
  ]
})

const toggleRowMenu = (event: Event, profile: SnmpCollectionProfile) => {
  rowMenuTarget.value = profile
  rowMenu.value?.toggle(event)
}

const onChangeSearchTerm = (value: string | undefined) => {
  firstRow.value = 0
  store.onChangeProfilesSearchTerm(value ?? '')
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
.snmp-data-collection-profiles-table {
  margin-top: 10px;
  padding: 25px;
  border: 1px solid var(--p-content-border-color);

  .header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;

    .section-left {
      display: flex;
      gap: 10px;

      .search-container {
        width: 400px;
      }
    }
  }

  .enabled-tag {
    border-radius: 4px;
    background-color: #0B720C1F;

    :deep(.p-tag-label) {
      color: #0B720C !important;
    }
  }

  .disabled-tag {
    border-radius: 4px;
    background-color: #7575751F;

    :deep(.p-tag-label) {
      color: #757575 !important;
    }
  }

  .action-container {
    display: flex;
    align-items: center;
    gap: 5px;
  }
}
</style>
