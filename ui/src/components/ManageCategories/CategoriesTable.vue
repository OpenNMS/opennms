<template>
  <TableCard class="categories-table">
    <div class="header">
      <div class="card-title">Surveillance Categories</div>
      <div class="header-actions">
        <OnmsButton
          variant="outlined"
          label="Add New Category"
          icon="pi pi-plus"
          data-test="add-category-button"
          @click="openEditor(null)"
        />
        <AboutDialogButton title="Surveillance Categories">
          <CategoriesAbout />
        </AboutDialogButton>
      </div>
    </div>

    <OnmsTable
      :value="store.categories"
      :paginator="store.categories.length > 0"
      dataKey="name"
      sortField="name"
      :sortOrder="1"
      :rows="10"
      :rowsPerPageOptions="[10, 20, 50, 100]"
      class="data-table"
      data-test="categories-table"
    >
      <template #empty>
        <EmptyList
          :content="store.loadError ? errorListContent : emptyListContent"
          data-test="empty-list"
        />
      </template>
      <OnmsColumn field="name" header="Name" sortable />
      <OnmsColumn header="Description">
        <template #body="{ data }">{{ data.description || '-' }}</template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <div class="action-container">
            <OnmsIconButton
              :icon="Nodes"
              :title="`Manage nodes in ${data.name}`"
              :aria-label="`Manage nodes in ${data.name}`"
              data-test="manage-nodes-button"
              @click="openNodes(data)"
            />
            <OnmsIconButton
              :icon="Edit"
              :title="`Edit ${data.name}`"
              :aria-label="`Edit ${data.name}`"
              data-test="edit-category-button"
              @click="openEditor(data)"
            />
            <OnmsIconButton
              :icon="Delete"
              severity="danger"
              :title="`Delete ${data.name}`"
              :aria-label="`Delete ${data.name}`"
              data-test="delete-category-button"
              @click="askDelete(data)"
            />
          </div>
        </template>
      </OnmsColumn>
    </OnmsTable>
  </TableCard>

  <CategoryEditorDialog v-model:visible="showEditor" :category="categoryToEdit" />
  <CategoryNodesDialog v-model:visible="showNodes" :categoryId="actionCategoryId" :categoryName="actionCategoryName" />
  <OnmsConfirmationDialog
    :visible="showDeleteConfirmation"
    title="Delete Surveillance Category"
    actionButtonText="Delete"
    @ok="confirmDelete"
    @cancel="cancelDelete"
  >
    <template #content>
      <p>
        Are you sure you want to delete the surveillance category
        <strong>{{ categoryToDelete?.name }}</strong>? Nodes will lose this
        category, and any group authorizations or filters that reference it by
        name will no longer match. This action cannot be undone.
      </p>
    </template>
  </OnmsConfirmationDialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import { OnmsButton, OnmsColumn, OnmsConfirmationDialog, OnmsIconButton, OnmsTable, useOnmsToast } from '@opennms/onms-ui'

import AboutDialogButton from '@/components/Common/AboutDialogButton.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import Edit from '@opennms/onms-ui/icons/action/Edit.vue'
import Nodes from '@opennms/onms-ui/icons/network/Nodes.vue'
import TableCard from '@/components/Common/TableCard.vue'
import CategoriesAbout from '@/components/ManageCategories/CategoriesAbout.vue'
import CategoryEditorDialog from '@/components/ManageCategories/CategoryEditorDialog.vue'
import CategoryNodesDialog from '@/components/ManageCategories/CategoryNodesDialog.vue'
import { useCategoryAdminStore } from '@/stores/categoryAdminStore'
import { AdminCategory } from '@/types/categoryAdmin'

const store = useCategoryAdminStore()
const { showToast } = useOnmsToast()

const showEditor = ref(false)
const categoryToEdit = ref<AdminCategory | null>(null)
const showNodes = ref(false)
const actionCategoryName = ref('')
const actionCategoryId = ref<number | null>(null)
const showDeleteConfirmation = ref(false)
const categoryToDelete = ref<AdminCategory | null>(null)

const emptyListContent = { msg: 'No surveillance categories found.' }
const errorListContent = { msg: 'Could not load surveillance categories. Please retry.' }

const openEditor = (category: AdminCategory | null) => {
  categoryToEdit.value = category
  showEditor.value = true
}

const openNodes = (category: AdminCategory) => {
  actionCategoryName.value = category.name
  actionCategoryId.value = category.id ?? null
  showNodes.value = true
}

const askDelete = (category: AdminCategory) => {
  categoryToDelete.value = category
  showDeleteConfirmation.value = true
}

const confirmDelete = async () => {
  const category = categoryToDelete.value
  showDeleteConfirmation.value = false
  categoryToDelete.value = null
  if (!category) {
    return
  }
  const result = await store.deleteCategory(category.name)
  if (result.success) {
    showToast({ message: `Category '${category.name}' deleted.`, severity: 'success' })
  } else {
    showToast({ message: result.message, severity: 'error' })
  }
}

const cancelDelete = () => {
  showDeleteConfirmation.value = false
  categoryToDelete.value = null
}
</script>

<style lang="scss" scoped>
.categories-table {
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

.action-container {
  display: flex;
  gap: 0.25rem;
  flex-wrap: wrap;
}
</style>
