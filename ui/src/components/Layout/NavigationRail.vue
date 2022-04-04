<template>
  <FeatherNavigationRail @update:modelValue="onNavRailClick">
    <template v-slot:main>
      <FeatherRailItem
        :class="{ selected: isSelected('/') }"
        href="#/"
        :icon="Instances"
        title="Nodes"
      />
      <FeatherRailItem
        :class="{ selected: isSelected('/map') }"
        href="#/map"
        :icon="Location"
        title="Map"
      />
      <FeatherRailItem
        :class="{ selected: isSelected('/configuration') }"
        href="#/configuration"
        :icon="LoggerConfigs"
        title="Configuration"
      />
      <FeatherRailItem
        :class="{ selected: isSelected('/file-editor') }"
        v-if="isAdmin"
        href="#/file-editor"
        :icon="AddNote"
        title="File Editor"
      />
      <FeatherRailItem
        :class="{ selected: isSelected('/logs') }"
        v-if="isAdmin"
        href="#/logs"
        :icon="MarkComplete"
        title="Logs"
      />
      <FeatherRailItem
        :class="{ selected: isSelected('/open-api') }"
        href="#/open-api"
        :icon="Cloud"
        title="Endpoints"
      />
      <FeatherRailItem
        :class="{ selected: isSelected('/resource-graphs') }"
        href="#/resource-graphs"
        :icon="Reporting"
        title="Resource Graphs"
      />

      <!-- loop plugin menu items -->
      <FeatherRailItem
        v-for="plugin of plugins"
        :key="plugin.extensionId"
        :class="{ selected: isSelected(`/plugins/${plugin.extensionId}/${plugin.resourceRootPath}/${plugin.moduleFileName}`) }"
        :href="`#/plugins/${plugin.extensionId}/${plugin.resourceRootPath}/${plugin.moduleFileName}`"
        :title="plugin.menuEntry"
        :icon="UpdateUtilities"
      />
    </template>
  </FeatherNavigationRail>
</template>
<script setup lang=ts>
import { useStore } from 'vuex'
import Instances from '@featherds/icon/hardware/Instances'
import AddNote from '@featherds/icon/action/AddNote'
import LoggerConfigs from '@featherds/icon/action/LoggerConfigs'
import Location from '@featherds/icon/action/Location'
import MarkComplete from '@featherds/icon/action/MarkComplete'
import Cloud from '@featherds/icon/action/Cloud'
import Reporting from '@featherds/icon/action/Reporting'
import UpdateUtilities from '@featherds/icon/action/UpdateUtilities'
import {
  FeatherNavigationRail,
  FeatherRailItem,
} from '@featherds/navigation-rail'
import { Plugin } from '@/types'

const store = useStore()
const route = useRoute()
const plugins = computed<Plugin[]>(() => store.state.pluginModule.plugins)
const isAdmin = computed(() => store.getters['authModule/isAdmin'])
const navRailOpen = computed(() => store.state.appModule.navRailOpen)
const onNavRailClick = () => store.dispatch('appModule/setNavRailOpen', !navRailOpen.value)
const isSelected = (path: string) => path === route.fullPath
</script>

<style scopes lang="scss">
.nav-header {
  display: none !important;
}
</style>
