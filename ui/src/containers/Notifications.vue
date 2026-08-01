<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="notifications-container">
    <div class="page-header">
      <h1 class="page-title">Notifications</h1>
      <OnmsIconButton
        v-if="adminRole"
        variant="text"
        :icon="SettingsIcon"
        title="Configure Notifications"
        aria-label="Configure Notifications"
        data-test="configure-notifications-button"
        @click="showConfigDialog = true"
      />
    </div>

    <NotificationExplanationsCard />
    <NotificationQueriesCard />
    <NoticesTable />

    <ConfigureNotificationsDialog
      v-if="adminRole"
      v-model:visible="showConfigDialog"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import { OnmsIconButton } from '@opennms/onms-ui'

import { whenever } from '@vueuse/core'

import ConfigureNotificationsDialog from '@/components/AdminNotifications/ConfigureNotificationsDialog.vue'
import NoticesTable from '@/components/AdminNotifications/NoticesTable.vue'
import NotificationExplanationsCard from '@/components/AdminNotifications/NotificationExplanationsCard.vue'
import NotificationQueriesCard from '@/components/AdminNotifications/NotificationQueriesCard.vue'
import SettingsIcon from '@/components/icons/action/Settings.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import useRole from '@/composables/useRole'
import { useAuthStore } from '@/stores/authStore'
import { useMenuStore } from '@/stores/menuStore'
import { useNoticesStore } from '@/stores/noticesStore'
import { BreadCrumb } from '@/types'

const authStore = useAuthStore()
const menuStore = useMenuStore()
const noticesStore = useNoticesStore()
const { adminRole } = useRole()

const showConfigDialog = ref(false)

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Notifications', to: '#', position: 'last' }
  ]
})

// The default preset filters by the logged-in user; whoAmI is fetched
// fire-and-forget at app startup, so wait for it or the first query would
// silently drop the user filter and show every user's notices.
const authLoaded = computed<boolean>(() => authStore.loaded)

onMounted(() => {
  if (authLoaded.value) {
    noticesStore.load()
  } else {
    whenever(authLoaded, () => noticesStore.load(), { once: true })
  }
})
</script>

<style lang="scss" scoped>
.notifications-container {
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
