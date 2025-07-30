<template>
  <FeatherAppLayout content-layout="full">
    <template v-slot:header>
      <Menubar />
      <SideMenu
        pushedSelector=".app-layout"
      />
    </template>

    <div class="main-content">
      <Spinner />
      <Snackbar />
      <router-view v-slot="{ Component }">
        <keep-alive include="MapKeepAlive">
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </div>
    <template v-slot:footer>
      <Footer />
    </template>
  </FeatherAppLayout>
</template>

<script
  setup
  lang="ts"
>
import { FeatherAppLayout } from '@featherds/app-layout'
import Footer from '@/components/Layout/Footer.vue'
import Menubar from '@/components/Menu/Menubar.vue'
import SideMenu from '@/components/Menu/SideMenu.vue'
import Spinner from '@/components/Common/Spinner.vue'
import Snackbar from '@/components/Common/Snackbar.vue'
import { useAuthStore } from '@/stores/authStore'
import { useInfoStore } from '@/stores/infoStore'
import { usePluginStore } from '@/stores/pluginStore'
import { useMenuStore } from '@/stores/menuStore'
import { useMonitoringSystemStore } from '@/stores/monitoringSystemStore'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'

const authStore = useAuthStore()
const infoStore = useInfoStore()
const menuStore = useMenuStore()
const monitoringSystemStore = useMonitoringSystemStore()
const nodeStructureStore = useNodeStructureStore()
const pluginStore = usePluginStore()

onMounted(() => {
  authStore.getWhoAmI()
  infoStore.getInfo()
  menuStore.getMainMenu()
  menuStore.getNotificationSummary()
  menuStore.loadSideMenuExpanded()
  monitoringSystemStore.getMainMonitoringSystem()
  nodeStructureStore.getCategories()
  nodeStructureStore.getMonitoringLocations()
  pluginStore.getPlugins()
})
</script>

<style lang="scss">
@import "@featherds/styles/lib/grid";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/open-mixins";
@import "@featherds/styles/themes/variables";

html {
  overflow-x: hidden;
}
.main-content {
  margin-left: 3.5rem;

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
  @include headline3;
}
.headline4 {
  @include headline4;
}
.subtitle1 {
  @include subtitle1;
}
.subtitle2 {
  @include subtitle2;
}
</style>
