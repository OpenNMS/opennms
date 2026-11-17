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
          @click="openEditor(null)"
        />
        <AboutDialogButton title="Users">
          <UsersAbout />
        </AboutDialogButton>
      </div>
    </div>

    <OnmsSearchInput
      v-if="store.users.length"
      v-model="filter"
      class="users-filter"
      placeholder="Filter users"
      aria-label="Filter users"
      data-test="users-filter"
    />

    <OnmsTable
      v-if="store.users.length"
      v-model:first="first"
      :value="filteredUsers"
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
          <span class="user-id">{{ data.userId }}</span>
          <OnmsTag
            v-if="isProtected(data.userId)"
            value="system"
            severity="secondary"
            class="system-tag"
            v-onms-tooltip.top="'System account: cannot be deleted or renamed'"
          />
          <OnmsTag
            v-if="data.readOnly"
            value="read-only"
            severity="secondary"
            class="system-tag"
            data-test="read-only-tag"
            v-onms-tooltip.top="'This user cannot change anything in the web console'"
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
      <OnmsColumn header="Comments">
        <template #body="{ data }">
          <span
            v-if="data.userComments"
            class="comments"
            v-onms-tooltip.top="data.userComments"
            data-test="user-comments"
          >{{ data.userComments }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Roles">
        <template #body="{ data }">
          <span class="roles">{{ (data.roles ?? []).join(', ') || '--' }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <div class="action-container">
            <OnmsIconButton
              :icon="Edit"
              :title="`Edit ${data.userId}`"
              :aria-label="`Edit ${data.userId}`"
              data-test="edit-user-button"
              @click="openEditor(data)"
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
    :version="actionVersion"
  />
  <UserRenameDialog
    v-model:visible="showRename"
    :userId="actionUserId"
    :version="actionVersion"
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
        The user is also removed from all groups. A user cannot be deleted while they supervise an
        on-call role or a destination path sends to them, or if they are the last administrator.
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
import UserPasswordDialog from '@/components/ManageUsers/UserPasswordDialog.vue'
import UserRenameDialog from '@/components/ManageUsers/UserRenameDialog.vue'
import UsersAbout from '@/components/ManageUsers/UsersAbout.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import useAdminWriteFeedback from '@/composables/useAdminWriteFeedback'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import Edit from '@opennms/onms-ui/icons/action/Edit.vue'
import MoreVert from '@opennms/onms-ui/icons/navigation/MoreVert.vue'
import { useUserAdminStore } from '@/stores/userAdminStore'
import { ManagedUser, PROTECTED_USER_IDS } from '@/types/userAdmin'

const store = useUserAdminStore()
const { withSpinner } = useActionFeedback()
const { reportWrite } = useAdminWriteFeedback()

const showPassword = ref(false)
const showRename = ref(false)
const actionUserId = ref('')
const actionVersion = ref(0)
const showDeleteConfirmation = ref(false)
const userToDelete = ref<ManagedUser | null>(null)

const emptyListContent = {
  msg: 'No users found.'
}

// the table only shows while there are users, so an empty table means the filter matched none
const noMatchContent = {
  msg: 'No users match the filter.'
}

const filter = ref('')
const first = ref(0)

// client-side: id, full name, email and comments, ignoring case; the paginator goes back to page 1
const filteredUsers = computed(() => {
  const term = (filter.value ?? '').trim().toLowerCase()
  if (!term) {
    return store.users
  }
  return store.users.filter(u => [u.userId, u.fullName, u.email, u.userComments]
    .some(v => (v ?? '').toLowerCase().includes(term)))
})
watch(filter, () => {
  first.value = 0
})

const isProtected = (userId: string) => PROTECTED_USER_IDS.includes(userId)

// the editor panel replaces this table (see ManageUsers)
const openEditor = (user: ManagedUser | null) => store.openUserEditor(user)

const openPassword = (user: ManagedUser) => {
  actionUserId.value = user.userId
  actionVersion.value = user.version
  showPassword.value = true
}

const openRename = (user: ManagedUser) => {
  actionUserId.value = user.userId
  actionVersion.value = user.version
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
  const user = userToDelete.value
  if (user) {
    const result = await withSpinner(() => store.deleteUser(user.userId, user.version))
    // no banner here: a failure, stale or not, is a snackbar; the confirmation closes either way
    reportWrite(result, `User '${user.userId}' deleted.`)
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

.users-filter {
  display: block;
  margin-bottom: 0.75rem;
  max-width: 24rem;
}

.comments {
  display: inline-block;
  max-width: 16rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: bottom;
}

.action-container {
  display: flex;
  gap: 0.25rem;
  flex-wrap: wrap;
}
</style>
