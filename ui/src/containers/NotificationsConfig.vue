<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="notifications-config-container">
    <div class="page-header">
      <h1 class="page-title">Configure Notifications</h1>
      <OnmsButton
        variant="outlined"
        label="View Notifications"
        data-test="view-notifications-button"
        @click="router.push('/notifications')"
      />
    </div>

    <NotificationsConfigTabs />
  </div>
</template>

<script setup lang="ts">
import { computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

import { OnmsButton } from '@opennms/onms-ui'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import NotificationsConfigTabs from '@/components/Notifications/NotificationsConfigTabs.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useNotificationConfigStore } from '@/stores/notificationConfigStore'
import { BreadCrumb } from '@/types'

const menuStore = useMenuStore()
const router = useRouter()
const notificationConfigStore = useNotificationConfigStore()

// The editor state lives in the store so it survives switching tabs; leaving
// the page should land back on the table next time.
onUnmounted(() => notificationConfigStore.closeDestinationPathEditor())

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => [
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Notifications', to: '/notifications' },
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
