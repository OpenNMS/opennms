<template>
  <TableCard class="general-configuration">
    <div class="header">
      <div class="section-left">
        <h3>Trap Listener Settings</h3>
      </div>
    </div>
    <div class="info-section">
      <span>Configure trap listener settings.</span>
      <FeatherIcon
        :icon="InfoIcon"
        class="info-icon"
        role="button"
        tabindex="0"
        @click="isMessageDialogVisible = true"
        @keydown.enter.space.prevent="isMessageDialogVisible = true"
        data-test="trap-config-general-info-icon"
      />
    </div>
    <div class="section">
      <div class="field">
        <IftaLabel>
          <PInputNumber
            inputId="trap-port"
            v-model="port"
            :min="MIN_PORT"
            :max="MAX_PORT"
            :useGrouping="false"
            :invalid="!!trapConfigError.port"
          />
          <label for="trap-port">Port</label>
        </IftaLabel>
        <small
          v-if="trapConfigError.port"
          class="field-error"
        >{{ trapConfigError.port }}</small>
        <small
          v-else
          class="field-hint"
        >Default: 10162</small>
      </div>
      <div class="field">
        <IftaLabel>
          <PInputText
            id="trap-bind-address"
            v-model="bindAddress"
            :invalid="!!trapConfigError.bindAddress"
          />
          <label for="trap-bind-address">Bind Address</label>
        </IftaLabel>
        <small
          v-if="trapConfigError.bindAddress"
          class="field-error"
        >{{ trapConfigError.bindAddress }}</small>
        <small
          v-else
          class="field-hint"
        >* for all, or specify IP address</small>
      </div>
    </div>
    <div class="spacer"></div>
    <div class="spacer"></div>
    <div class="switch-row">
      <PToggleSwitch
        v-model="newSuspectOnTrap"
        data-test="unknown-devices-input"
      />
      <label class="switch-label">Create new nodes when receiving an SNMP trap with an unknown source IP address?</label>
    </div>
    <div class="spacer"></div>
    <div class="spacer"></div>
    <div class="expansion-panel">
      <TogglePanel
        header="Advanced Configuration Options"
        v-model:collapsed="advancedCollapsed"
      >
        <div class="expansion-section">
          <div class="spacer"></div>
          <div class="spacer"></div>
          <div class="trap-message-row">
            <PToggleSwitch
              v-model="trapMessageStatus"
              data-test="trap-message-input"
            />
            <label class="switch-label">Include raw trap message (before processing)</label>
          </div>
          <div class="spacer"></div>
          <div class="spacer"></div>
          <div class="trap-source-address-row">
            <PToggleSwitch
              v-model="trapSourceAddressStatus"
              data-test="trap-source-address-input"
            />
            <label class="switch-label">Use forwarded trap source address (for forwarded SNMPv2 traps)</label>
          </div>
          <div class="spacer"></div>
          <div class="spacer"></div>
          <div class="field">
            <IftaLabel>
              <PInputNumber
                inputId="trap-threads"
                v-model="threads"
                :min="0"
                :useGrouping="false"
                :invalid="!!trapConfigError.threads"
              />
              <label for="trap-threads">Threads</label>
            </IftaLabel>
            <small
              v-if="trapConfigError.threads"
              class="field-error"
            >{{ trapConfigError.threads }}</small>
            <small
              v-else
              class="field-hint"
            >Default: 0</small>
          </div>
          <div class="spacer"></div>
          <div class="spacer"></div>
          <div class="field">
            <IftaLabel>
              <PInputNumber
                inputId="trap-queue-size"
                v-model="queueSize"
                :min="0"
                :useGrouping="false"
                :invalid="!!trapConfigError.queueSize"
              />
              <label for="trap-queue-size">Queue Size</label>
            </IftaLabel>
            <small
              v-if="trapConfigError.queueSize"
              class="field-error"
            >{{ trapConfigError.queueSize }}</small>
            <small
              v-else
              class="field-hint"
            >Default: 10000</small>
          </div>
          <div class="spacer"></div>
          <div class="spacer"></div>
          <div class="field">
            <IftaLabel>
              <PInputNumber
                inputId="trap-batch-size"
                v-model="batchSize"
                :min="0"
                :useGrouping="false"
                :invalid="!!trapConfigError.batchSize"
              />
              <label for="trap-batch-size">Batch Size</label>
            </IftaLabel>
            <small
              v-if="trapConfigError.batchSize"
              class="field-error"
            >{{ trapConfigError.batchSize }}</small>
            <small
              v-else
              class="field-hint"
            >Default: 1000</small>
          </div>
          <div class="spacer"></div>
          <div class="spacer"></div>
          <div class="field">
            <IftaLabel>
              <PInputNumber
                inputId="trap-batch-interval"
                v-model="batchInterval"
                :min="0"
                :useGrouping="false"
                :invalid="!!trapConfigError.batchInterval"
              />
              <label for="trap-batch-interval">Batch Interval ms</label>
            </IftaLabel>
            <small
              v-if="trapConfigError.batchInterval"
              class="field-error"
            >{{ trapConfigError.batchInterval }}</small>
            <small
              v-else
              class="field-hint"
            >Default: 500ms</small>
          </div>
          <div class="spacer"></div>
          <div class="spacer"></div>
        </div>
      </TogglePanel>
    </div>
    <div class="spacer"></div>
    <div class="spacer"></div>
    <div class="footer">
      <PButton
        data-test="save-button"
        label="Update Changes"
        :disabled="isSaveDisabled || isSaving"
        @click="updateConfig"
      />
    </div>
    <MessageDialog
      :visible="isMessageDialogVisible"
      maxHeight="22em"
      maxWidth="50em"
      title="Trap Configuration"
      @close="isMessageDialogVisible = false"
    >
      <template #content>
        <div>
          <p>Configure trap listener settings. <strong>Note</strong> that the settings here only apply to the OpenNMS core system, not to any Minions or other distributed components.</p>
        </div>
      </template>
    </MessageDialog>
  </TableCard>
