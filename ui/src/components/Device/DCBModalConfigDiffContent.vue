<template>
  <PButton
    text
    class="compare-btn"
    aria-label="Compare configs"
    v-tooltip="'Compare configs'"
    @click="onCompare"
    v-if="!isCompareView"
    :disabled="!config1 || !config2"
  >
    <OnmsIcon :icon="Compare" />
  </PButton>

  <PButton
    text
    class="return-btn"
    aria-label="Return"
    v-tooltip="'Return'"
    @click="onReturn"
    v-if="isCompareView"
  >
    <OnmsIcon :icon="Restore" />
  </PButton>

  <PButton
    text
    class="dwnld-btn"
    aria-label="Download configs"
    v-tooltip="'Download configs'"
    @click="onDownload"
    v-if="isCompareView"
  >
    <OnmsIcon :icon="Download" />
  </PButton>

  <p class="select-msg" v-if="numberOfSelectedConfigs < 2">Select two dates to compare.</p>
  <p
    class="select-msg"
    v-if="!deviceStore.historyModalBackups.length"
  >No dates are available.</p>

  <div
    class="dcb-date-chips"
    aria-label="Compare selected configurations."
    v-if="config1 && config2"
  >
    <PChip>
      <span v-date>{{ config1.lastBackupDate }}</span>
    </PChip>
    <PChip>
      <span v-date>{{ config2.lastBackupDate }}</span>
    </PChip>
  </div>

  <div class="flex-container" v-if="!isCompareView">
    <div class="checkbox-group" v-if="deviceStore.historyModalBackups.length">
      <p class="group-label">{{ deviceStore.historyModalBackups[0].configName }}</p>
      <div class="history-dates-column">
        <div
          class="history-date"
          v-for="config of deviceStore.historyModalBackups"
          :key="config.id"
        >
          <PCheckbox
            binary
            :inputId="`dcb-date-${config.id}`"
            :modelValue="selectedConfigs[config.id]"
            @update:modelValue="onCheckbox(config)"
          />
          <label :for="`dcb-date-${config.id}`" v-date>{{ config.lastBackupDate }}</label>
        </div>
      </div>
    </div>
  </div>

  <div class="compare-container" v-if="config1 && config2 && isCompareView">
    <p class="changes">
      DIFFERENCES:
      <span class="deletions">-{{ changes.deletions }}</span>
      <span class="additions"> +{{ changes.additions }}</span>
    </p>
    <DCBDiff :config1="config1" :config2="config2" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import { diffLines } from 'diff'
import { orderBy } from 'lodash'
import Button from 'primevue/button'
import Checkbox from 'primevue/checkbox'
import Chip from 'primevue/chip'
import OnmsIcon from '@/components/icons/OnmsIcon.vue'
import Restore from '@/components/icons/action/Restore.vue'
import Download from '@/components/icons/action/DownloadFile.vue'
import DCBDiff from './DCBDiff.vue'
import Compare from '@/assets/Compare.vue'
import { useDeviceStore } from '@/stores/deviceStore'
import { DeviceConfigBackup } from '@/types/deviceConfig'

const PButton = Button
const PCheckbox = Checkbox
const PChip = Chip

const deviceStore = useDeviceStore()

const selectedConfigs = ref<Record<number, boolean>>({})
const config1 = ref<DeviceConfigBackup | null>(null)
const config2 = ref<DeviceConfigBackup | null>(null)
const isCompareView = ref(false)
const changes = ref<{ additions: number; deletions: number }>({ additions: 0, deletions: 0 })

const numberOfSelectedConfigs = computed<number>(() => Object.values(selectedConfigs.value).filter(val => val).length)

const onCompare = () => isCompareView.value = true
const onReturn = () => isCompareView.value = false
const onDownload = () => {
  const ids = [config1.value?.id || 0, config2.value?.id || 0].filter(x => x !== 0)

  if (ids.length > 0) {
    deviceStore.downloadByConfig(ids)
  }
}

const onCheckbox = (config: DeviceConfigBackup) => {
  setConfig(config)
  updateCheckboxes()
  orderByDates()
  calculateChanges()
}

const orderByDates = () => {
  // must have both configs to order
  if (config1.value && config2.value) {
    // order so that config1 is the 'previous' or 'older' version
    const orderedByDate = orderBy([config1.value, config2.value], 'lastBackupDate', 'asc')
    config1.value = orderedByDate[0]
    config2.value = orderedByDate[1]
  }
}

const updateCheckboxes = () => {
  // clear all checkboxes
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

/**
 * Sets the config1 or config2 variables, which are
 * used for updating which checkboxes are true,
 * displaying the selected date chips, and the
 *  prev/current configuration text comparison.
 *
 * @param config device config from checkbox clicked
 * @returns void
 */
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

const calculateChanges = () => {
  if (config1.value && config2.value) {
    const diff = diffLines(config1.value.config, config2.value.config)
    const additions = diff.filter(item => item.added).length
    const deletions = diff.filter(item => item.removed).length

    changes.value = {
      additions,
      deletions
    }
  }
}

const getHistoryBackups = () => deviceStore.getHistoryByIpInterface()
onMounted(() => getHistoryBackups())
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
.flex-container {
  display: flex;
  max-width: 1000px;
  max-height: calc(100vh - 400px);
  overflow: auto;
  padding-left: 15px;

  .checkbox-group {
    .group-label {
      @include subtitle2;
    }
  }

  .history-dates-column {
    display: flex;
    flex-direction: column;
    white-space: nowrap;

    .history-date {
      @include body-small;
      display: flex;
      align-items: center;
      gap: 0.5rem;
      color: var(--p-primary-color);

      label {
        cursor: pointer;
      }
    }
  }
}

.compare-container {
  max-width: 1000px;
  max-height: calc(100vh - 400px);
  overflow: auto;
}
.select-msg {
  @include subtitle1;
  color: var(--p-primary-color);
  padding-left: 15px;
  margin-bottom: 33px;
}

.dcb-date-chips {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 23px;
}
.changes {
  @include button;

  .deletions {
    color: var(--p-red-500);
  }

  .additions {
    color: var(--p-green-500);
  }
}
.compare-btn,
.return-btn {
  position: absolute;
  right: 35px;
  top: 7px;
}

.dwnld-btn {
  position: absolute;
  right: 70px;
  top: 7px;
}
</style>
