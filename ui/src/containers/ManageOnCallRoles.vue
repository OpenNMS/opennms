<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="manage-oncall-roles-container">
    <h1 class="page-title">Manage On-Call Roles</h1>
    <!-- hidden, not removed, while the editor is open: the table keeps its filter, page and sort -->
    <OnCallRolesTable v-show="store.editMode === AdminEditMode.Table" />
    <OnCallRoleEditorPanel
      v-if="store.editMode !== AdminEditMode.Table"
      :role="store.currentRole"
      @close="store.closeRoleEditor()"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import OnCallRoleEditorPanel from '@/components/OnCallRoles/OnCallRoleEditorPanel.vue'
import OnCallRolesTable from '@/components/OnCallRoles/OnCallRolesTable.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { useOnCallRoleAdminStore } from '@/stores/onCallRoleAdminStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'
import { AdminEditMode } from '@/types/adminWrite'

const menuStore = useMenuStore()
const { withSpinner, report } = useActionFeedback()
const store = useOnCallRoleAdminStore()

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Manage On-Call Roles', to: '#', position: 'last' }
  ]
})

onMounted(async () => {
  report(await withSpinner(() => store.populate()))
})

// the editor panel replaces the table on this route; leaving the page closes it
onUnmounted(() => {
  store.closeRoleEditor()
})
</script>

<style lang="scss" scoped>
.manage-oncall-roles-container {
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
