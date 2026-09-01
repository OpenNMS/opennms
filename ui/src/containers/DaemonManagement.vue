<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="daemon-management-container">
    <h1 class="page-title">Daemon Management</h1>
    <DaemonManagementTable />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import DaemonManagementTable from '@/components/DaemonManagement/DaemonManagementTable.vue'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'

const menuStore = useMenuStore()

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Daemon Management', to: '#', position: 'last' }
  ]
})
</script>

<style lang="scss" scoped>
.daemon-management-container {
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
