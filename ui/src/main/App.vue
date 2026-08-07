<template>
  <OnmsAppLayout content-layout="full">
    <template v-slot:header>
      <Menubar />
      <SideMenu
        pushedSelector=".app-layout"
      />
    </template>

    <div class="main-content">
      <Spinner />
      <OnmsToastHost />
      <router-view v-slot="{ Component }">
        <keep-alive include="MapKeepAlive">
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </div>
    <template v-slot:footer>
      <Footer />
    </template>
  </OnmsAppLayout>
</template>

<script
  setup
  lang="ts"
>
import { computed, onMounted } from 'vue'
import { whenever } from '@vueuse/core'

import { OnmsToastHost } from '@opennms/onms-ui'
import startBrowserNotifications from '@/composables/useBrowserNotifications'
import OnmsAppLayout from '@/components/Layout/OnmsAppLayout.vue'
import Footer from '@/components/Layout/Footer.vue'
import Menubar from '@/components/Menu/Menubar.vue'
import SideMenu from '@/components/Menu/SideMenu.vue'
import Spinner from '@/components/Common/Spinner.vue'
import { useAuthStore } from '@/stores/authStore'
import { useInfoStore } from '@/stores/infoStore'
import { usePluginStore } from '@/stores/pluginStore'
import { useMenuStore } from '@/stores/menuStore'
import { useMonitoringSystemStore } from '@/stores/monitoringSystemStore'
import { useNodeListStore } from '@/stores/nodeListStore'

const authStore = useAuthStore()
const infoStore = useInfoStore()
const menuStore = useMenuStore()
const monitoringSystemStore = useMonitoringSystemStore()
const nodeListStore = useNodeListStore()
const pluginStore = usePluginStore()

// notifd 'browser' method delivery for /ui pages; needs baseHref from the menu
const baseHref = computed<string>(() => menuStore.mainMenu?.baseHref ?? '')
whenever(() => !!baseHref.value, () => startBrowserNotifications(baseHref.value), { once: true })

onMounted(() => {
  authStore.getWhoAmI()
  infoStore.getInfo()
  menuStore.getMainMenu()
  menuStore.getNotificationSummary()
  menuStore.loadSideMenuExpanded()
  monitoringSystemStore.getMainMonitoringSystem()
  nodeListStore.getCategories()
  nodeListStore.getMonitoringLocations()
  nodeListStore.getServiceTypes()
  pluginStore.getPlugins()
})
</script>

<style lang="scss">
@import '@/styles/onms-typography';
@import "@/styles/onms-tokens";

html {
  overflow-x: hidden;
}
// Offsets for the SPA content, clearing the two fixed chrome elements:
// - padding-top clears the fixed top menu bar (Menubar, --onms-header-height).
// - padding-left clears the fixed side-menu rail. SideMenu's applyPush only
//   overrides padding-left (inline) while the rail is pinned-expanded; otherwise
//   this base applies.
.app-layout {
  padding-top: var(--onms-header-height, 3.75rem);
  padding-left: calc(var(--onms-header-height, 3.75rem) + 0.25rem);
}
.main-content {
  table {
    width: 100%;
  }
}
a {
  text-decoration: none;
  color: var($clickable-normal);
}
.pointer {
  cursor: pointer !important;
}

// global feather typography classes
.headline3 {
  @include onms-headline3;
}
.headline4 {
  @include onms-headline4;
}
.subtitle1 {
  @include onms-subtitle1;
}
.subtitle2 {
  @include onms-subtitle2;
}
</style>
