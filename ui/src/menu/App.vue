<template>
  <Menubar />
  <SideMenu />
</template>

<script setup lang="ts">

import { onMounted } from 'vue'
import Menubar from '@/components/Menu/Menubar.vue'
import SideMenu from '@/components/Menu/SideMenu.vue'

import { useMenuStore } from '@/stores/menuStore'

const menuStore = useMenuStore()

onMounted(() => {
  menuStore.getMainMenu()
  menuStore.getNotificationSummary()
})
</script>

<style scoped>
</style>
