<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="manage-locations-container">
    <h1 class="page-title">Manage Monitoring Locations</h1>
    <LocationsHelpPanel />
    <LocationsTable />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import LocationsHelpPanel from '@/components/ManageMonitoringLocations/LocationsHelpPanel.vue'
import LocationsTable from '@/components/ManageMonitoringLocations/LocationsTable.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useMonitoringLocationAdminStore } from '@/stores/monitoringLocationAdminStore'
import { BreadCrumb } from '@/types'

const menuStore = useMenuStore()
const store = useMonitoringLocationAdminStore()

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Manage Monitoring Locations', to: '#', position: 'last' }
  ]
})

onMounted(async () => {
  await store.getLocations()
})
</script>

<style lang="scss" scoped>
.manage-locations-container {
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
