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
import { onMounted } from 'vue'

import { OnmsToastHost } from '@opennms/onms-ui'
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

// `overflow-x: hidden` makes <html> itself the SPA's scroll container: the
// other axis can no longer compute to `visible`, so the body's overflow is not
// propagated to the viewport. That is why PrimeVue's `blockScroll` — which only
// adds `.p-overflow-hidden` (overflow: hidden) to <body> — cannot stop the page
// scrolling on its own here, and the wheel over an open drawer/dialog kept
// scrolling the content behind it. Lock the real scroller instead, keyed off the
// same body class so it tracks PrimeVue's own lock state (NMS-20182).
//
// `scrollbar-gutter: stable` reserves the scrollbar's width permanently. Without
// it, locking removes the page scrollbar and the fixed chrome (Menubar, side
// rail) jumps ~15px wider; PrimeVue's `padding-right` compensation only shifts
// in-flow content, never fixed elements.
html {
  overflow-x: hidden;
  scrollbar-gutter: stable;
}
// The `.p-overlay-mask` arm covers nesting: PrimeVue's block/unblock is not
// reference-counted, so closing a dialog opened *inside* an open drawer strips
// the body class and unlocked the page while the drawer was still up. The mask
// element exists for exactly as long as a modal overlay is open, so matching it
// keeps the lock until the last one closes.
html:has(body.p-overflow-hidden),
html:has(.p-overlay-mask) {
  overflow: hidden;
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
