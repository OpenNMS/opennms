<template>
  <TableCard class="users-table">
    <div class="header">
      <div class="card-title">Users</div>
      <OnmsButton
        variant="outlined"
        label="Add New User"
        icon="pi pi-plus"
        data-test="add-user-button"
        @click="openEditor(null)"
      />
    </div>

    <OnmsTable
      v-if="store.users.length"
      :value="store.users"
      paginator
      dataKey="userId"
      :rows="10"
      :rowsPerPageOptions="[10, 20, 50, 100]"
      class="data-table"
      data-test="users-table"
    >
      <OnmsColumn
        field="userId"
        header="User ID"
        sortable
      >
        <template #body="{ data }">
          <span class="userId">{{ data.userId }}</span>
          <OnmsTag
            v-if="isProtected(data.userId)"
            value="system"
            severity="secondary"
            class="system-tag"
            v-tooltip.top="'System account: cannot be deleted or renamed'"
          />
        </template>
      </OnmsColumn>
      <OnmsColumn
        field="full-name"
        header="Full Name"
        sortable
      />
      <OnmsColumn
        field="email"
        header="Email"
        sortable
      />
      <OnmsColumn
        field="pager-email"
        header="Pager Email"
      />
      <OnmsColumn header="Roles">
        <template #body="{ data }">
          <span class="roles">{{ (data.roles ?? []).join(', ') || '-' }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <span
            v-if="!isPathAddressable(data.userId)"
            class="unaddressable"
            v-tooltip.top="'This user-id contains / \\ or %, which the API cannot address; manage it by editing users.xml.'"
            data-test="unaddressable-note"
          >file-managed</span>
          <div v-else class="action-container">
            <OnmsButton
              variant="text"
              label="Edit"
              :aria-label="`Edit ${data.userId}`"
              data-test="edit-user-button"
              @click="openEditor(data)"
            />
            <OnmsButton
              variant="text"
              label="Password"
              :aria-label="`Change password for ${data.userId}`"
              data-test="password-user-button"
              @click="openPassword(data)"
            />
            <OnmsButton
              variant="text"
              label="Rename"
              :disabled="isProtected(data.userId)"
              :aria-label="`Rename ${data.userId}`"
              data-test="rename-user-button"
              @click="openRename(data)"
            />
            <OnmsButton
              variant="text"
              label="Delete"
              severity="danger"
              :disabled="isProtected(data.userId)"
              :aria-label="`Delete ${data.userId}`"
              data-test="delete-user-button"
              @click="askDelete(data)"
            />
          </div>
        </template>
      </OnmsColumn>
    </OnmsTable>

    <div v-if="!store.users.length">
      <EmptyList
        :content="emptyListContent"
        data-test="empty-list"
      />
    </div>
  </TableCard>

  <UserEditorDialog
    v-model:visible="showEditor"
    :user="userToEdit"
  />
  <UserPasswordDialog
    v-model:visible="showPassword"
    :userId="actionUserId"
  />
  <UserRenameDialog
    v-model:visible="showRename"
    :userId="actionUserId"
  />
  <OnmsConfirmationDialog
    :visible="showDeleteConfirmation"
    title="Delete User"
    actionButtonText="Delete"
    @ok="confirmDelete"
    @cancel="cancelDelete"
  >
    <template #content>
      <p>
        Are you sure you want to delete the user <strong>{{ userToDelete?.userId }}</strong>?
        The user is also removed from all groups. This action cannot be undone.
      </p>
    </template>
  </OnmsConfirmationDialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import { OnmsButton, OnmsColumn, OnmsConfirmationDialog, OnmsTable, OnmsTag } from '@opennms/onms-ui'

import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import UserEditorDialog from '@/components/ManageUsers/UserEditorDialog.vue'
import UserPasswordDialog from '@/components/ManageUsers/UserPasswordDialog.vue'
import UserRenameDialog from '@/components/ManageUsers/UserRenameDialog.vue'
import { isPathAddressable } from '@/lib/adminValidation'
import { useUserAdminStore } from '@/stores/userAdminStore'
import { ManagedUser, PROTECTED_USER_IDS } from '@/types/userAdmin'

const store = useUserAdminStore()

const showEditor = ref(false)
const userToEdit = ref<ManagedUser | null>(null)
const showPassword = ref(false)
const showRename = ref(false)
const actionUserId = ref('')
const showDeleteConfirmation = ref(false)
const userToDelete = ref<ManagedUser | null>(null)

const emptyListContent = {
  msg: 'No users found.'
}

const isProtected = (userId: string) => PROTECTED_USER_IDS.includes(userId)

const openEditor = (user: ManagedUser | null) => {
  userToEdit.value = user
  showEditor.value = true
}

const openPassword = (user: ManagedUser) => {
  actionUserId.value = user.userId
  showPassword.value = true
}

const openRename = (user: ManagedUser) => {
  actionUserId.value = user.userId
  showRename.value = true
}

const askDelete = (user: ManagedUser) => {
  userToDelete.value = user
  showDeleteConfirmation.value = true
}

const confirmDelete = async () => {
  if (userToDelete.value) {
    await store.deleteUser(userToDelete.value.userId)
  }
  showDeleteConfirmation.value = false
  userToDelete.value = null
}

const cancelDelete = () => {
  showDeleteConfirmation.value = false
  userToDelete.value = null
}
</script>

<style lang="scss" scoped>
.users-table {
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

.user-id {
  font-weight: 600;
}

.system-tag {
  margin-left: 0.5rem;
}

.roles {
  font-size: 0.875rem;
  color: var(--p-text-muted-color);
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
