<template>
  <Menubar />
  <SideMenu
    pushedSelector="#content"
  />

</template>

<script setup lang="ts">

import { onMounted } from 'vue'
import Menubar from '@/components/Menu/Menubar.vue'
import SideMenu from '@/components/Menu/SideMenu.vue'

import { useMenuStore } from '@/stores/menuStore'
import { usePluginStore } from '@/stores/pluginStore'

const menuStore = useMenuStore()
const pluginStore = usePluginStore()

onMounted(() => {
  menuStore.getMainMenu()
  menuStore.getNotificationSummary()
  pluginStore.getPlugins()
})
</script>

<style scoped>
</style>
