<template>
  <TableCard class="groups-table">
    <div class="header">
      <div class="card-title">Groups</div>
      <div class="header-actions">
        <OnmsButton
          variant="outlined"
          label="Add New Group"
          icon="pi pi-plus"
          data-test="add-group-button"
          @click="openEditor(null)"
        />
        <AboutDialogButton title="Groups">
          <GroupsAbout />
        </AboutDialogButton>
      </div>
    </div>

    <OnmsTable
      v-if="store.groups.length"
      :value="store.groups"
      paginator
      dataKey="name"
      :rows="10"
      :rowsPerPageOptions="[10, 20, 50, 100]"
      class="data-table"
      data-test="groups-table"
    >
      <OnmsColumn
        field="name"
        header="Group Name"
        sortable
      >
        <template #body="{ data }">
          <span class="group-name">{{ data.name }}</span>
          <OnmsTag
            v-if="isProtected(data.name)"
            value="system"
            severity="secondary"
            class="system-tag"
            v-tooltip.top="'System group: cannot be deleted or renamed'"
          />
        </template>
      </OnmsColumn>
      <OnmsColumn
        field="comments"
        header="Comments"
        sortable
      />
      <OnmsColumn header="Members">
        <template #body="{ data }">
          <span class="members">{{ (data.users ?? []).join(', ') || '--' }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <span
            v-if="!isPathAddressable(data.name)"
            class="unaddressable"
            v-tooltip.top="'This group name contains / \\ or %, which the API cannot address; manage it by editing groups.xml.'"
            data-test="unaddressable-note"
          >file-managed</span>
          <div v-else class="action-container">
            <OnmsIconButton
              :icon="Edit"
              :title="`Edit ${data.name}`"
              :aria-label="`Edit ${data.name}`"
              data-test="edit-group-button"
              @click="openEditor(data)"
            />
            <OnmsIconButton
              :icon="Delete"
              severity="danger"
              :disabled="isProtected(data.name)"
              :title="`Delete ${data.name}`"
              :aria-label="`Delete ${data.name}`"
              data-test="delete-group-button"
              @click="askDelete(data)"
            />
            <OnmsIconButton
              :icon="MoreVert"
              title="More actions"
              :aria-label="`More actions for ${data.name}`"
              data-test="group-actions-menu-button"
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

    <div v-if="!store.groups.length">
      <EmptyList
        :content="emptyListContent"
        data-test="empty-list"
      />
    </div>
  </TableCard>

  <GroupEditorDialog
    v-model:visible="showEditor"
    :group="groupToEdit"
  />
  <GroupRenameDialog
    v-model:visible="showRename"
    :groupName="actionGroupName"
  />
  <OnmsConfirmationDialog
    :visible="showDeleteConfirmation"
    title="Delete Group"
    actionButtonText="Delete"
    @ok="confirmDelete"
    @cancel="cancelDelete"
  >
    <template #content>
      <p>
        Are you sure you want to delete the group <strong>{{ groupToDelete?.name }}</strong>?
        Destination paths targeting it will stop resolving. This action cannot be undone.
      </p>
    </template>
  </OnmsConfirmationDialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

import { OnmsButton, OnmsColumn, OnmsConfirmationDialog, OnmsIconButton, OnmsMenu, OnmsMenuItem, OnmsTable, OnmsTag } from '@opennms/onms-ui'

import AboutDialogButton from '@/components/Common/AboutDialogButton.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import GroupEditorDialog from '@/components/ManageGroups/GroupEditorDialog.vue'
import GroupRenameDialog from '@/components/ManageGroups/GroupRenameDialog.vue'
import GroupsAbout from '@/components/ManageGroups/GroupsAbout.vue'
import Delete from '@/components/icons/action/Delete.vue'
import Edit from '@/components/icons/action/Edit.vue'
import MoreVert from '@/components/icons/navigation/MoreVert.vue'
import { isPathAddressable } from '@/lib/adminValidation'
import { useGroupAdminStore } from '@/stores/groupAdminStore'
import { ManagedGroup, PROTECTED_GROUP_NAMES } from '@/types/groupAdmin'

const store = useGroupAdminStore()

const showEditor = ref(false)
const groupToEdit = ref<ManagedGroup | null>(null)
const showRename = ref(false)
const actionGroupName = ref('')
const showDeleteConfirmation = ref(false)
const groupToDelete = ref<ManagedGroup | null>(null)

const emptyListContent = {
  msg: 'No groups found.'
}

const isProtected = (name: string) => PROTECTED_GROUP_NAMES.includes(name)

const openEditor = (group: ManagedGroup | null) => {
  groupToEdit.value = group
  showEditor.value = true
}

const openRename = (group: ManagedGroup) => {
  actionGroupName.value = group.name
  showRename.value = true
}

const askDelete = (group: ManagedGroup) => {
  groupToDelete.value = group
  showDeleteConfirmation.value = true
}

// Secondary row actions live in a single shared overflow menu (Edit and Delete
// stay inline); the target is captured on open so one menu serves every row.
const rowMenu = ref()
const rowMenuTarget = ref<ManagedGroup | null>(null)
const rowMenuItems = computed<OnmsMenuItem[]>(() => {
  const target = rowMenuTarget.value
  if (!target) {
    return []
  }
  return [
    { label: 'Rename', disabled: isProtected(target.name), command: () => openRename(target) }
  ]
})

const toggleRowMenu = (event: Event, group: ManagedGroup) => {
  rowMenuTarget.value = group
  rowMenu.value?.toggle(event)
}

const confirmDelete = async () => {
  if (groupToDelete.value) {
    await store.deleteGroup(groupToDelete.value.name)
  }
  showDeleteConfirmation.value = false
  groupToDelete.value = null
}

const cancelDelete = () => {
  showDeleteConfirmation.value = false
  groupToDelete.value = null
}
</script>

<style lang="scss" scoped>
.groups-table {
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

.group-name {
  font-weight: 600;
}

.system-tag {
  margin-left: 0.5rem;
}

.members {
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
