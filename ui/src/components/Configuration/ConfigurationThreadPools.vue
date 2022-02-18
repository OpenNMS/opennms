<template>
  <FeatherExpansionPanel
    id="thread-pool-expansion"
    class="expansion-panel"
    v-model="threadPoolsActive"
  >
    <template v-slot:title>
      <div class="title-flex">
        <div class="title">Thread Pools</div>
        <div v-if="!threadPoolsActive">
          <FeatherChipList>
            <FeatherChip v-if="unTouchedThreadPoolData.importThreads">
              <template v-slot:icon>{{ unTouchedThreadPoolData.importThreads }}</template>Import Threads
            </FeatherChip>
            <FeatherChip v-if="unTouchedThreadPoolData.scanThreads">
              <template v-slot:icon>{{ unTouchedThreadPoolData.scanThreads }}</template>Scan Threads
            </FeatherChip>
            <FeatherChip v-if="unTouchedThreadPoolData.rescanThreads">
              <template v-slot:icon>{{ unTouchedThreadPoolData.rescanThreads }}</template>Rescan Threads
            </FeatherChip>
            <FeatherChip v-if="unTouchedThreadPoolData.writeThreads">
              <template v-slot:icon>{{ unTouchedThreadPoolData.writeThreads }}</template>Write Threads
            </FeatherChip>
          </FeatherChipList>
        </div>
      </div>
    </template>
    <div>
      <p>Thread pool sizes have been set based on your configuration. To adjust them, select a box below.</p>
      <FeatherInput
        :error="getError('importThreads')"
        type="number"
        label="Import"
        v-model="threadPoolData.importThreads"
        @keypress="enterCheck"
      />
      <FeatherInput
        :error="getError('scanThreads')"
        type="number"
        label="Scan"
        v-model="threadPoolData.scanThreads"
        @keypress="enterCheck"
      />
      <FeatherInput
        :error="getError('rescanThreads')"
        type="number"
        label="Rescan"
        v-model="threadPoolData.rescanThreads"
        @keypress="enterCheck"
      />
      <FeatherInput
        :error="getError('writeThreads')"
        type="number"
        label="Write"
        v-model="threadPoolData.writeThreads"
        @keypress="enterCheck"
      />
      <div class="spinner-button">
        <FeatherButton primary @click="updateThreadpools" :disabled="loading">
          <FeatherSpinner v-if="loading" />
          <span v-if="!loading">Update Threadpools</span>
        </FeatherButton>
      </div>
    </div>
  </FeatherExpansionPanel>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useStore } from 'vuex'

import { FeatherInput } from '@featherds/input'
import { FeatherButton } from '@featherds/button'
import { FeatherExpansionPanel } from '@featherds/expansion'
import { FeatherChip, FeatherChipList } from '@featherds/chips'
import { FeatherSpinner } from '@featherds/progress'

import { populateProvisionD, putProvisionDService } from '@/services/configurationService'
import { useConfigurationToast } from './hooks/configurationToast'
import { threadPoolKeys } from './copy/threadPoolKeys'
import { ConfigurationHelper } from './ConfigurationHelper'

/**
 * Local State
 */
const threadPoolsErrors = ref<Record<string, boolean>>({})
const errorMessage = 'Thread pool values have to be between 1 and 2000.'
const threadPoolsActive = ref(false)
const loading = ref(false)

const threadPoolData = computed(() => {
  const localThreads: Record<string, string> = {}
  threadPoolKeys.forEach((key) => (localThreads[key] = store?.state?.configuration?.provisionDService?.[key]))
  return reactive(localThreads)
})

const unTouchedThreadPoolData = computed(() => {
  const localThreads: Record<string, string> = {}
  threadPoolKeys.forEach((key) => (localThreads[key] = store?.state?.configuration?.provisionDService?.[key]))
  return reactive(localThreads)
})

/**
 * Hooks
 */
const store = useStore()
const { updateToast } = useConfigurationToast()

/**
 * User has opted to update threadpool data.
 */
const updateThreadpools = async () => {
  loading.value = true
  // Clear Errors
  threadPoolsErrors.value = {}

  // Set Current Threadpool state.
  const currentThreadpoolState = threadPoolData.value
  const updatedProvisionDData = store?.state?.configuration?.provisionDService

  // Validate Threadpool Data
  threadPoolKeys.forEach((key) => {
    const val = parseInt(currentThreadpoolState?.[key])
    if (val < 1 || val > 2000) {
      threadPoolsErrors.value[key] = true
    }
  })

  let toastMessage = {
    basic: 'Error!',
    detail: errorMessage,
    hasErrors: true
  }
  // If there are no errors.
  if (Object.keys(threadPoolsErrors.value).length === 0) {

    try {
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
      await populateProvisionD(store)
      toastMessage = {
        basic: 'Success!',
        detail: 'Thread Pool data saved.',
        hasErrors: false
      }
    } catch (e) {
      toastMessage = {
        basic: 'Error!',
        detail: 'Thread Pool data not saved.',
        hasErrors: true
      }

    }
  }

  //Send Toast Message
  updateToast(toastMessage)
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
  return threadPoolsErrors.value[key] ? errorMessage : ''
}
</script>

<style lang="scss">
.spinner-button {
  .spinner {
    width: 20px;
    height: 20px;
  }
  .spinner-container {
    display: flex;
    align-items: center;
    height: 100%;
  }
}
</style>

<style lang="scss" scoped>
@import "@featherds/styles/mixins/typography";
.title {
  @include headline3();
  margin-right: 16px;
}
.title-flex {
  display: flex;
  align-items: center;
}
</style>
<style lang="scss">
@import "@featherds/styles/mixins/typography";

#thread-pool-expansion {
  .feather-expansion-header-button {
    height: 72px;
  }
}
</style>