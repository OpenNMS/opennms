<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="manage-roles-container">
    <h1 class="page-title">Manage On-Call Roles</h1>
    <RolesHelpPanel />
    <RolesTable />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import RolesHelpPanel from '@/components/ManageOnCallRoles/RolesHelpPanel.vue'
import RolesTable from '@/components/ManageOnCallRoles/RolesTable.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useOnCallRoleAdminStore } from '@/stores/onCallRoleAdminStore'
import { BreadCrumb } from '@/types'

const menuStore = useMenuStore()
const store = useOnCallRoleAdminStore()

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Manage On-Call Roles', to: '#', position: 'last' }
  ]
})

onMounted(async () => {
  await store.populate()
})
</script>

<style lang="scss" scoped>
.manage-roles-container {
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
