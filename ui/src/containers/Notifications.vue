<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="notifications-container">
    <div class="page-header">
      <div class="title-with-help">
        <h1 class="page-title">Notifications</h1>
        <OnmsIconButton
          variant="text"
          :icon="InfoIcon"
          title="About Notices and Escalation"
          aria-label="About Notices and Escalation"
          data-test="notifications-about-button"
          @click="showAboutDialog = true"
        />
      </div>
      <OnmsButton
        v-if="adminRole"
        variant="outlined"
        label="Configure Notifications"
        icon="pi pi-plus"
        data-test="configure-notifications-button"
        @click="showConfigDialog = true"
      />
    </div>

    <NotificationQueriesCard />
    <NoticesTable />

    <NotificationExplanationsCard v-model:visible="showAboutDialog" />

    <ConfigureNotificationsDialog
      v-if="adminRole"
      v-model:visible="showConfigDialog"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import { OnmsButton, OnmsIconButton } from '@opennms/onms-ui'

import { whenever } from '@vueuse/core'

import ConfigureNotificationsDialog from '@/components/AdminNotifications/ConfigureNotificationsDialog.vue'
import NoticesTable from '@/components/AdminNotifications/NoticesTable.vue'
import NotificationExplanationsCard from '@/components/AdminNotifications/NotificationExplanationsCard.vue'
import NotificationQueriesCard from '@/components/AdminNotifications/NotificationQueriesCard.vue'
import InfoIcon from '@/components/icons/action/Info.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import useRole from '@/composables/useRole'
import { useAuthStore } from '@/stores/authStore'
import { useMenuStore } from '@/stores/menuStore'
import { useNoticesStore } from '@/stores/noticesStore'
import { BreadCrumb } from '@/types'
import { NoticeQueryPreset } from '@/types/notices'

const authStore = useAuthStore()
const route = useRoute()
const menuStore = useMenuStore()
const noticesStore = useNoticesStore()
const { adminRole } = useRole()

const showConfigDialog = ref(false)
const showAboutDialog = ref(false)

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

// the top-bar bell deep-links here with a preset (NMS-20124)
const PRESETS = ['yourOutstanding', 'teamOutstanding', 'allOutstanding', 'allAcknowledged'] as const
const initialLoad = () => {
  const preset = route.query.preset as string
  if ((PRESETS as readonly string[]).includes(preset)) {
    noticesStore.applyPreset(preset as NoticeQueryPreset)
  } else {
    noticesStore.load()
  }
}

onMounted(() => {
  if (authLoaded.value) {
    initialLoad()
  } else {
    whenever(authLoaded, () => initialLoad(), { once: true })
  }
})

// Clicking a bell link while already on this page only changes the hash query,
// so the component is not remounted — re-apply the preset when it changes.
watch(() => route.query.preset, () => {
  if (authLoaded.value) {
    initialLoad()
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

  .title-with-help {
    display: flex;
    align-items: center;
    gap: 0.25rem;
  }

  .page-title {
    font-size: 1.4rem;
    font-weight: 600;
    margin: 0;
  }
}

</style>
