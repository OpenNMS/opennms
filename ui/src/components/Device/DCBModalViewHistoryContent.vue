<template>
  <FeatherButton class="compare-btn" icon="Compare configs" @click="emit('onCompare')">
    <FeatherIcon :icon="Compare" />
  </FeatherButton>

  <FeatherButton class="dwnld-btn" icon="Download config" @click="onDownload">
    <FeatherIcon :icon="Download" />
  </FeatherButton>

  <div class="flex-container">
    <div class="history-dates-column" v-if="hasOnlyDefaultConfigs">
      <div
        v-date
        class="history-date pointer"
        :class="{ 'selected' : selectedConfig?.id === config.id }"
        v-for="config of defaultConfigTypeBackups"
        :key="config.id"
        @click="setSelectedConfig(config)"
      >{{ config.lastBackupDate }}</div>
    </div>

    <div v-if="selectedConfig && hasOnlyDefaultConfigs">
      <div class="config-display-container">
        <!-- We can use the diff editor display for a single config -->
        <DCBDiff :config1="selectedConfig" :config2="selectedConfig" :mode="'unified'" />
      </div>
    </div>

    <FeatherTabContainer class="tabs" v-if="!hasOnlyDefaultConfigs">
      <template v-slot:tabs>
        <FeatherTab @click="onTabClick('default')">Startup Configuration</FeatherTab>
        <FeatherTab @click="onTabClick('running')">Running Configuration</FeatherTab>
      </template>

      <!-- Startup config tab content -->
      <FeatherTabPanel>
        <div v-if="selectedConfig" class="flex">
          <div class="history-dates-column">
            <div
              v-date
              class="history-date pointer"
              :class="{ 'selected' : selectedConfig?.id === config.id }"
              v-for="config of defaultConfigTypeBackups"
              :key="config.id"
              @click="setSelectedConfig(config)"
            >{{ config.lastBackupDate }}</div>
          </div>
          <div class="config-display-container">
            <!-- We can use the diff editor display for a single config -->
            <DCBDiff :config1="selectedConfig" :config2="selectedConfig" :mode="'unified'" />
          </div>
        </div>
      </FeatherTabPanel>

      <!-- Running config tab content -->
      <FeatherTabPanel>
        <div v-if="selectedConfig" class="flex">
          <div class="history-dates-column">
            <div
              v-date
              class="history-date pointer"
              :class="{ 'selected' : selectedConfig?.id === config.id }"
              v-for="config of otherConfigTypeBackups"
              :key="config.id"
              @click="setSelectedConfig(config)"
            >{{ config.lastBackupDate }}</div>
          </div>
          <div class="config-display-container">
            <!-- We can use the diff editor display for a single config -->
            <DCBDiff :config1="selectedConfig" :config2="selectedConfig" :mode="'unified'" />
          </div>
        </div>
      </FeatherTabPanel>
    </FeatherTabContainer>
  </div>
</template>

<script setup lang="ts">
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'
import Download from '@featherds/icon/action/DownloadFile'
import Compare from '@featherds/icon/action/ContentCopy'
import { useStore } from 'vuex'
import { DeviceConfigBackup, defaultConfig, runningConfig } from '@/types/deviceConfig'
import DCBDiff from './DCBDiff.vue'

const emit = defineEmits(['onCompare'])

const store = useStore()
const selectedConfig = ref<DeviceConfigBackup>()

const modalDeviceConfigBackup = computed<DeviceConfigBackup>(() => store.state.deviceModule.modalDeviceConfigBackup)
const historyModalBackups = computed<DeviceConfigBackup[]>(() => store.state.deviceModule.historyModalBackups)
const defaultConfigTypeBackups = computed<DeviceConfigBackup[]>(() => historyModalBackups.value.filter((config) => config.configType === 'default'))
const otherConfigTypeBackups = computed<DeviceConfigBackup[]>(() => historyModalBackups.value.filter((config) => config.configType !== 'default'))
const hasOnlyDefaultConfigs = computed<boolean>(() => {
  return Boolean(modalDeviceConfigBackup.value.configType === 'default' ||
    (defaultConfigTypeBackups.value.length === historyModalBackups.value.length)
  )
})

const onDownload = () => store.dispatch('deviceModule/downloadByConfig', selectedConfig.value)
const getHistoryBackups = () => store.dispatch('deviceModule/getHistoryByIpInterface')
const setSelectedConfig = (config: DeviceConfigBackup) => selectedConfig.value = config
const onTabClick = (configType: defaultConfig | runningConfig) => {
  if (configType === 'default') {
    setSelectedConfig(defaultConfigTypeBackups.value[0])
  } else {
    setSelectedConfig(otherConfigTypeBackups.value[0])
  }
}

watch(defaultConfigTypeBackups, (defaultConfigTypeBackups) => {
  if (defaultConfigTypeBackups) {
    selectedConfig.value = defaultConfigTypeBackups[0]
  }
})

onMounted(() => getHistoryBackups())
</script>

<style scoped lang="scss">
@import "@featherds/styles/themes/variables";
@import "@featherds/styles/mixins/typography";

.flex {
  display: flex;
}
.flex-container {
  display: flex;
  max-width: 1000px;
  max-height: calc(100vh - 400px);
  overflow: auto;

  .history-dates-column {
    display: flex;
    flex-direction: column;
    white-space: nowrap;
    margin-right: 15px;

    .history-date {
      @include body-small;
      color: var($primary);
      margin-top: 5px;

      &.selected {
        font-weight: bold;
      }
    }
  }
}

.config-display-container {
  display: block;
}
.dwnld-btn {
  position: absolute;
  right: 70px;
  top: -55px;
}

.compare-btn {
  position: absolute;
  right: 35px;
  top: -55px;
}
</style>
