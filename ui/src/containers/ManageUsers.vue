<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="manage-users-container">
    <h1 class="page-title">Manage Users</h1>
    <UsersHelpPanel />
    <UsersTable />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'

import UsersHelpPanel from '@/components/ManageUsers/UsersHelpPanel.vue'
import UsersTable from '@/components/ManageUsers/UsersTable.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useUserAdminStore } from '@/stores/userAdminStore'
import { BreadCrumb } from '@/types'

const menuStore = useMenuStore()
const store = useUserAdminStore()

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Manage Users', to: '#', position: 'last' }
  ]
})

onMounted(async () => {
  await store.populate()
})
</script>

<style lang="scss" scoped>
.manage-users-container {
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
