<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="manage-categories-container">
    <h1 class="page-title">Surveillance Categories</h1>
    <CategoriesTable />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'

import { useOnmsToast } from '@opennms/onms-ui'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import CategoriesTable from '@/components/ManageCategories/CategoriesTable.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useCategoryAdminStore } from '@/stores/categoryAdminStore'
import { BreadCrumb } from '@/types'

const menuStore = useMenuStore()
const store = useCategoryAdminStore()

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Surveillance Categories', to: '#', position: 'last' }
  ]
})

const { showToast } = useOnmsToast()

onMounted(async () => {
  if (!(await store.getCategories())) {
    showToast({ message: 'Failed to load surveillance categories.', severity: 'error' })
  }
})
</script>

<style lang="scss" scoped>
.manage-categories-container {
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