</template>

<script setup lang="ts">
import { ref, watch, watchEffect } from 'vue'

import { isEqual } from 'lodash'
import Button from 'primevue/button'
import IftaLabel from 'primevue/iftalabel'
import InputNumber from 'primevue/inputnumber'
import InputText from 'primevue/inputtext'
import ToggleSwitch from 'primevue/toggleswitch'
import { FeatherIcon } from '@featherds/icon'
import InfoIcon from '@featherds/icon/action/Info'
import useSnackbar from '@/composables/useSnackbar'
import { DEFAULT_TRAPD_BATCH_INTERVAL, DEFAULT_TRAPD_BATCH_SIZE, DEFAULT_TRAPD_BIND_ADDRESS, DEFAULT_TRAPD_INCLUDE_RAW_MESSAGE, DEFAULT_TRAPD_NEW_SUSPECT_ON_TRAP, DEFAULT_TRAPD_PORT, DEFAULT_TRAPD_QUEUE_SIZE, DEFAULT_TRAPD_THREADS, DEFAULT_TRAPD_USE_ADDRESS_FROM_VARBIND } from '@/lib/constants'
import { isValidIP, isValidPort, MAX_PORT, MIN_PORT } from '@/lib/trapdValidator'
import { updateTrapdConfiguration } from '@/services/trapdConfigurationService'
import { useTrapdConfigStore } from '@/stores/trapdConfigStore'
import { TrapConfig, TrapdConfigurationError } from '@/types/trapConfig'
import MessageDialog from '../Common/MessageDialog.vue'
import TableCard from '../Common/TableCard.vue'
import TogglePanel from '../Common/TogglePanel.vue'

const PButton = Button
const PInputNumber = InputNumber
const PInputText = InputText
const PToggleSwitch = ToggleSwitch

