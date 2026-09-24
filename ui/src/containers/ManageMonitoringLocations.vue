<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="manage-locations-container">
    <h1 class="page-title">Manage Monitoring Locations</h1>
    <LocationsTable />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'

import { useOnmsToast } from '@opennms/onms-ui'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
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

const { showToast } = useOnmsToast()

onMounted(async () => {
  if (!(await store.getLocations())) {
    showToast({ message: 'Failed to load monitoring locations.', severity: 'error' })
  }
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
