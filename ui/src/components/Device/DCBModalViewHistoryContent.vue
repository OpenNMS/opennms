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
import { storeToRefs } from 'pinia'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Download from '@featherds/icon/action/DownloadFile'
import Compare from '@/assets/Compare.vue'
import DCBDiff from './DCBDiff.vue'
import { DeviceConfigBackup } from '@/types/deviceConfig'
import { useDeviceStore } from '@/stores/deviceStore'

const emit = defineEmits(['onCompare'])

const deviceStore = useDeviceStore()
const selectedConfig = ref<DeviceConfigBackup>()
const { historyModalBackups } = storeToRefs(deviceStore)

watch(historyModalBackups, (backups) => {
  if (backups) {
    selectedConfig.value = backups[0]
  }
})

const onDownload = () => {
  if (selectedConfig.value?.id) {
    deviceStore.downloadByConfig([selectedConfig.value.id])
  }
}

const setSelectedConfig = (config: DeviceConfigBackup) => selectedConfig.value = config

onMounted(() => deviceStore.getHistoryByIpInterface())
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
