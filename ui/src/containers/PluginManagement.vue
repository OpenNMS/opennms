<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="plugin-management-container">
    <h1 class="page-title">Plugin Management</h1>

    <div v-if="store.state && !store.containerAvailable" class="callout error-callout" role="alert" data-test="container-unavailable">
      <strong>The plugin container is not available.</strong>
      <span>Plugins cannot be loaded or unloaded until the Karaf container is running; check that OpenNMS has started completely, then reload the page.</span>
    </div>

    <div v-if="store.restartRequired" class="callout warn-callout" role="status" data-test="restart-banner">
      <span>A restart is required to finish loading or unloading plugins.</span>
      <OnmsButton variant="outlined" label="Show restart instructions" data-test="show-restart-instructions" @click="showRestartDialog = true" />
    </div>

    <PluginManagementAbout :restartInstructions="store.restartInstructions" :deployDir="store.state?.deployDir" />

    <p v-if="store.loadError" class="error" data-test="load-error">
      Failed to read the plugin list. Check that the server is up and that you are still logged in, then reload the page.
    </p>

    <PluginLoadCard :disabled="!store.containerAvailable" />

    <PluginsTable :plugins="store.plugins" :containerAvailable="store.containerAvailable" @unload="askUnload" />

    <PluginActivityLog />

    <RestartInstructionsDialog v-model:visible="showRestartDialog" :instructions="store.restartInstructions" />
    <PluginUnloadDialog v-model:visible="showUnloadDialog" :plugin="pluginToUnload" @unloaded="onUnloaded" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { OnmsButton, useOnmsToast } from '@opennms/onms-ui'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import PluginActivityLog from '@/components/PluginManagement/PluginActivityLog.vue'
import PluginLoadCard from '@/components/PluginManagement/PluginLoadCard.vue'
import PluginManagementAbout from '@/components/PluginManagement/PluginManagementAbout.vue'
import PluginsTable from '@/components/PluginManagement/PluginsTable.vue'
import PluginUnloadDialog from '@/components/PluginManagement/PluginUnloadDialog.vue'
import RestartInstructionsDialog from '@/components/PluginManagement/RestartInstructionsDialog.vue'
import { useMenuStore } from '@/stores/menuStore'
import { usePluginManagementStore } from '@/stores/pluginManagementStore'
import { BreadCrumb } from '@/types'
import { PluginEntry } from '@/types/pluginManagement'

const menuStore = useMenuStore()
const store = usePluginManagementStore()
const { showToast } = useOnmsToast()

const showRestartDialog = ref(false)
const showUnloadDialog = ref(false)
const pluginToUnload = ref<PluginEntry | null>(null)

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => [
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Plugin Management', to: '#', position: 'last' }
])

onMounted(async () => {
  await Promise.all([store.load(), store.getRestartInstructions(), store.refreshLog()])
})

const askUnload = (plugin: PluginEntry) => {
  pluginToUnload.value = plugin
  showUnloadDialog.value = true
}

const onUnloaded = (plugin: PluginEntry) => {
  showToast({ message: `Plugin ${plugin.karName} unloaded. Restart OpenNMS to finish removing it.`, severity: 'success' })
}
</script>

<style lang="scss" scoped>
.plugin-management-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 0 2px 2rem 2px;
}

.page-title {
  font-size: 1.4rem;
  font-weight: 600;
  margin: 0;
}

.callout {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 1rem;
  padding: 0.75rem 1rem;
  border-radius: 6px;
  font-size: 0.9rem;
}

.warn-callout {
  border-left: 4px solid var(--p-orange-500, #ef6c00);
  background: color-mix(in srgb, var(--p-orange-500, #ef6c00) 12%, transparent);
}

.error-callout {
  flex-direction: column;
  align-items: flex-start;
  gap: 0.25rem;
  border-left: 4px solid var(--p-red-500, #c62828);
  background: color-mix(in srgb, var(--p-red-500, #c62828) 10%, transparent);
}

.error {
  color: var(--p-red-500, #c62828);
  margin: 0;
}
</style>
