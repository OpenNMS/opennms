<template>
  <TableCard class="groups-table">
    <div class="header">
      <div class="card-title">Groups</div>
      <Button
        outlined
        label="Add New Group"
        icon="pi pi-plus"
        data-test="add-group-button"
        @click="openEditor(null)"
      />
    </div>

    <DataTable
      v-if="store.groups.length"
      :value="store.groups"
      paginator
      dataKey="name"
      :rows="10"
      :rowsPerPageOptions="[10, 20, 50, 100]"
      class="data-table"
      data-test="groups-table"
    >
      <Column
        field="name"
        header="Group Name"
        sortable
      >
        <template #body="{ data }">
          <span class="group-name">{{ data.name }}</span>
          <Tag
            v-if="isProtected(data.name)"
            value="system"
            severity="secondary"
            class="system-tag"
            v-tooltip.top="'System group: cannot be deleted or renamed'"
          />
        </template>
      </Column>
      <Column
        field="comments"
        header="Comments"
        sortable
      />
      <Column header="Members">
        <template #body="{ data }">
          <span class="members">{{ (data.user ?? []).join(', ') || '-' }}</span>
        </template>
      </Column>
      <Column header="Actions">
        <template #body="{ data }">
          <span
            v-if="!isPathAddressable(data.name)"
            class="unaddressable"
            v-tooltip.top="'This group name contains / \\ or %, which the API cannot address; manage it by editing groups.xml.'"
            data-test="unaddressable-note"
          >file-managed</span>
          <div v-else class="action-container">
            <Button
              text
              label="Edit"
              :aria-label="`Edit ${data.name}`"
              data-test="edit-group-button"
              @click="openEditor(data)"
            />
            <Button
              text
              label="Rename"
              :disabled="isProtected(data.name)"
              :aria-label="`Rename ${data.name}`"
              data-test="rename-group-button"
              @click="openRename(data)"
            />
            <Button
              text
              label="Delete"
              severity="danger"
              :disabled="isProtected(data.name)"
              :aria-label="`Delete ${data.name}`"
              data-test="delete-group-button"
              @click="askDelete(data)"
            />
          </div>
        </template>
      </Column>
    </DataTable>

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
import { ref } from 'vue'

import { OnmsConfirmationDialog } from '@opennms/onms-ui'

import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Tag from 'primevue/tag'

import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import GroupEditorDialog from '@/components/ManageGroups/GroupEditorDialog.vue'
import GroupRenameDialog from '@/components/ManageGroups/GroupRenameDialog.vue'
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
