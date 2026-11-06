<template>
  <TableCard class="oncall-roles-table">
    <div class="header">
      <div class="card-title">On-Call Roles</div>
      <div class="header-actions">
        <OnmsButton
          variant="outlined"
          label="Add New On-Call Role"
          icon="pi pi-plus"
          data-test="add-oncall-role-button"
          @click="openEditor(null)"
        />
        <AboutDialogButton title="On-Call Roles">
          <OnCallRolesAbout />
        </AboutDialogButton>
      </div>
    </div>

    <OnmsSearchInput
      v-if="store.roles.length"
      v-model="filter"
      class="roles-filter"
      placeholder="Filter on-call roles"
      aria-label="Filter on-call roles"
      data-test="oncall-roles-filter"
    />

    <OnmsTable
      v-if="store.roles.length"
      v-model:first="first"
      :value="filteredRoles"
      paginator
      dataKey="name"
      :rows="10"
      :rowsPerPageOptions="[10, 20, 50, 100]"
      class="data-table"
      data-test="oncall-roles-table"
    >
      <OnmsColumn
        field="name"
        header="Name"
        sortable
      >
        <template #body="{ data }">
          <span class="role-name">{{ data.name }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn
        field="supervisor"
        header="Supervisor"
        sortable
      >
        <template #body="{ data }">
          <span v-onms-tooltip.top="store.fullName(data.supervisor) || data.supervisor">{{ data.supervisor }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="On Call Now">
        <template #body="{ data }">
          <span class="on-call-now">{{ (data.onCallNow ?? []).join(', ') || '--' }}</span>
          <OnmsTag
            v-if="data.onCallNowIsSupervisor"
            value="fallback"
            severity="secondary"
            class="fallback-tag"
            v-onms-tooltip.top="'Nobody is scheduled: notices go to the supervisor'"
          />
        </template>
      </OnmsColumn>
      <OnmsColumn
        field="membershipGroup"
        header="Membership Group"
        sortable
      />
      <OnmsColumn
        field="description"
        header="Description"
        sortable
      >
        <template #body="{ data }">
          <span
            class="description"
            v-onms-tooltip.top="data.description || ''"
          >{{ data.description }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <div class="action-container">
            <OnmsIconButton
              :icon="Edit"
              :title="`Edit ${data.name}`"
              :aria-label="`Edit ${data.name}`"
              data-test="edit-oncall-role-button"
              @click="openEditor(data)"
            />
            <OnmsIconButton
              :icon="Delete"
              severity="danger"
              :title="`Delete ${data.name}`"
              :aria-label="`Delete ${data.name}`"
              data-test="delete-oncall-role-button"
              @click="askDelete(data)"
            />
            <OnmsIconButton
              :icon="MoreVert"
              title="More actions"
              :aria-label="`More actions for ${data.name}`"
              data-test="oncall-role-actions-menu-button"
              @click="toggleRowMenu($event, data)"
            />
          </div>
        </template>
      </OnmsColumn>
      <template #empty>
        <EmptyList
          :content="noMatchContent"
          data-test="no-match"
        />
      </template>
    </OnmsTable>

    <OnmsMenu
      ref="rowMenu"
      :items="rowMenuItems"
    />

    <div v-if="!store.roles.length">
      <EmptyList
        :content="emptyListContent"
        data-test="empty-list"
      />
    </div>
  </TableCard>

  <OnCallRoleRenameDialog
    v-model:visible="showRename"
    :roleName="actionRoleName"
    :version="actionVersion"
  />
  <OnmsConfirmationDialog
    :visible="showDeleteConfirmation"
    title="Delete On-Call Role"
    actionButtonText="Delete"
    @ok="confirmDelete"
    @cancel="cancelDelete"
  >
    <template #content>
      <p>
        Are you sure you want to delete the on-call role <strong>{{ roleToDelete?.name }}</strong>?
        A role that a notification destination path names can't be deleted until the path no longer names it.
        This action cannot be undone.
      </p>
    </template>
  </OnmsConfirmationDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import { OnmsButton, OnmsColumn, OnmsConfirmationDialog, OnmsIconButton, OnmsMenu, OnmsMenuItem, OnmsSearchInput, OnmsTable, OnmsTag } from '@opennms/onms-ui'

import AboutDialogButton from '@/components/Common/AboutDialogButton.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import OnCallRoleRenameDialog from '@/components/OnCallRoles/OnCallRoleRenameDialog.vue'
import OnCallRolesAbout from '@/components/OnCallRoles/OnCallRolesAbout.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import useAdminWriteFeedback from '@/composables/useAdminWriteFeedback'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import Edit from '@opennms/onms-ui/icons/action/Edit.vue'
import MoreVert from '@opennms/onms-ui/icons/navigation/MoreVert.vue'
import { useOnCallRoleAdminStore } from '@/stores/onCallRoleAdminStore'
import { ManagedOnCallRole } from '@/types/onCallRoleAdmin'

const store = useOnCallRoleAdminStore()
const { withSpinner } = useActionFeedback()
const { reportWrite } = useAdminWriteFeedback()

const showRename = ref(false)
const actionRoleName = ref('')
const actionVersion = ref(0)
const showDeleteConfirmation = ref(false)
const roleToDelete = ref<ManagedOnCallRole | null>(null)

const emptyListContent = {
  msg: 'No on-call roles found.'
}

// the table only shows while there are roles, so an empty table means the filter matched none
const noMatchContent = {
  msg: 'No on-call roles match the filter.'
}

const filter = ref('')
const first = ref(0)

// client-side: name, supervisor, membership group and description, ignoring case; the paginator goes back to page 1
const filteredRoles = computed(() => {
  const term = (filter.value ?? '').trim().toLowerCase()
  if (!term) {
    return store.roles
  }
  return store.roles.filter(r => [r.name, r.supervisor, r.membershipGroup, r.description]
    .some(v => (v ?? '').toLowerCase().includes(term)))
})
watch(filter, () => {
  first.value = 0
})

// the editor panel replaces this table (see ManageOnCallRoles)
const openEditor = (role: ManagedOnCallRole | null) => store.openRoleEditor(role)

const openRename = (role: ManagedOnCallRole) => {
  actionRoleName.value = role.name
  actionVersion.value = role.version
  showRename.value = true
}

const askDelete = (role: ManagedOnCallRole) => {
  roleToDelete.value = role
  showDeleteConfirmation.value = true
}

// Secondary row actions live in a single shared overflow menu (Edit and Delete
// stay inline); the target is captured on open so one menu serves every row.
const rowMenu = ref()
const rowMenuTarget = ref<ManagedOnCallRole | null>(null)
const rowMenuItems = computed<OnmsMenuItem[]>(() => {
  const target = rowMenuTarget.value
  if (!target) {
    return []
  }
  return [
    { label: 'Rename', command: () => openRename(target) }
  ]
})

const toggleRowMenu = (event: Event, role: ManagedOnCallRole) => {
  rowMenuTarget.value = role
  rowMenu.value?.toggle(event)
}

const confirmDelete = async () => {
  const role = roleToDelete.value
  if (role) {
    const result = await withSpinner(() => store.deleteRole(role.name, role.version))
    // no banner here: a failure, stale or not, is a snackbar; the confirmation closes either way
    reportWrite(result, `On-call role '${role.name}' deleted.`)
  }
  showDeleteConfirmation.value = false
  roleToDelete.value = null
}

const cancelDelete = () => {
  showDeleteConfirmation.value = false
  roleToDelete.value = null
}
</script>

<style lang="scss" scoped>
.oncall-roles-table {
  padding: 25px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.card-title {
  font-size: 1.1rem;
  font-weight: 600;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.role-name {
  font-weight: 600;
}

.fallback-tag {
  margin-left: 0.5rem;
}

.on-call-now {
  font-size: 0.875rem;
}

.description {
  display: inline-block;
  max-width: 20rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: bottom;
  color: var(--p-text-muted-color);
}

.roles-filter {
  display: block;
  margin-bottom: 0.75rem;
  max-width: 24rem;
}

.action-container {
  display: flex;
  gap: 0.25rem;
  flex-wrap: wrap;
}
</style>
