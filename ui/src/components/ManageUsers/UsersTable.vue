<template>
  <TableCard class="users-table">
    <div class="header">
      <div class="card-title">Users</div>
      <div class="header-actions">
        <OnmsButton
          variant="outlined"
          label="Add New User"
          icon="pi pi-plus"
          data-test="add-user-button"
          @click="router.push({ path: '/admin/users/create' })"
        />
        <AboutDialogButton title="Users">
          <UsersAbout />
        </AboutDialogButton>
      </div>
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
        field="fullName"
        header="Full Name"
        sortable
      />
      <OnmsColumn
        field="email"
        header="Email"
        sortable
      />
      <OnmsColumn
        field="pagerEmail"
        header="Pager Email"
      />
      <OnmsColumn header="Roles">
        <template #body="{ data }">
          <span class="roles">{{ (data.roles ?? []).join(', ') || '--' }}</span>
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
            <OnmsIconButton
              :icon="Edit"
              :title="`Edit ${data.userId}`"
              :aria-label="`Edit ${data.userId}`"
              data-test="edit-user-button"
              @click="openEditor(data.userId)"
            />
            <OnmsIconButton
              :icon="Delete"
              severity="danger"
              :disabled="isProtected(data.userId)"
              :title="`Delete ${data.userId}`"
              :aria-label="`Delete ${data.userId}`"
              data-test="delete-user-button"
              @click="askDelete(data)"
            />
            <OnmsIconButton
              :icon="MoreVert"
              title="More actions"
              :aria-label="`More actions for ${data.userId}`"
              data-test="user-actions-menu-button"
              @click="toggleRowMenu($event, data)"
            />
          </div>
        </template>
      </OnmsColumn>
    </OnmsTable>

    <OnmsMenu
      ref="rowMenu"
      :items="rowMenuItems"
    />

    <div v-if="!store.users.length">
      <EmptyList
        :content="emptyListContent"
        data-test="empty-list"
      />
    </div>
  </TableCard>

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
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

import { OnmsButton, OnmsColumn, OnmsConfirmationDialog, OnmsIconButton, OnmsMenu, OnmsMenuItem, OnmsTable, OnmsTag } from '@opennms/onms-ui'

import AboutDialogButton from '@/components/Common/AboutDialogButton.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import UserPasswordDialog from '@/components/ManageUsers/UserPasswordDialog.vue'
import UserRenameDialog from '@/components/ManageUsers/UserRenameDialog.vue'
import UsersAbout from '@/components/ManageUsers/UsersAbout.vue'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import Edit from '@opennms/onms-ui/icons/action/Edit.vue'
import MoreVert from '@opennms/onms-ui/icons/navigation/MoreVert.vue'
import { isPathAddressable } from '@/lib/adminValidation'
import { useUserAdminStore } from '@/stores/userAdminStore'
import { ManagedUser, PROTECTED_USER_IDS } from '@/types/userAdmin'

const store = useUserAdminStore()
const router = useRouter()

const showPassword = ref(false)
const showRename = ref(false)
const actionUserId = ref('')
const showDeleteConfirmation = ref(false)
const userToDelete = ref<ManagedUser | null>(null)

const emptyListContent = {
  msg: 'No users found.'
}

const isProtected = (userId: string) => PROTECTED_USER_IDS.includes(userId)

// the editor is a routed page now (NMS-20281): too much form for a dialog
const openEditor = (userId: string) => {
  router.push({ path: `/admin/users/${encodeURIComponent(userId)}` })
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

// Secondary row actions live in a single shared overflow menu (Edit and Delete
// stay inline); the target is captured on open so one menu serves every row.
const rowMenu = ref()
const rowMenuTarget = ref<ManagedUser | null>(null)
const rowMenuItems = computed<OnmsMenuItem[]>(() => {
  const target = rowMenuTarget.value
  if (!target) {
    return []
  }
  return [
    { label: 'Change Password', command: () => openPassword(target) },
    { label: 'Rename', disabled: isProtected(target.userId), command: () => openRename(target) }
  ]
})

const toggleRowMenu = (event: Event, user: ManagedUser) => {
  rowMenuTarget.value = user
  rowMenu.value?.toggle(event)
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
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
