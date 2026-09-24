<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="notifications-config-container">
    <div class="page-header">
      <h1 class="page-title">Configure Notifications</h1>
    </div>

    <NotificationsConfigTabs />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import NotificationsConfigTabs from '@/components/Notifications/NotificationsConfigTabs.vue'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'

const menuStore = useMenuStore()

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => [
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Notifications', to: '/admin/notifications' },
  { label: 'Configure Notifications', to: '#', position: 'last' }
])
</script>

<style lang="scss" scoped>
.notifications-config-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 0 2px 2rem 2px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .page-title {
    font-size: 1.4rem;
    font-weight: 600;
    margin: 0;
  }
}
</style>
