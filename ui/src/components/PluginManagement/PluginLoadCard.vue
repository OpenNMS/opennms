<template>
  <OnmsCard class="plugin-load-card" data-test="plugin-load-card">
    <template #title>
      <div class="card-header">
        <span class="card-title">Load a plugin</span>
        <AboutDialogButton title="Plugin management">
          <PluginManagementAbout :restartInstructions="store.restartInstructions ?? null" :deployDir="store.state?.deployDir" />
        </AboutDialogButton>
      </div>
    </template>
    <template #content>
      <OnmsTabs v-model:value="activeTab">
        <OnmsTabList>
          <OnmsTab value="repository" data-test="load-tab-repository">From a repository</OnmsTab>
          <OnmsTab value="file" data-test="load-tab-file">From a file</OnmsTab>
        </OnmsTabList>
        <OnmsTabPanels>
          <OnmsTabPanel value="repository">
            <PluginRepositoryLoad :disabled="disabled" @checked="onChecked" @reset="clearResult" />
          </OnmsTabPanel>
          <OnmsTabPanel value="file">
            <PluginFileLoad ref="fileLoad" :disabled="disabled" @checked="onChecked" @reset="clearResult" />
          </OnmsTabPanel>
        </OnmsTabPanels>
      </OnmsTabs>
      <PluginCheckResult :inspection="inspection" :source="source" :disabled="disabled" @loaded="onLoaded" />
    </template>
  </OnmsCard>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { OnmsCard, OnmsTab, OnmsTabList, OnmsTabPanel, OnmsTabPanels, OnmsTabs } from '@opennms/onms-ui'

import AboutDialogButton from '@/components/Common/AboutDialogButton.vue'
import { usePluginManagementStore } from '@/stores/pluginManagementStore'
import { KarInspection } from '@/types/pluginManagement'
import PluginCheckResult from './PluginCheckResult.vue'
import PluginFileLoad from './PluginFileLoad.vue'
import PluginManagementAbout from './PluginManagementAbout.vue'
import PluginRepositoryLoad from './PluginRepositoryLoad.vue'

defineProps<{
  // the container is unavailable: nothing can be checked or loaded
  disabled: boolean
}>()

const store = usePluginManagementStore()

const activeTab = ref<string | number>('repository')
const fileLoad = ref<InstanceType<typeof PluginFileLoad> | null>(null)
// the one KAR waiting on the server, whichever tab produced it
const inspection = ref<KarInspection | null>(null)
const source = ref('')

const clearResult = () => {
  inspection.value = null
  source.value = ''
}

const onChecked = (checked: KarInspection, sourceLine: string) => {
  inspection.value = checked
  source.value = sourceLine
}

const onLoaded = () => {
  clearResult()
  fileLoad.value?.clear()
}
</script>

<style lang="scss" scoped>
.plugin-load-card {
  padding: 25px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.card-title {
  font-size: 1.1rem;
  font-weight: 600;
}
</style>
