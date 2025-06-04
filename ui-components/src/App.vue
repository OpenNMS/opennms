<template>
  <Menubar />
  <SideMenu />
</template>

<script setup lang="ts">

import { onMounted } from 'vue'
import Menubar from './components/Menubar.vue'
import SideMenu from './components/SideMenu.vue'

import { useMenuStore } from '@/stores/menuStore'

const menuStore = useMenuStore()

onMounted(() => {
  menuStore.getMainMenu()
  menuStore.getNotificationSummary()
})
</script>

<style scoped>
</style>
