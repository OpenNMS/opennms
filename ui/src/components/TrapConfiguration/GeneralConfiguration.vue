<template>
  <TableCard class="general-configuration">
    <div class="header">
      <div class="section-left">
        <h3>TrapD Listener Settings</h3>
        <p>General config for TrapD Config</p>
      </div>
    </div>
    <div class="section">
      <FeatherInput
        label="Port"
        placeholder="Enter port number"
        v-model="port"
        :error="trapConfigError.port"
        type="number"
        :hint="'Default: 162'"
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
        :checked="status"
        @click="onChangeStatus"
        data-test="unknown-devices-input"
      />
      <label class="switch-label">Auto-discover unknown devices</label>
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
            :error="trapConfigError.batchSize"
            type="number"
            :hint="'Default: 1000'"
          />
          <div class="spacer"></div>
          <div class="spacer"></div>
          <FeatherInput
            label="Batch Interval"
            placeholder="Enter batch interval"
            v-model="batchInterval"
            :error="trapConfigError.batchInterval"
            type="number"
            :hint="'Default: 500'"
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
  </TableCard>
</template>

<script setup lang="ts">
import useSnackbar from '@/composables/useSnackbar'
import { updateTrapdConfiguration } from '@/services/trapdConfigurationService'
import { useTrapConfigStore } from '@/stores/trapConfigStore'
import { TrapdConfigurationError } from '@/types/trapConfig'
import { FeatherButton } from '@featherds/button'
import { FeatherExpansionPanel } from '@featherds/expansion'
import { FeatherInput } from '@featherds/input'
import { SwitchRender } from '@featherds/switch'
import TableCard from '../Common/TableCard.vue'

const status = ref(false)
const port = ref<number>(162)
const bindAddress = ref('*')
const trapMessageStatus = ref(false)
const trapSourceAddressStatus = ref(false)
const threads = ref<number>(0)
const queueSize = ref<number>(10000)
const batchSize = ref<number>(1000)
const batchInterval = ref<number>(500)
const trapConfigError = ref<TrapdConfigurationError>({})
const isSaveDisabled = ref(false)
const isSaving = ref(false)
const store = useTrapConfigStore()
const { showSnackBar } = useSnackbar()

const onChangeStatus = () => {
  status.value = !status.value
}

const onChangeTrapMessageStatus = () => {
  trapMessageStatus.value = !trapMessageStatus.value
}

const onChangeTrapSourceAddressStatus = () => {
  trapSourceAddressStatus.value = !trapSourceAddressStatus.value
}

const validateInputs = (): TrapdConfigurationError => {
  const trapConfigError: TrapdConfigurationError = {}
  
  if (port.value < 1 || port.value > 65535) {
    trapConfigError.port = 'Port must be between 1 and 65535.'
  }
  
  if (bindAddress.value === '') {
    trapConfigError.bindAddress = 'Bind Address cannot be empty.'
  } else if (bindAddress.value !== '*' && !isValidIP(bindAddress.value)) {
    trapConfigError.bindAddress = 'Bind Address must be * or a valid IP address.'
  }
  
  if (threads.value < 0) {
    trapConfigError.threads = 'Threads cannot be negative.'
  }
  
  if (queueSize.value < 0) {
    trapConfigError.queueSize = 'Queue Size cannot be negative.'
  }
  
  if (batchSize.value < 0) {
    trapConfigError.batchSize = 'Batch Size cannot be negative.'
  }
  
  if (batchInterval.value < 0) {
    trapConfigError.batchInterval = 'Batch Interval cannot be negative.'
  }
  
  return trapConfigError
}

// Add this helper function
const isValidIP = (ip: string): boolean => {
  const parts = ip.split('.')
  if (parts.length !== 4) return false
  return parts.every(part => {
    const num = parseInt(part, 10)
    return !isNaN(num) && num >= 0 && num <= 255
  })
}

const updateConfig = async () => {
  if (isSaveDisabled.value || isSaving.value) {
    return
  }

  const newConfig = {
    snmpTrapPort: port.value,
    snmpTrapAddress: bindAddress.value,
    newSuspectOnTrap: status.value,
    useAddressFromVarbind: trapSourceAddressStatus.value,
    includeRawMessage: trapMessageStatus.value,
    threads: threads.value,
    queueSize: queueSize.value,
    batchSize: batchSize.value,
    batchInterval: batchInterval.value
  }

  try {
    isSaving.value = true

    const response = await updateTrapdConfiguration(newConfig)
    store.trapdConfig = response
    store.SnmpV3Users = response.snmpv3User

    showSnackBar({ msg: 'Trap configuration updated successfully.' })
  } catch (err) {
    const msg = err instanceof Error ? err.message : 'Failed to update trap configuration.'
    showSnackBar({ msg, error: true })
  } finally {
    isSaving.value = false
  }
}

const loadInitialConfig = () => {
  if (store.trapdConfig) {
    port.value = store.trapdConfig.snmpTrapPort || 162
    bindAddress.value = store.trapdConfig.snmpTrapAddress || '*'
    status.value = store.trapdConfig.newSuspectOnTrap || false
    trapSourceAddressStatus.value = store.trapdConfig.useAddressFromVarbind || false
    trapMessageStatus.value = store.trapdConfig.includeRawMessage || false
    threads.value = store.trapdConfig.threads || 0
    queueSize.value = store.trapdConfig.queueSize || 10000
    batchSize.value = store.trapdConfig.batchSize || 1000
    batchInterval.value = store.trapdConfig.batchInterval || 500
  }
}

watchEffect(() => {
  trapConfigError.value = validateInputs()
  isSaveDisabled.value = Object.keys(trapConfigError.value).length > 0
})

onMounted(() => {
  loadInitialConfig()
})

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