const newSuspectOnTrap = ref(DEFAULT_TRAPD_NEW_SUSPECT_ON_TRAP)
const port = ref<number>(DEFAULT_TRAPD_PORT)
const bindAddress = ref(DEFAULT_TRAPD_BIND_ADDRESS)
const trapMessageStatus = ref(DEFAULT_TRAPD_INCLUDE_RAW_MESSAGE)
const trapSourceAddressStatus = ref(DEFAULT_TRAPD_USE_ADDRESS_FROM_VARBIND)
const threads = ref<number>(DEFAULT_TRAPD_THREADS)
const queueSize = ref<number>(DEFAULT_TRAPD_QUEUE_SIZE)
const batchSize = ref<number>(DEFAULT_TRAPD_BATCH_SIZE)
const batchInterval = ref<number>(DEFAULT_TRAPD_BATCH_INTERVAL)
const trapConfigError = ref<TrapdConfigurationError>({})
const isSaveDisabled = ref(true)
const isSaving = ref(false)
const store = useTrapdConfigStore()
const { showSnackBar } = useSnackbar()
const isMessageDialogVisible = ref(false)
const advancedCollapsed = ref(true)

const validateInputs = (): TrapdConfigurationError => {
  const trapConfigError: TrapdConfigurationError = {}

  if (!isValidPort(port.value)) {
    trapConfigError.port = `Port must be between ${MIN_PORT} and ${MAX_PORT}.`
  }

  if (bindAddress.value === '') {
    trapConfigError.bindAddress = 'Bind Address cannot be empty.'
  } else if (bindAddress.value !== '*' && !isValidIP(bindAddress.value)) {
    trapConfigError.bindAddress = 'Bind Address must be * or a valid IP address.'
  }

  if (threads.value < 0) {
    trapConfigError.threads = 'Threads cannot be negative.'
  }

  if (queueSize.value < 1) {
    trapConfigError.queueSize = 'Queue Size must be greater than 0.'
  }

  if (batchSize.value < 1) {
    trapConfigError.batchSize = 'Batch Size must be greater than 0.'
  }

  if (batchInterval.value < 0) {
    trapConfigError.batchInterval = 'Batch Interval cannot be negative.'
  }

  return trapConfigError
}

const updateConfig = async () => {
  const error = validateInputs()
  if (Object.keys(error).length > 0) {
    return
  }

  const newConfig: TrapConfig = {
    snmpTrapPort: Number(port.value),
    snmpTrapAddress: bindAddress.value,
    newSuspectOnTrap: newSuspectOnTrap.value,
    useAddressFromVarbind: trapSourceAddressStatus.value,
    includeRawMessage: trapMessageStatus.value,
    threads: Number(threads.value),
    queueSize: Number(queueSize.value),
    batchSize: Number(batchSize.value),
    batchInterval: Number(batchInterval.value),
    snmpv3User: store.snmpV3Users || []
  }

  try {
    isSaving.value = true

    await updateTrapdConfiguration(newConfig)
    showSnackBar({ msg: 'Trap configuration updated successfully.' })
    await store.fetchTrapConfig()
  } catch (err) {
    const msg = err instanceof Error ? err.message : 'Failed to update trap configuration.'
    showSnackBar({ msg, error: true })
  } finally {
    isSaving.value = false
  }
}

const loadInitialConfig = () => {
  if (store.trapdConfig) {
    port.value = store.trapdConfig.snmpTrapPort || DEFAULT_TRAPD_PORT
    bindAddress.value = store.trapdConfig.snmpTrapAddress || DEFAULT_TRAPD_BIND_ADDRESS
    newSuspectOnTrap.value = store.trapdConfig.newSuspectOnTrap ?? DEFAULT_TRAPD_NEW_SUSPECT_ON_TRAP
    trapSourceAddressStatus.value = store.trapdConfig.useAddressFromVarbind ?? DEFAULT_TRAPD_USE_ADDRESS_FROM_VARBIND
    trapMessageStatus.value = store.trapdConfig.includeRawMessage ?? DEFAULT_TRAPD_INCLUDE_RAW_MESSAGE
    threads.value = store.trapdConfig.threads || DEFAULT_TRAPD_THREADS
    queueSize.value = store.trapdConfig.queueSize || DEFAULT_TRAPD_QUEUE_SIZE
    batchSize.value = store.trapdConfig.batchSize || DEFAULT_TRAPD_BATCH_SIZE
    batchInterval.value = store.trapdConfig.batchInterval || DEFAULT_TRAPD_BATCH_INTERVAL
  }
}

