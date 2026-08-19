<template>
  <OnmsAppLayout content-layout="full" :class="{ 'app-layout-bounded': fillsLayoutCell }">
    <template v-slot:header>
      <Menubar />
      <SideMenu
        pushedSelector=".app-layout"
      />
    </template>

    <div class="main-content" :class="{ 'main-content-fill': fillsLayoutCell }">
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
import { useRoute } from 'vue-router'

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

// Pages that must sit exactly inside the layout's main cell rather than run as
// long as their content: the topology canvas sizes itself to the space it is
// given. Opt-in per route, so no other page's block layout changes.
const FILL_LAYOUT_CELL_ROUTES = new Set(['Topology'])

const route = useRoute()
const fillsLayoutCell = computed<boolean>(() =>
  FILL_LAYOUT_CELL_ROUTES.has(String(route.name ?? '')))

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
// Bound the layout to the viewport for pages that fill their cell. .app-layout
// is min-height:100vh, which is a floor and not a ceiling: a tall child grows
// the grid row, the layout, and pushes the footer off the bottom. A fixed height
// makes the middle row a real ceiling, so the page inside it has to scroll its
// own content rather than expand.
.app-layout.app-layout-bounded {
  height: 100vh;
}

// Per route, not app-wide. onms-base.scss resets this under .onms-styles, but
// that class sits on #app, so body keeps its 8px margin and the page overflows
// by 16px. Putting the class on <body> also restyled every other page's gutters
// and every teleported PrimeVue overlay's typography.
body:has(.app-layout-bounded) {
  margin: 0;
}

// Pass that bounded row height down to the page, instead of the page guessing it
// from 100vh minus an allowance that has to match the footer's rendered height.
.main-content.main-content-fill {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
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
