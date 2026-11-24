<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="manage-applications-container">
    <div class="page-header">
      <div>
        <div class="title-row">
          <h1 class="page-title">Manage Applications</h1>
          <AboutDialog />
        </div>
        <p class="page-subtitle">Group monitored services into applications and choose where the perspective poller checks them from.</p>
      </div>
    </div>
    <ApplicationsTable />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import AboutDialog from '@/components/ManageApplications/AboutDialog.vue'
import ApplicationsTable from '@/components/ManageApplications/ApplicationsTable.vue'
import { useApplicationAdminStore } from '@/stores/applicationAdminStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'

const menuStore = useMenuStore()
const store = useApplicationAdminStore()

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => [
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Manage Applications', to: '#', position: 'last' }
])

onMounted(() => {
  store.getApplications()
})
</script>

<style lang="scss" scoped>
.manage-applications-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 0 2px 2rem 2px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  flex-wrap: wrap;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.page-title {
  font-size: 1.4rem;
  font-weight: 600;
  margin: 0;
}

.page-subtitle {
  margin: 0.25rem 0 0 0;
  color: var(--p-text-muted-color);
}
</style>
