<template>
  <TableCard class="roles-table">
    <div class="header">
      <div class="card-title">On-Call Roles</div>
      <OnmsButton
        variant="outlined"
        label="Add New Role"
        icon="pi pi-plus"
        data-test="add-role-button"
        @click="openEditor(null)"
      />
    </div>

    <OnmsTable
      v-if="store.roles.length"
      :value="store.roles"
      paginator
      dataKey="name"
      :rows="10"
      :rowsPerPageOptions="[10, 20, 50, 100]"
      class="data-table"
      data-test="roles-table"
    >
      <OnmsColumn
        field="name"
        header="Role Name"
        sortable
      />
      <OnmsColumn header="Currently On Call">
        <template #body="{ data }">
          <OnmsTag
            v-if="data['schedule-error']"
            severity="warn"
            value="schedule error"
            v-tooltip.top="data['schedule-error']"
            data-test="schedule-error-tag"
          />
          <span
            v-else-if="(data['currently-on-call'] ?? []).length"
            class="on-call"
          >{{ data['currently-on-call'].join(', ') }}</span>
          <span
            v-else
            class="on-call fallback"
            v-tooltip.top="'Nobody scheduled; the supervisor handles notifications'"
          >{{ data.supervisor }} (supervisor)</span>
        </template>
      </OnmsColumn>
      <OnmsColumn
        field="supervisor"
        header="Supervisor"
        sortable
      />
      <OnmsColumn
        field="membership-group"
        header="Membership Group"
        sortable
      />
      <OnmsColumn
        field="description"
        header="Description"
      />
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <span
            v-if="!isPathAddressable(data.name)"
            class="unaddressable"
            v-tooltip.top="'This role name contains / \\ or %, which the API cannot address; manage it by editing groups.xml.'"
            data-test="unaddressable-note"
          >file-managed</span>
          <div v-else class="action-container">
            <OnmsButton
              variant="text"
              label="Schedule"
              :aria-label="`View the schedule for ${data.name}`"
              data-test="schedule-role-button"
              @click="openCalendar(data)"
            />
            <OnmsButton
              variant="text"
              label="Edit"
              :aria-label="`Edit ${data.name}`"
              data-test="edit-role-button"
              @click="openEditor(data)"
            />
            <OnmsButton
              variant="text"
              label="Rename"
              :aria-label="`Rename ${data.name}`"
              data-test="rename-role-button"
              @click="openRename(data)"
            />
            <OnmsButton
              variant="text"
              label="Delete"
              severity="danger"
              :aria-label="`Delete ${data.name}`"
              data-test="delete-role-button"
              @click="askDelete(data)"
            />
          </div>
        </template>
      </OnmsColumn>
    </OnmsTable>

    <div v-if="!store.roles.length">
      <EmptyList
        :content="emptyListContent"
        data-test="empty-list"
      />
    </div>
  </TableCard>

  <RoleEditorDialog
    v-model:visible="showEditor"
    :role="roleToEdit"
  />
  <RoleRenameDialog
    v-model:visible="showRename"
    :roleName="actionRoleName"
  />
  <RoleCalendarDialog
    v-model:visible="showCalendar"
    :roleName="actionRoleName"
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
        Are you sure you want to delete the on-call role
        <strong>{{ roleToDelete?.name }}</strong>? Destination paths targeting
        it will stop resolving. This action cannot be undone.
      </p>
    </template>
  </OnmsConfirmationDialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import { OnmsButton, OnmsColumn, OnmsConfirmationDialog, OnmsTable, OnmsTag } from '@opennms/onms-ui'

import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import RoleCalendarDialog from '@/components/ManageOnCallRoles/RoleCalendarDialog.vue'
import RoleEditorDialog from '@/components/ManageOnCallRoles/RoleEditorDialog.vue'
import RoleRenameDialog from '@/components/ManageOnCallRoles/RoleRenameDialog.vue'
import { isPathAddressable } from '@/lib/adminValidation'
import { useOnCallRoleAdminStore } from '@/stores/onCallRoleAdminStore'
import { OnCallRole } from '@/types/onCallRoleAdmin'

const store = useOnCallRoleAdminStore()

const showEditor = ref(false)
const roleToEdit = ref<OnCallRole | null>(null)
const showRename = ref(false)
const showCalendar = ref(false)
const actionRoleName = ref('')
const showDeleteConfirmation = ref(false)
const roleToDelete = ref<OnCallRole | null>(null)

const emptyListContent = {
  msg: 'No on-call roles found.'
}

const openEditor = (role: OnCallRole | null) => {
  roleToEdit.value = role
  showEditor.value = true
}

const openRename = (role: OnCallRole) => {
  actionRoleName.value = role.name
  showRename.value = true
}

const openCalendar = (role: OnCallRole) => {
  actionRoleName.value = role.name
  showCalendar.value = true
}

const askDelete = (role: OnCallRole) => {
  roleToDelete.value = role
  showDeleteConfirmation.value = true
}

const confirmDelete = async () => {
  if (roleToDelete.value) {
    await store.deleteRole(roleToDelete.value.name)
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
.roles-table {
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

.on-call {
  font-weight: 500;

  &.fallback {
    color: var(--p-text-muted-color);
    font-weight: 400;
  }
}

.unaddressable {
  color: var(--p-text-muted-color);
  font-style: italic;
}

.action-container {
  display: flex;
  gap: 0.25rem;
  flex-wrap: wrap;
}
</style>
