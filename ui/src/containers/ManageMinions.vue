<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="manage-minions-container">
    <h1 class="page-title">Manage Minions</h1>
    <MinionsTable />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'

import { useOnmsToast } from '@opennms/onms-ui'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import MinionsTable from '@/components/ManageMinions/MinionsTable.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useMinionAdminStore } from '@/stores/minionAdminStore'
import { BreadCrumb } from '@/types'

const menuStore = useMenuStore()
const store = useMinionAdminStore()

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Manage Minions', to: '#', position: 'last' }
  ]
})

const { showToast } = useOnmsToast()

onMounted(async () => {
  if (!(await store.getMinions())) {
    showToast({ message: 'Failed to load minions.', severity: 'error' })
  }
})
</script>

<style lang="scss" scoped>
.manage-minions-container {
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
