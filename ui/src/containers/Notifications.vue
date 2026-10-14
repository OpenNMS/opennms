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
          title="About Notifications and Escalation"
          tooltip="About Notifications and Escalation"
          aria-label="About Notifications and Escalation"
          data-test="notifications-about-button"
          @click="showAboutDialog = true"
        />
      </div>
      <OnmsButton
        v-if="adminRole"
        variant="outlined"
        label="Configure Notifications"
        data-test="configure-notifications-button"
        @click="router.push('/notifications-config')"
      />
    </div>

    <NotificationQueriesCard />
    <NotificationsTable />

    <NotificationsHelpDialog v-model:visible="showAboutDialog" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { OnmsButton, OnmsIconButton } from '@opennms/onms-ui'

import { useEventListener, whenever } from '@vueuse/core'

import NotificationsTable from '@/components/Notifications/NotificationsTable.vue'
import NotificationsHelpDialog from '@/components/Notifications/NotificationsHelpDialog.vue'
import NotificationQueriesCard from '@/components/Notifications/NotificationQueriesCard.vue'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import useRole from '@/composables/useRole'
import { SAME_PAGE_NAVIGATION_EVENT } from '@/lib/navigation'
import { useAuthStore } from '@/stores/authStore'
import { useMenuStore } from '@/stores/menuStore'
import { useNotificationsStore } from '@/stores/notificationsStore'
import { BreadCrumb } from '@/types'
import { NotificationQueryPreset } from '@/types/notifications'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()
const menuStore = useMenuStore()
const notificationsStore = useNotificationsStore()
const { adminRole } = useRole()
const { withSpinner, report } = useActionFeedback()

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
// silently drop the user filter and show every user's notifications.
const authLoaded = computed<boolean>(() => authStore.loaded)

// Presets a URL can name: the top-bar bell (NMS-20124), Node Details and the dashboard link here.
// userSearch is not one of them; it is typed on the page.
const LINKABLE_PRESETS: readonly NotificationQueryPreset[] = ['yourOutstanding', 'yourAcknowledged', 'teamOutstanding', 'allOutstanding', 'allAcknowledged']

const queryFor = (preset: NotificationQueryPreset, nodeId: string | null | undefined): Record<string, string> => ({
  ...(preset !== 'userSearch' ? { preset } : {}),
  ...(nodeId ? { node: nodeId } : {})
})

const sameQuery = (a: Record<string, unknown>, b: Record<string, unknown>) =>
  JSON.stringify(Object.entries(a).sort()) === JSON.stringify(Object.entries(b).sort())

// preset is null when the URL names no valid preset, so the page can keep the view it has.
const readRoute = () => {
  const preset = route.query.preset as string
  const node = route.query.node as string
  return {
    preset: (LINKABLE_PRESETS as readonly string[]).includes(preset) ? preset as NotificationQueryPreset : null,
    nodeId: typeof node === 'string' && /^\d+$/.test(node) && Number(node) > 0 ? String(Number(node)) : null
  }
}

// A link with a preset applies exactly what it says. Without one (a bare #/notifications, a menu
// re-click) the current view stays: a user search is never in the URL, so applying the default
// preset would silently drop it.
const loadFromRoute = async () => {
  const { preset, nodeId } = readRoute()
  if (preset) {
    report(await withSpinner(() => notificationsStore.applyQuery(preset, nodeId)))
  } else if (nodeId) {
    report(await withSpinner(() => notificationsStore.setNodeFilter(nodeId)))
  } else {
    report(await withSpinner(() => notificationsStore.load()))
  }
}

onMounted(() => {
  if (authLoaded.value) {
    loadFromRoute()
  } else {
    whenever(authLoaded, () => loadFromRoute(), { once: true })
  }
})

// Another link changing the hash query does not remount the page. The page's own router.replace
// (below) also lands here; its query already matches the store, so it is skipped rather than
// loaded a second time.
watch(() => route.query, (query) => {
  if (!authLoaded.value) {
    return
  }
  if (sameQuery(query, queryFor(notificationsStore.preset, notificationsStore.nodeFilter?.id))) {
    return
  }
  loadFromRoute()
})

// Presets chosen and the node chip removed on the page go into the URL, so refresh and bookmarks
// keep the view.
watch(() => [notificationsStore.preset, notificationsStore.nodeFilter?.id] as const, ([preset, nodeId]) => {
  const query = queryFor(preset, nodeId)
  if (!sameQuery(route.query, query)) {
    router.replace({ query })
  }
})

// ...and re-clicking the link for the view already showing leaves the hash unchanged, so neither
// watch fires; the menu signals it instead.
useEventListener(window, SAME_PAGE_NAVIGATION_EVENT, () => {
  if (authLoaded.value) {
    loadFromRoute()
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
