
<template>
  <PluginTable :plugins="plugins" />
</template>

<script setup lang="ts">
import { Plugin } from '@/types'
import { computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import PluginTable from '@/components/Plugin/PluginTable.vue'

const store = useStore()
const plugins = computed<Plugin[]>(() => store.state.pluginModule.plugins)
onMounted(() => {
  store.dispatch('pluginModule/getPlugins')
  store.dispatch('pluginModule/getEnabledPlugins')
})
</script>
