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

    <OnmsSearchInput
      v-if="store.groups.length"
      v-model="filter"
      class="groups-filter"
      placeholder="Filter groups"
      aria-label="Filter groups"
      data-test="groups-filter"
    />

    <OnmsTable
      v-if="store.groups.length"
      v-model:first="first"
      :value="filteredGroups"
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
            v-onms-tooltip.top="'System group: cannot be deleted or renamed'"
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
          <div class="action-container">
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

  <GroupRenameDialog
    v-model:visible="showRename"
    :groupName="actionGroupName"
    :version="actionVersion"
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
        A group that an on-call role or a destination path refers to cannot be deleted. This action cannot be undone.
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
import GroupRenameDialog from '@/components/ManageGroups/GroupRenameDialog.vue'
import GroupsAbout from '@/components/ManageGroups/GroupsAbout.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import useAdminWriteFeedback from '@/composables/useAdminWriteFeedback'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import Edit from '@opennms/onms-ui/icons/action/Edit.vue'
import MoreVert from '@opennms/onms-ui/icons/navigation/MoreVert.vue'
import { useGroupAdminStore } from '@/stores/groupAdminStore'
import { ManagedGroup, PROTECTED_GROUP_NAMES } from '@/types/groupAdmin'

const store = useGroupAdminStore()
const { withSpinner } = useActionFeedback()
const { reportWrite } = useAdminWriteFeedback()

const showRename = ref(false)
const actionGroupName = ref('')
const actionVersion = ref(0)
const showDeleteConfirmation = ref(false)
const groupToDelete = ref<ManagedGroup | null>(null)

const emptyListContent = {
  msg: 'No groups found.'
}

const filter = ref('')
const first = ref(0)

// client-side: name, comments and member ids, ignoring case; the paginator goes back to page 1
const filteredGroups = computed(() => {
  const term = (filter.value ?? '').trim().toLowerCase()
  if (!term) {
    return store.groups
  }
  return store.groups.filter(g => [g.name, g.comments, ...(g.users ?? [])]
    .some(v => (v ?? '').toLowerCase().includes(term)))
})
watch(filter, () => {
  first.value = 0
})

const isProtected = (name: string) => PROTECTED_GROUP_NAMES.includes(name)

// the editor panel replaces this table (see ManageGroups)
const openEditor = (group: ManagedGroup | null) => store.openGroupEditor(group)

const openRename = (group: ManagedGroup) => {
  actionGroupName.value = group.name
  actionVersion.value = group.version
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
  const group = groupToDelete.value
  if (group) {
    const error = await withSpinner(() => store.deleteGroup(group.name, group.version))
    // no banner here: a failure, stale or not, is a snackbar; the confirmation closes either way
    reportWrite(error, `Group '${group.name}' deleted.`, store.reloadError)
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

.groups-filter {
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
