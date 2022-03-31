<template>
  <FeatherButton class="dwnld-btn" icon="Download config" @click="onCompare">
    <FeatherIcon :icon="Compare" />
  </FeatherButton>

  <div class="flex-container" v-if="!isCompareView">
    <FeatherCheckboxGroup label="Default" vertical>
      <div class="history-dates-column">
        <FeatherCheckbox
          class="history-date"
          v-for="config of defaultConfigTypeBackups"
          :key="config.id"
          @update:modelValue="onCheckbox(config)"
          :modelValue="selectedConfigs[config.id]"
        >
          <span v-date>{{ config.lastBackupDate }}</span>
        </FeatherCheckbox>
      </div>
    </FeatherCheckboxGroup>

    <FeatherCheckboxGroup label="Running" vertical v-if="otherConfigTypeBackups.length">
      <div class="history-dates-column">
        <FeatherCheckbox
          class="history-date"
          v-for="config of otherConfigTypeBackups"
          :key="config.id"
          @update:modelValue="onCheckbox(config)"
          :modelValue="selectedConfigs[config.id]"
        >
          <span v-date>{{ config.lastBackupDate }}</span>
        </FeatherCheckbox>
      </div>
    </FeatherCheckboxGroup>
  </div>

  <DCBDiff :config1="config1" :config2="config2" v-if="config1 && config2 && isCompareView" />
</template>

<script setup lang="ts">
import DCBDiff from './DCBDiff.vue'
import { useStore } from 'vuex'
import { DeviceConfigBackup } from '@/types/deviceConfig'
import { FeatherCheckbox, FeatherCheckboxGroup } from '@featherds/checkbox'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Compare from '@featherds/icon/action/ContentCopy'


const store = useStore()

const selectedConfigs = ref<Record<number, boolean>>({})
const config1 = ref<DeviceConfigBackup | null>(null)
const config2 = ref<DeviceConfigBackup | null>(null)
const isCompareView = ref(false)

const historyModalBackups = computed<DeviceConfigBackup[]>(() => store.state.deviceModule.historyModalBackups)
const defaultConfigTypeBackups = computed<DeviceConfigBackup[]>(() => historyModalBackups.value.filter((config) => config.configType === 'default'))
const otherConfigTypeBackups = computed<DeviceConfigBackup[]>(() => historyModalBackups.value.filter((config) => config.configType !== 'default'))

const onCompare = () => isCompareView.value = true

const onCheckbox = (config: DeviceConfigBackup) => {
  setConfig(config)

  // reset checkboxes
  for (const key in selectedConfigs.value) {
    selectedConfigs.value[key] = false
  }

  // set configs as selected
  if (config1.value) {
    selectedConfigs.value[config1.value.id] = true
  }

  if (config2.value) {
    selectedConfigs.value[config2.value.id] = true
  }
}

const setConfig = (config: DeviceConfigBackup) => {
  // if there is a config1, compare ids
  if (config1.value && config1.value.id === config.id) {
    // if they match, clear config
    config1.value = null
    return
  }

  // if there is a config2 and the ids match
  if (config2.value && config2.value.id === config.id) {
    // clear config two
    config2.value = null
    return
  }


  // if there is no config1, add it.
  if (!config1.value) {
    config1.value = config
    return
  }


  // if there is no config2, compare incoming configType
  if (!config2.value && config1.value.configType === config.configType) {
    // if types the same, add to config2
    config2.value = config
    return
  }

  // hits only if config1 & config2 and types match
  // or types do not match. Equivalent of first selection.
  config1.value = config
  config2.value = null
  return
}

const getHistoryBackups = () => store.dispatch('deviceModule/getHistoryByIpInterface')
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
  padding-left: 15px;

  .history-dates-column {
    display: flex;
    flex-direction: column;
    white-space: nowrap;

    .history-date {
      @include body-small;
      color: var($primary);
    }
  }
}
</style>
