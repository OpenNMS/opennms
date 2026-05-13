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
      <FeatherInput
        label="Port"
        placeholder="Enter port number"
        v-model="port"
        :min="MIN_PORT"
        :max="MAX_PORT"
        :error="trapConfigError.port"
        type="number"
        :hint="'Default: 10162'"
      />
      <FeatherInput
        label="Bind Address"
        placeholder="Enter host name"
        v-model="bindAddress"
        :error="trapConfigError.bindAddress"
        :hint="'* for all, or specify IP address'"
      />
    </div>
    <div class="spacer"></div>
    <div class="spacer"></div>
    <div class="switch-row">
      <SwitchRender
        :checked="newSuspectOnTrap"
        @click="onChangeNewSuspectOnTrap"
        data-test="unknown-devices-input"
      />
      <label class="switch-label">Create new nodes when receiving an SNMP trap with an unknown source IP address?</label>
    </div>
    <div class="spacer"></div>
    <div class="spacer"></div>
    <div class="expansion-panel">
      <FeatherExpansionPanel title="Advanced Configuration Options">
        <div class="expansion-section">
          <div class="spacer"></div>
          <div class="spacer"></div>
          <div class="trap-message-row">
            <SwitchRender
              :checked="trapMessageStatus"
              @click="onChangeTrapMessageStatus"
              data-test="trap-message-input"
            />
            <label class="switch-label">Include raw trap message (before processing)</label>
          </div>
          <div class="spacer"></div>
          <div class="spacer"></div>
          <div class="trap-source-address-row">
            <SwitchRender
              :checked="trapSourceAddressStatus"
              @click="onChangeTrapSourceAddressStatus"
              data-test="trap-source-address-input"
            />
            <label class="switch-label">Use forwarded trap source address (for forwarded SNMPv2 traps)</label>
          </div>
          <div class="spacer"></div>
          <div class="spacer"></div>
          <FeatherInput
            label="Threads"
            placeholder="Enter number of threads"
            v-model="threads"
            :min="0"
            :error="trapConfigError.threads"
            type="number"
            :hint="'Default: 0'"
          />
          <div class="spacer"></div>
          <div class="spacer"></div>
          <FeatherInput
            label="Queue Size"
            placeholder="Enter queue size"
            v-model="queueSize"
            :min="0"
            :error="trapConfigError.queueSize"
            type="number"
            :hint="'Default: 10000'"
          />
          <div class="spacer"></div>
          <div class="spacer"></div>
          <FeatherInput
            label="Batch Size"
            placeholder="Enter batch size"
            v-model="batchSize"
            :min="0"
            :error="trapConfigError.batchSize"
            type="number"
            :hint="'Default: 1000'"
          />
          <div class="spacer"></div>
          <div class="spacer"></div>
          <FeatherInput
            label="Batch Interval ms"
            placeholder="Enter batch interval in ms"
            v-model="batchInterval"
            :min="0"
            :error="trapConfigError.batchInterval"
            type="number"
            :hint="'Default: 500ms'"
          />
          <div class="spacer"></div>
          <div class="spacer"></div>
        </div>
      </FeatherExpansionPanel>
    </div>
    <div class="spacer"></div>
    <div class="spacer"></div>
    <div class="footer">
      <FeatherButton
        primary
        data-test="save-button"
        :disabled="isSaveDisabled || isSaving"
        @click="updateConfig"
      >
        Update Changes
      </FeatherButton>
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
import { isEqual } from 'lodash'
import { FeatherButton } from '@featherds/button'
import { FeatherExpansionPanel } from '@featherds/expansion'
import { FeatherIcon } from '@featherds/icon'
import InfoIcon from '@featherds/icon/action/Info'
import { FeatherInput } from '@featherds/input'
import { SwitchRender } from '@featherds/switch'
import useSnackbar from '@/composables/useSnackbar'
import { DEFAULT_TRAPD_BATCH_INTERVAL, DEFAULT_TRAPD_BATCH_SIZE, DEFAULT_TRAPD_BIND_ADDRESS, DEFAULT_TRAPD_INCLUDE_RAW_MESSAGE, DEFAULT_TRAPD_NEW_SUSPECT_ON_TRAP, DEFAULT_TRAPD_PORT, DEFAULT_TRAPD_QUEUE_SIZE, DEFAULT_TRAPD_THREADS, DEFAULT_TRAPD_USE_ADDRESS_FROM_VARBIND } from '@/lib/constants'
import { isValidIP, isValidPort, MAX_PORT, MIN_PORT } from '@/lib/trapdValidator'
import { updateTrapdConfiguration } from '@/services/trapdConfigurationService'
import { useTrapdConfigStore } from '@/stores/trapdConfigStore'
import { TrapConfig, TrapdConfigurationError } from '@/types/trapConfig'
import MessageDialog from '../Common/MessageDialog.vue'
import TableCard from '../Common/TableCard.vue'

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

const onChangeNewSuspectOnTrap = () => {
  newSuspectOnTrap.value = !newSuspectOnTrap.value
}

const onChangeTrapMessageStatus = () => {
  trapMessageStatus.value = !trapMessageStatus.value
}

const onChangeTrapSourceAddressStatus = () => {
  trapSourceAddressStatus.value = !trapSourceAddressStatus.value
}

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
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';

.general-configuration {
  margin-top: 10px;
  padding: 25px;
  border: 1px solid var(--feather-border-on-surface);

  .header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;

    .section-left {
      h3 {
        @include typography.headline3;
        color: var(--feather-text-primary);
      }

      p {
        @include typography.body-large;
        color: var(--feather-text-secondary);
      }
    }
  }

  .spacer {
    height: 0.5em;
  }

  .info-section {
    margin-bottom: 1em;

    .label {
      color: var(variables.$primary-text-on-surface);
    }

    .info-icon {
      cursor: pointer;
      font-size: 1.5em;
      margin-left: 0.5em;
      color: var(variables.$primary);

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
    align-items: center;
    gap: 20px;
    width: 50%;

    &>* {
      flex: 1;
    }
  }

  .expansion-panel {
    :deep(.feather-expansion) {
      [role="heading"] {
        background-color: rgba(10, 12, 27, 0.12);
        border: 1px solid var(--feather-border-on-surface);

        a {
          span {
            @include typography.headline4;
          }
        }
      }

      .expansion-section {
        width: 45%;

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
  }

  .footer {
    display: flex;
    justify-content: flex-end;
  }
}
</style>
