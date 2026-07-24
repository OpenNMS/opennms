<template>
  <TogglePanel
    id="thread-pool-expansion"
    class="expansion-panel"
    :collapsed="!threadPoolsActive"
    @update:collapsed="(v) => threadPoolsActive = !v"
  >
    <template #header>
      <div class="title-flex">
        <div class="title">Thread Pools</div>
        <div v-if="!threadPoolsActive" class="chip-list">
          <PChip v-if="unTouchedThreadPoolData.importThreads" :label="`${unTouchedThreadPoolData.importThreads} Import Threads`" />
          <PChip v-if="unTouchedThreadPoolData.scanThreads" :label="`${unTouchedThreadPoolData.scanThreads} Scan Threads`" />
          <PChip v-if="unTouchedThreadPoolData.rescanThreads" :label="`${unTouchedThreadPoolData.rescanThreads} Rescan Threads`" />
          <PChip v-if="unTouchedThreadPoolData.writeThreads" :label="`${unTouchedThreadPoolData.writeThreads} Write Threads`" />
        </div>
      </div>
    </template>
    <div>
      <p class="pb-xl">
        Thread pool sizes impact the performance of the provisioning subsystem. Larger systems may require larger
        values. To adjust them, type a new number in the field or use the up/down arrows to select a value.
      </p>
      <FormField label="Import" :error="getError('importThreads')" hint="Number of threads to allocate for requisition import tasks.">
        <PInputText
          type="number"
          :invalid="Boolean(getError('importThreads'))"
          v-model="threadPoolData.importThreads"
          @keypress="enterCheck"
        />
      </FormField>
      <FormField label="Scan" :error="getError('scanThreads')" hint="Number of threads to allocate for manual scanning tasks.">
        <PInputText
          type="number"
          :invalid="Boolean(getError('scanThreads'))"
          v-model="threadPoolData.scanThreads"
          @keypress="enterCheck"
        />
      </FormField>
      <FormField label="Rescan" :error="getError('rescanThreads')" hint="Number of threads to allocate for scheduled rescanning tasks.">
        <PInputText
          type="number"
          :invalid="Boolean(getError('rescanThreads'))"
          v-model="threadPoolData.rescanThreads"
          @keypress="enterCheck"
        />
      </FormField>
      <FormField class="last-input" label="Write" :error="getError('writeThreads')" hint="Number of threads to allocate for writing to the database.">
        <PInputText
          type="number"
          :invalid="Boolean(getError('writeThreads'))"
          v-model="threadPoolData.writeThreads"
          @keypress="enterCheck"
        />
      </FormField>
      <OnmsButton
        label="Update Thread Pools"
        :loading="loading"
        :disabled="loading"
        @click="updateThreadpools"
      />
    </div>
  </TogglePanel>
</template>

<script
  setup
  lang="ts"
>
import { computed, reactive, ref } from 'vue'

import { useConfigurationStore } from '@/stores/configurationStore'

import { OnmsButton } from '@opennms/onms-ui'
import InputText from 'primevue/inputtext'
import Chip from 'primevue/chip'
import TogglePanel from '@/components/Common/TogglePanel.vue'
import FormField from '@/components/Common/FormField.vue'
import { isEqual as _isEqual } from 'lodash'

import { putProvisionDService } from '@/services/configurationService'
import useSnackbar from '@/composables/useSnackbar'
import { threadPoolKeys } from './copy/threadPoolKeys'
import { ConfigurationHelper } from './ConfigurationHelper'

const PInputText = InputText
const PChip = Chip

const configurationStore = useConfigurationStore()
const { showSnackBar } = useSnackbar()

const threadPoolsErrors = ref<Record<string, boolean>>({})
const threadPoolsActive = ref(false)
const loading = ref(false)

const getUpperBound = (key: string) => ['importThreads', 'writeThreads'].includes(key) ? 100 : 2000
const upperBoundErrorMessage = (upperBound: number) => `Thread pool values have to be between 1 and ${upperBound}.`
const snackbarErrorMessage = 'Thread pool values are outside of supported range.'

