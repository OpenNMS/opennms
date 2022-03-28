<template>
  <FeatherButton class="dwnld-btn" icon="Download config" @click="onDownload">
    <FeatherIcon :icon="Download" />
  </FeatherButton>

  <div class="flex-container">
    <div class="history-dates-column" v-if="hasOnlyDefaultConfigs || selectedTab === 'default'">
      <div
        v-date
        class="history-date pointer"
        v-for="config of defaultConfigTypeBackups"
        :key="config.id"
        @click="setSelectedConfig(config)"
      >{{ config.lastBackupDate }}</div>
    </div>

    <div class="history-dates-column" v-if="selectedTab !== 'default'">
      <div
        v-date
        class="history-date pointer"
        v-for="config of otherConfigTypeBackups"
        :key="config.id"
        @click="setSelectedConfig(config)"
      >{{ config.lastBackupDate }}</div>
    </div>

    <div v-if="selectedConfig && hasOnlyDefaultConfigs">{{ selectedConfig.config }}</div>

    <FeatherTabContainer class="tabs" v-if="!hasOnlyDefaultConfigs">
      <template v-slot:tabs>
        <FeatherTab @click="setSelectedTab('default')">Startup Configuration</FeatherTab>
        <FeatherTab @click="setSelectedTab('running')">Running Configuration</FeatherTab>
      </template>
      <FeatherTabPanel>
        <div v-if="selectedConfig">{{ selectedConfig.config }}</div>
      </FeatherTabPanel>
      <FeatherTabPanel>
        <div v-if="selectedConfig">{{ selectedConfig.config }}</div>
      </FeatherTabPanel>
    </FeatherTabContainer>
  </div>
</template>

<script setup lang="ts">
import { computed, watch, ref, onMounted } from 'vue'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'
import Download from '@featherds/icon/action/DownloadFile'
import { useStore } from 'vuex'
import { DeviceConfigBackup, defaultConfig, runningConfig } from '@/types/deviceConfig'

const store = useStore()
const selectedTab = ref<defaultConfig | runningConfig>('default')
const selectedConfig = ref<DeviceConfigBackup>()

const modalDeviceConfigBackup = computed<DeviceConfigBackup>(() => store.state.deviceModule.modalDeviceConfigBackup)
const historyModalBackups = computed<DeviceConfigBackup[]>(() => store.state.deviceModule.historyModalBackups)
const defaultConfigTypeBackups = computed<DeviceConfigBackup[]>(() => historyModalBackups.value.filter((config) => config.configType === 'default'))
const otherConfigTypeBackups = computed<DeviceConfigBackup[]>(() => historyModalBackups.value.filter((config) => config.configType !== 'default'))
const hasOnlyDefaultConfigs = computed<boolean>(() => historyModalBackups.value.length === defaultConfigTypeBackups.value.length)

const onDownload = () => store.dispatch('deviceModule/downloadByConfig', selectedConfig.value)
const getHistoryBackups = () => store.dispatch('deviceModule/getHistoryByIpInterface', modalDeviceConfigBackup.value.ipInterfaceId)
const setSelectedConfig = (config: DeviceConfigBackup) => selectedConfig.value = config
const setSelectedTab = (configType: defaultConfig | runningConfig) => {
  selectedTab.value = configType

  if (configType === 'default') {
    setSelectedConfig(defaultConfigTypeBackups.value[0])
  } else {
    setSelectedConfig(otherConfigTypeBackups.value[0])
  }
}
const historyColumnMarginTop = computed<string>(() => hasOnlyDefaultConfigs.value ? '0px' : '45px')

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
    margin-top: v-bind(historyColumnMarginTop);

    .history-date {
      @include body-small;
      color: var($primary);
    }
  }
}
.dwnld-btn {
  position: absolute;
  right: 36px;
  top: -77px;
}
</style>