watchEffect(() => {
  trapConfigError.value = validateInputs()
  isSaveDisabled.value = Object.keys(trapConfigError.value).length > 0 || isEqual({
    snmpTrapPort: Number(port.value),
    snmpTrapAddress: bindAddress.value,
    newSuspectOnTrap: newSuspectOnTrap.value,
    useAddressFromVarbind: trapSourceAddressStatus.value,
    includeRawMessage: trapMessageStatus.value,
    threads: Number(threads.value),
    queueSize: Number(queueSize.value),
    batchSize: Number(batchSize.value),
    batchInterval: Number(batchInterval.value)
  }, {
    snmpTrapPort: store.trapdConfig?.snmpTrapPort,
    snmpTrapAddress: store.trapdConfig?.snmpTrapAddress,
    newSuspectOnTrap: store.trapdConfig?.newSuspectOnTrap,
    useAddressFromVarbind: store.trapdConfig?.useAddressFromVarbind,
    includeRawMessage: store.trapdConfig?.includeRawMessage,
    threads: store.trapdConfig?.threads,
    queueSize: store.trapdConfig?.queueSize,
    batchSize: store.trapdConfig?.batchSize,
    batchInterval: store.trapdConfig?.batchInterval
  })
})

watch(() => store.trapdConfig, () => {
  loadInitialConfig()
}, { immediate: true, deep: true })
</script>

<style lang="scss" scoped>
@use '@featherds/styles/mixins/typography';

.general-configuration {
  margin-top: 10px;
  padding: 25px;
  border: 1px solid var(--p-content-border-color);

  .header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;

    .section-left {
      h3 {
        @include typography.headline3;
        color: var(--p-text-color);
      }

      p {
        @include typography.body-large;
        color: var(--p-text-muted-color);
      }
    }
  }

  .spacer {
    height: 0.5em;
  }

  .info-section {
    margin-bottom: 1em;

    .label {
      color: var(--p-text-color);
    }

    .info-icon {
      cursor: pointer;
      font-size: 1.5em;
      margin-left: 0.5em;
      color: var(--p-primary-color);

      &:hover {
        opacity: 0.8;
      }
    }
  }

  .switch-row {
    display: flex;
    align-items: center;
    gap: 0.75rem;

    .switch-label {
      @include typography.body-small;
    }
  }

  .section {
    display: flex;
    align-items: flex-start;
    gap: 20px;
    width: 50%;
    margin-top: 1.5em;

    &>* {
      flex: 1;
    }
  }

  // input field with optional hint/error helper text
  .field {
    display: flex;
    flex-direction: column;

    :deep(.p-inputtext),
    :deep(.p-inputnumber) {
      width: 100%;
    }

    // .field-hint is styled globally (primevue-overrides.scss); keep the error
    // aligned and sized to match it
    .field-error {
      color: var(--p-red-500);
      font-size: 0.875rem;
      margin-top: 0.25em;
      padding-left: var(--p-iftalabel-position-x, 0.75rem);
    }
  }

  .expansion-panel {
    .expansion-section {
      width: 45%;
      margin-top: 1.5em;

      // keep the stacked IftaLabel fields from smooshing together
      .field {
        margin-top: 1em;
      }

      .trap-message-row,
      .trap-source-address-row {
        display: flex;
        align-items: center;
        gap: 0.75rem;

        .switch-label {
          @include typography.body-small;
        }
      }
    }
  }

  .footer {
    display: flex;
    justify-content: flex-start;
  }
}
</style>
