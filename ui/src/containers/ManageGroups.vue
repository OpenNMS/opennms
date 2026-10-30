<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="manage-groups-container">
    <h1 class="page-title">Manage Groups</h1>
    <!-- hidden, not removed, while the editor is open: the table keeps its filter, page and sort -->
    <GroupsTable v-show="store.editMode === AdminEditMode.Table" />
    <GroupEditorPanel
      v-if="store.editMode !== AdminEditMode.Table"
      :group="store.currentGroup"
      @close="store.closeGroupEditor()"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import GroupEditorPanel from '@/components/ManageGroups/GroupEditorPanel.vue'
import GroupsTable from '@/components/ManageGroups/GroupsTable.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { useGroupAdminStore } from '@/stores/groupAdminStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'
import { AdminEditMode } from '@/types/adminWrite'

const menuStore = useMenuStore()
const { withSpinner, report } = useActionFeedback()
const store = useGroupAdminStore()

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Manage Groups', to: '#', position: 'last' }
  ]
})

onMounted(async () => {
  report(await withSpinner(() => store.populate()))
})

// the editor panel replaces the table on this route; leaving the page closes it
onUnmounted(() => {
  store.closeGroupEditor()
})
</script>

<style lang="scss" scoped>
.manage-groups-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 0 2px 2rem 2px;
}

.page-title {
  font-size: 1.4rem;
  font-weight: 600;
  margin: 0;
}
</style>
