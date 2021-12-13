<template>
  <FeatherNavigationRail>
    <FeatherRailItem
      :class="{ selected: isSelected('/') }"
      slot="main"
      href="#/"
      :icon="Instances"
      title="Nodes"
    />
    <FeatherRailItem
      :class="{ selected: isSelected('/file-editor') }"
      v-if="isAdmin"
      slot="main"
      href="#/file-editor"
      :icon="AddNote"
      title="File Editor"
    />
    <FeatherRailItem
      :class="{ selected: isSelected('/logs') }"
      v-if="isAdmin"
      slot="main"
      href="#/logs"
      :icon="MarkComplete"
      title="Logs"
    />
  </FeatherNavigationRail>
</template>
<script setup lang=ts>
import { computed } from 'vue'
import { useStore } from 'vuex'
import { useRoute } from 'vue-router'
import Instances from "@featherds/icon/hardware/Instances"
import AddNote from "@featherds/icon/action/AddNote"
import MarkComplete from "@featherds/icon/action/MarkComplete"
import {
  FeatherNavigationRail,
  FeatherRailItem,
} from "@featherds/navigation-rail"

const store = useStore()
const route = useRoute()
const isAdmin = computed(() => store.getters['authModule/isAdmin'])
const isSelected = (path: string) => path === route.fullPath
</script>

<style scopes lang="scss">
.nav-header {
  display: none !important;
}
</style>
