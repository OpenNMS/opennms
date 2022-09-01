<template>
  <FeatherButton class="compare-btn" icon="Compare configs" @click="emit('onCompare')">
    <FeatherIcon :icon="Compare" />
  </FeatherButton>

  <FeatherButton class="dwnld-btn" icon="Download config" @click="onDownload">
    <FeatherIcon :icon="Download" />
  </FeatherButton>

  <span class="title">
    {{ selectedConfig?.configName }}
  </span>

  <div class="flex-container">
    <div v-if="selectedConfig" class="flex">
      <div class="history-dates-column">
        <div
          v-date
          class="history-date pointer"
          :class="{ 'selected' : selectedConfig?.id === config.id }"
          v-for="config of historyModalBackups"
          :key="config.id"
          @click="setSelectedConfig(config)"
        >{{ config.lastBackupDate }}</div>
      </div>
      <div class="config-display-container">
        <!-- We can use the diff editor display for a single config -->
        <DCBDiff :config1="selectedConfig" :config2="selectedConfig" :mode="'unified'" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Download from '@featherds/icon/action/DownloadFile'
import Compare from '@/assets/Compare.vue'
import { useStore } from 'vuex'
import { DeviceConfigBackup } from '@/types/deviceConfig'
import DCBDiff from './DCBDiff.vue'

const emit = defineEmits(['onCompare'])

const store = useStore()
const selectedConfig = ref<DeviceConfigBackup>()
const historyModalBackups = computed<DeviceConfigBackup[]>(() => store.state.deviceModule.historyModalBackups)

watch(historyModalBackups, (historyModalBackups) => {
  if (historyModalBackups) {
    selectedConfig.value = historyModalBackups[0]
  }
})

const onDownload = () => store.dispatch('deviceModule/downloadByConfig', selectedConfig.value)
const setSelectedConfig = (config: DeviceConfigBackup) => selectedConfig.value = config

onMounted(() => store.dispatch('deviceModule/getHistoryByIpInterface'))
</script>

<style scoped lang="scss">
@import "@featherds/styles/themes/variables";
@import "@featherds/styles/mixins/typography";

.title {
  @include subtitle1;
}
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
    margin-top: 12px;

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
