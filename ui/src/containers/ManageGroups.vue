<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="manage-groups-container">
    <h1 class="page-title">Manage Groups</h1>
    <GroupsHelpPanel />
    <GroupsTable />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import GroupsHelpPanel from '@/components/ManageGroups/GroupsHelpPanel.vue'
import GroupsTable from '@/components/ManageGroups/GroupsTable.vue'
import { useGroupAdminStore } from '@/stores/groupAdminStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'

const menuStore = useMenuStore()
const store = useGroupAdminStore()

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Manage Groups', to: '#', position: 'last' }
  ]
})

onMounted(async () => {
  await store.populate()
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