const threadPoolData = computed(() => {
  const localThreads: Record<string, string> = {}
  threadPoolKeys.forEach(key => (localThreads[key] = configurationStore.provisionDService?.[key]))

  return reactive(localThreads)
})

const unTouchedThreadPoolData = computed(() => {
  const localThreads: Record<string, string> = {}
  threadPoolKeys.forEach(key => (localThreads[key] = configurationStore.provisionDService?.[key]))

  return reactive(localThreads)
})

/** User has opted to update threadpool data.  */
const updateThreadpools = async () => {
  loading.value = true
  // Clear Errors
  threadPoolsErrors.value = {}

  // Set Current Threadpool state.
  const currentThreadpoolState = threadPoolData.value
  const updatedProvisionDData = configurationStore.provisionDService

  // Validate Threadpool Data
  threadPoolKeys.forEach((key) => {
    const val = parseInt(currentThreadpoolState?.[key])
    if (val < 1 || val > getUpperBound(key)) {
      threadPoolsErrors.value[key] = true
    }
  })

  // If there are no errors.
  if (Object.keys(threadPoolsErrors.value).length === 0) {
    try {
      // reduce provisionD data object to thread pool sizes, in order to determine whether thread pool sizes value has changed, upon update button clicked
      const reducedUpdatedProvisionDData = threadPoolKeys.reduce((acc, key) => {
        const obj: Record<string, string> = {}

        for (let elem in updatedProvisionDData) {
          if (elem === key) {
            obj[elem] = updatedProvisionDData[elem]
            break
          }
        }

        return { ...acc, ...obj }
      }, {})
      const haveThreadPoolValuesChanged = !_isEqual(currentThreadpoolState, reducedUpdatedProvisionDData)

      // Set Update State
      threadPoolKeys.forEach((key) => {
        if (updatedProvisionDData?.[key]) {
          updatedProvisionDData[key] = parseInt(currentThreadpoolState?.[key])
        }
      })
      if (updatedProvisionDData) {
        updatedProvisionDData['requisition-def'] = ConfigurationHelper.stripOriginalIndexes(updatedProvisionDData['requisition-def'])
      }
      // Push Updates to Server
      await putProvisionDService(updatedProvisionDData)
      // Redownload + Populate Data.
      await configurationStore.getProvisionDService()

      let messageUpdateSuccess = 'Thread pool data saved.'

      if (!haveThreadPoolValuesChanged) {
        showSnackBar({
          msg: messageUpdateSuccess
        })
      } else {
        messageUpdateSuccess += ' Restart OpenNMS for this change to take effect.'

        showSnackBar({
          msg: messageUpdateSuccess,
          timeout: 10000
        })
      }
    } catch (err) {
      showSnackBar({
        msg: `Thread pool data not saved. (${err})`,
        error: true
      })
    }
  } else {
    showSnackBar({
      msg: snackbarErrorMessage,
      error: true
    })
  }

  loading.value = false
}

/**
 * Check if User has hit enter in a Threadpool box.
 * @param key They key that has been pressed.
 */
const enterCheck = (key: { key: string }) => {
  if (key.key === 'Enter') {
    updateThreadpools()
  }
}

/**
 * Determine is error is set for a key, and if so, return generic error message.
 */
const getError = (key: string) => {
  return threadPoolsErrors.value[key] ? upperBoundErrorMessage(getUpperBound(key)) : ''
}
</script>

<style
  lang="scss"
  scoped
>
@import '@/styles/onms-typography';

// Local replacement for the removed FeatherDS global spacing utility
// (--onms-spacing-xl mirrors the original FeatherDS value).
.pb-xl {
  padding-bottom: var(--onms-spacing-xl);
}
.title {
  @include onms-headline3();
  margin-right: 16px;
}
.title-flex {
  display: flex;
  align-items: center;
}
.chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}
:deep(.form-field) {
  margin-bottom: 1.25rem;
}
.last-input {
  margin-bottom: 10px;
}
</style>
