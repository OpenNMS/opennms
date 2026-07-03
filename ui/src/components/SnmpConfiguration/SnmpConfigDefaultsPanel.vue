<template>
  <div class="snmp-config-defaults-panel">
    <div class="main-section">
      <div>
        <h3>SNMP Configuration Default Overrides</h3>
        <span>View and set "global" default override values for SNMP configuration parameters.</span>
        <FeatherIcon
          :icon="InfoIcon"
          class="info-icon"
          @click="isMessageDialogVisible = true"
          data-test="snmp-config-defaults-info-icon"
        />
      </div>

      <div class="large-spacer" />

      <div class="onms-row header-row">
        <div class="onms-col-3">
          <label class="label">Parameter</label>
        </div>
        <div class="onms-col-4">
          <label class="label">System Default Value</label>
        </div>
        <div class="onms-col-5">
          <label class="label">User Defined Overrides</label>
        </div>
      </div>

      <div class="onms-row" v-for="param in parameters" :key="param.key">
        <div class="onms-col-3">
          <label class="label">{{ param.label }}</label>
        </div>
        <div class="onms-col-4">
          <span>{{ param.defaultValue }}</span>
        </div>
        <div class="onms-col-5">
          <!-- Select dropdown for version, securityLevel, authProtocol, privacyProtocol -->
          <FormField
            v-if="param.isSelect"
            :error="(formErrors as any)[param.key]"
            :hint="param.hint"
          >
            <PSelect
              class="snmp-config-defaults-select"
              :data-test="`snmp-config-default-${param.key}`"
              :aria-label="param.label"
              optionLabel="_text"
              showClear
              :options="param.selectOptions"
              :modelValue="selectModel[param.key]"
              :invalid="!!(formErrors as any)[param.key]"
              @update:modelValue="(val: any) => handleSelectUpdate(param.key, val)"
            />
          </FormField>

          <!-- Input with SCV button -->
          <div v-else-if="param.scvEnabled" class="onms-row scv-input-row">
            <div class="onms-col-10">
              <FormField
                :error="(formErrors as any)[param.key]"
                :hint="param.hint"
              >
                <PInputText
                  class="snmp-config-defaults-input"
                  :data-test="`snmp-config-default-${param.key}`"
                  :aria-label="param.label"
                  :type="param.inputType"
                  :invalid="!!(formErrors as any)[param.key]"
                  :modelValue="(formConfig[param.key] as string)"
                  @update:modelValue="(val) => handleInputUpdate(param.key, val ?? undefined)"
                />
              </FormField>
            </div>
            <div class="onms-col-2">
              <div class="scv-icon-container">
                <ScvInputIcon @click="() => onScvButtonClick(param.key)" />
              </div>
            </div>
          </div>

          <!-- Regular numeric input -->
          <FormField
            v-else-if="param.inputType === 'number'"
            :error="(formErrors as any)[param.key]"
            :hint="param.hint"
          >
            <PInputNumber
              class="snmp-config-defaults-input"
              :inputProps="{ 'data-test': `snmp-config-default-${param.key}`, 'aria-label': param.label }"
              :useGrouping="false"
              :invalid="!!(formErrors as any)[param.key]"
              :modelValue="(formConfig[param.key] as number)"
              @update:modelValue="(val) => handleInputUpdate(param.key, val ?? undefined)"
            />
          </FormField>

          <!-- Regular text input -->
          <FormField
            v-else
            :error="(formErrors as any)[param.key]"
            :hint="param.hint"
          >
            <PInputText
              class="snmp-config-defaults-input"
              :data-test="`snmp-config-default-${param.key}`"
              :aria-label="param.label"
              :type="param.inputType"
              :invalid="!!(formErrors as any)[param.key]"
              :modelValue="(formConfig[param.key] as string)"
              @update:modelValue="(val) => handleInputUpdate(param.key, val ?? undefined)"
            />
          </FormField>
        </div>
      </div>

      <div class="onms-row button-row">
        <div class="onms-col-12">
          <PButton
            outlined
            label="Reset to System Defaults"
            @click="onReset"
          />
           <PButton
            label="Save"
            @click="onSave"
          />
        </div>
      </div>
    </div>

    <ScvSearchDrawer
      :isOpen="scvSearchDrawerOpen"
      @hidden="scvSearchDrawerOpen = false"
      @itemSelected="scvItemSelected"
    />
    <MessageDialog
      :visible="isMessageDialogVisible"
      title="SNMP Configuration Defaults"
      @close="isMessageDialogVisible = false"
    >
      <template #content>
        <div class="message-dialog-content-body">
          <h4>View and set "global" default values for SNMP configuration parameters.</h4>
          <p>These are the default values that are used unless you have specifically configured a different value for a profile or definition.</p>
          <p>The Parameter column lists the various SNMP configuration parameters.</p>
          <p>The System Default Value column shows the built-in default values.</p>
          <p>The User Defined Overrides column allows you to specify custom default values that will override the system defaults when creating new definitions or profiles.</p>
          <p>To change a default value, enter an override value in the User Defined Overrides column and click Save.</p>
          <p>To clear an individual override and revert back to using the system default for that parameter, either clear the value in the User Defined Overrides column for that parameter, or else set it to the system default value and click Save.</p>
          <p>To reset all overrides and return to using the system defaults, click the "Reset to System Defaults" button and then click Save to apply the reset.</p>
        </div>
      </template>
    </MessageDialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { isEqual } from 'lodash'
import Button from 'primevue/button'
import InputNumber from 'primevue/inputnumber'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import { FeatherIcon } from '@featherds/icon'
import InfoIcon from '@featherds/icon/action/Info'
import { ISelectItemType } from '@featherds/select'

import FormField from '@/components/Common/FormField.vue'
import ScvInputIcon from '@/components/SCV/ScvInputIcon.vue'
import ScvSearchDrawer from '@/components/SCV/ScvSearchDrawer.vue'
import useSnackbar from '@/composables/useSnackbar'
import {
  DEFAULT_SNMP_VERSION,
  DEFAULT_SNMP_TIMEOUT,
  DEFAULT_SNMP_RETRIES,
  DEFAULT_SNMP_PORT,
  DEFAULT_SNMP_TTL,
  DEFAULT_SNMP_MAX_REQUEST_SIZE,
  DEFAULT_SNMP_MAX_VARS_PER_PDU,
  DEFAULT_SNMP_MAX_REPETITIONS,
  DEFAULT_SNMP_READ_COMMUNITY_STRING,
  DEFAULT_SNMP_WRITE_COMMUNITY_STRING,
  DEFAULT_SNMP_V3_SECURITY_NAME,
  DEFAULT_SNMP_V3_SECURITY_LEVEL,
  DEFAULT_SNMP_V3_AUTH_PASSPHRASE,
  DEFAULT_SNMP_V3_AUTH_PROTOCOL,
  DEFAULT_SNMP_V3_PRIVACY_PASSPHRASE,
  DEFAULT_SNMP_V3_PRIVACY_PROTOCOL
} from '@/lib/constants'
import { SecurityLevelSelectionOptions, SnmpAuthProtocols, SnmpPrivacyProtocols, validateSnmpConfiguration } from '@/lib/snmpValidator'
import { saveSnmpConfigDefaultOverrides } from '@/services/snmpConfigService'
import { getDefaultSnmpBaseConfiguration, useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { ScvSearchItem } from '@/types/scv'
import { SnmpConfigFormErrors, type SnmpBaseConfiguration } from '@/types/snmpConfig'
import MessageDialog from '../Common/MessageDialog.vue'

const PButton = Button
const PInputNumber = InputNumber
const PInputText = InputText
const PSelect = Select

// SNMP Version options
const SnmpVersions: ISelectItemType[] = [
  { _text: 'v1', _value: 'v1' },
  { _text: 'v2c', _value: 'v2c' },
  { _text: 'v3', _value: 'v3' }
]

// Interface for parameter metadata
interface ParameterConfig {
  key: keyof SnmpBaseConfiguration
  label: string
  defaultValue: string | number
  inputType: string
  hint?: string
  isSelect?: boolean
  scvEnabled?: boolean
  selectOptions?: ISelectItemType[]
}

const snmpConfigStore = useSnmpConfigStore()
const snackbar = useSnackbar()
const selectModel = reactive<Record<string, ISelectItemType | undefined>>({})
const scvSearchDrawerOpen = ref(false)
const scvSelectedProperty = ref('')
const formConfig = ref<SnmpBaseConfiguration>({})
const formErrors = ref<SnmpConfigFormErrors>({})
const isMessageDialogVisible = ref(false)

const parameters: ParameterConfig[] = [
  {
    key: 'version',
    label: 'Version',
    defaultValue: DEFAULT_SNMP_VERSION,
    inputType: 'text',
    hint: 'SNMP version (v1, v2c, or v3)',
    isSelect: true,
    selectOptions: SnmpVersions
  },
  {
    key: 'timeout',
    label: 'Timeout',
    defaultValue: DEFAULT_SNMP_TIMEOUT,
    inputType: 'number',
    hint: 'Timeout in milliseconds'
  },
  {
    key: 'retry',
    label: 'Retries',
    defaultValue: DEFAULT_SNMP_RETRIES,
    inputType: 'number',
    hint: 'Number of retries'
  },
  {
    key: 'port',
    label: 'Port',
    defaultValue: DEFAULT_SNMP_PORT,
    inputType: 'number',
    hint: 'SNMP port (default: 161)'
  },
  {
    key: 'ttl',
    label: 'TTL',
    defaultValue: DEFAULT_SNMP_TTL,
    inputType: 'number',
    hint: 'Time to live'
  },
  {
    key: 'maxRequestSize',
    label: 'Max Request Size',
    defaultValue: DEFAULT_SNMP_MAX_REQUEST_SIZE,
    inputType: 'number',
    hint: 'Maximum bytes per PDU request'
  },
  {
    key: 'maxVarsPerPdu',
    label: 'Max Vars Per PDU',
    defaultValue: DEFAULT_SNMP_MAX_VARS_PER_PDU,
    inputType: 'number',
    hint: 'Variables per SNMP request'
  },
  {
    key: 'maxRepetitions',
    label: 'Max Repetitions',
    defaultValue: DEFAULT_SNMP_MAX_REPETITIONS,
    inputType: 'number',
    hint: 'Repetitions per get-bulk request'
  },
  {
    key: 'readCommunity',
    label: 'Read Community String',
    defaultValue: DEFAULT_SNMP_READ_COMMUNITY_STRING,
    inputType: 'text',
    hint: 'Read community string',
    scvEnabled: true
  },
  {
    key: 'writeCommunity',
    label: 'Write Community String',
    defaultValue: DEFAULT_SNMP_WRITE_COMMUNITY_STRING,
    inputType: 'text',
    hint: 'Write community string',
    scvEnabled: true
  },
  {
    key: 'securityName',
    label: 'V3 Security Name',
    defaultValue: DEFAULT_SNMP_V3_SECURITY_NAME,
    inputType: 'text',
    hint: 'SNMP v3 security name',
    scvEnabled: true
  },
  {
    key: 'securityLevel',
    label: 'V3 Security Level',
    defaultValue: DEFAULT_SNMP_V3_SECURITY_LEVEL,
    inputType: 'number',
    hint: 'SNMP v3 security level',
    isSelect: true,
    selectOptions: SecurityLevelSelectionOptions
  },
  {
    key: 'authPassphrase',
    label: 'V3 Auth Passphrase',
    defaultValue: DEFAULT_SNMP_V3_AUTH_PASSPHRASE,
    inputType: 'password',
    hint: 'Authentication passphrase',
    scvEnabled: true
  },
  {
    key: 'authProtocol',
    label: 'V3 Auth Protocol',
    defaultValue: DEFAULT_SNMP_V3_AUTH_PROTOCOL,
    inputType: 'text',
    hint: 'Authentication protocol',
    isSelect: true,
    selectOptions: SnmpAuthProtocols.map(protocol => ({ _text: protocol, _value: protocol }))
  },
  {
    key: 'privacyPassphrase',
    label: 'V3 Privacy Passphrase',
    defaultValue: DEFAULT_SNMP_V3_PRIVACY_PASSPHRASE,
    inputType: 'password',
    hint: 'Privacy passphrase',
    scvEnabled: true
  },
  {
    key: 'privacyProtocol',
    label: 'V3 Privacy Protocol',
    defaultValue: DEFAULT_SNMP_V3_PRIVACY_PROTOCOL,
    inputType: 'text',
    hint: 'Privacy protocol',
    isSelect: true,
    selectOptions: SnmpPrivacyProtocols.map(protocol => ({ _text: protocol, _value: protocol }))
  }
]

const handleInputUpdate = (key: keyof SnmpBaseConfiguration, value?: string | number) => {
  if (value !== undefined) {
    (formConfig.value as any)[key] = value
  } else {
    (formConfig.value as any)[key] = undefined
  }

  handleValidate()
}

const handleSelectUpdate = (key: keyof SnmpBaseConfiguration, val: ISelectItemType | undefined) => {
  selectModel[key as string] = val

  if (val) {
    (formConfig.value as any)[key] = val._value
  } else {
    (formConfig.value as any)[key] = undefined
  }

  handleValidate()
}

const onScvButtonClick = (key: keyof SnmpBaseConfiguration) => {
  scvSelectedProperty.value = key as string
  scvSearchDrawerOpen.value = true
}

const scvItemSelected = (item: ScvSearchItem) => {
  const scvValue = '${scv:' + item.alias + ':' + item.key + '}'

  const key = scvSelectedProperty.value as keyof SnmpBaseConfiguration
  (formConfig.value as any)[key] = scvValue

  scvSearchDrawerOpen.value = false
}

// create a version of an SnmpBaseConfiguration which is suitable for use with snmpValidator,
// i.e. all properties are defined and any empty values are set to undefined
// This allows the user to clear an override value (by setting it to empty string) and have that be treated
// as "use the system default" rather than trying to validate an empty string as a value for that parameter
const normalizeSnmpConfiguration = (value: SnmpBaseConfiguration) => {
  const isValueEmpty = (val: any) => val === '' || val === null || val === undefined

  const result: SnmpBaseConfiguration = {
    version: isValueEmpty(value.version) ? undefined : value.version,
    timeout: isValueEmpty(value.timeout) ? undefined : value.timeout,
    retry: isValueEmpty(value.retry) ? undefined : value.retry,
    port: isValueEmpty(value.port) ? undefined : value.port,
    ttl: isValueEmpty(value.ttl) ? undefined : value.ttl,
    maxRequestSize: isValueEmpty(value.maxRequestSize) ? undefined : value.maxRequestSize,
    maxVarsPerPdu: isValueEmpty(value.maxVarsPerPdu) ? undefined : value.maxVarsPerPdu,
    maxRepetitions: isValueEmpty(value.maxRepetitions) ? undefined : value.maxRepetitions,
    readCommunity: isValueEmpty(value.readCommunity) ? undefined : value.readCommunity,
    writeCommunity: isValueEmpty(value.writeCommunity) ? undefined : value.writeCommunity,
    securityName: isValueEmpty(value.securityName) ? undefined : value.securityName,
    securityLevel: isValueEmpty(value.securityLevel) ? undefined : value.securityLevel,
    authPassphrase: isValueEmpty(value.authPassphrase) ? undefined : value.authPassphrase,
    authProtocol: isValueEmpty(value.authProtocol) ? undefined : value.authProtocol,
    privacyPassphrase: isValueEmpty(value.privacyPassphrase) ? undefined : value.privacyPassphrase,
    privacyProtocol: isValueEmpty(value.privacyProtocol) ? undefined : value.privacyProtocol
  } as SnmpBaseConfiguration

  return result
}

const handleValidate = () => {
  const normalizedConfig = normalizeSnmpConfiguration(formConfig.value)
  const errors = validateSnmpConfiguration(normalizedConfig, String(selectModel.version?._value ?? ''))
  formErrors.value = errors
}

const resetConfig = () => {
  // reset config to current system defaults by clearing any user overrides
  // however for integer values, we use the default values defined in constants.ts
  const config = {
    ...getDefaultSnmpBaseConfiguration(),
    timeout: DEFAULT_SNMP_TIMEOUT,
    retry: DEFAULT_SNMP_RETRIES,
    port: DEFAULT_SNMP_PORT,
    ttl: DEFAULT_SNMP_TTL,
    maxRequestSize: DEFAULT_SNMP_MAX_REQUEST_SIZE,
    maxVarsPerPdu: DEFAULT_SNMP_MAX_VARS_PER_PDU,
    maxRepetitions: DEFAULT_SNMP_MAX_REPETITIONS
  }

  formConfig.value = config
  selectModel.version = undefined
  selectModel.securityLevel = undefined
  selectModel.authProtocol = undefined
  selectModel.privacyProtocol = undefined
}

const onReset = () => {
  resetConfig()
  formErrors.value = {}

  snackbar.showSnackBar({
    msg: 'Form reset to current system defaults. Click "Save" to apply these defaults as overrides, or modify values and save to set custom overrides.'
  })
}

const onSave = async () => {
  handleValidate()

  if (Object.keys(formErrors.value).length !== 0) {
    // There are validation errors, do not proceed with save
    snackbar.showSnackBar({
      msg: 'Please fix validation errors before saving.',
      error: true
    })

    return
  }

  if (isEqual(formConfig.value, snmpConfigStore.config) ||
    isEqual(normalizeSnmpConfiguration(formConfig.value), normalizeSnmpConfiguration(snmpConfigStore.config))) {
    snackbar.showSnackBar({
      msg: 'No changes to save.'
    })
    return
  }

  try {
    const result = await saveSnmpConfigDefaultOverrides(normalizeSnmpConfiguration(formConfig.value))

    if (result.success) {
      snackbar.showSnackBar({
        msg: 'SNMP Configuration Defaults saved successfully.'
      })

      await snmpConfigStore.populateSnmpConfig()
    } else {
      snackbar.showSnackBar({
        msg: 'Failed to save SNMP Configuration Defaults.',
        error: true
      })
    }
  } catch (err) {
    snackbar.showSnackBar({
      msg: `Error saving SNMP Configuration Defaults: ${err}`,
      error: true
    })
  }
}

const loadConfig = () => {
  // Initialize userConfig with values from snmpConfigStore.config
  // These values will be undefined if they have not been overridden
  formConfig.value = {
    version: snmpConfigStore.config.version,
    timeout: snmpConfigStore.config.timeout,
    retry: snmpConfigStore.config.retry,
    port: snmpConfigStore.config.port,
    ttl: snmpConfigStore.config.ttl,
    maxRequestSize: snmpConfigStore.config.maxRequestSize,
    maxVarsPerPdu: snmpConfigStore.config.maxVarsPerPdu,
    maxRepetitions: snmpConfigStore.config.maxRepetitions,
    readCommunity: snmpConfigStore.config.readCommunity,
    writeCommunity: snmpConfigStore.config.writeCommunity,
    securityName: snmpConfigStore.config.securityName,
    securityLevel: snmpConfigStore.config.securityLevel,
    authPassphrase: snmpConfigStore.config.authPassphrase,
    authProtocol: snmpConfigStore.config.authProtocol,
    privacyPassphrase: snmpConfigStore.config.privacyPassphrase,
    privacyProtocol: snmpConfigStore.config.privacyProtocol
  }

  // Initialize select models
  if (snmpConfigStore.config.version) {
    selectModel.version = SnmpVersions.find(v => v._value === snmpConfigStore.config.version)
  }
  if (snmpConfigStore.config.securityLevel !== undefined) {
    selectModel.securityLevel = SecurityLevelSelectionOptions.find(v => v._value === String(snmpConfigStore.config.securityLevel))
  }
  if (snmpConfigStore.config.authProtocol) {
    selectModel.authProtocol = { _text: snmpConfigStore.config.authProtocol, _value: snmpConfigStore.config.authProtocol }
  }
  if (snmpConfigStore.config.privacyProtocol) {
    selectModel.privacyProtocol = { _text: snmpConfigStore.config.privacyProtocol, _value: snmpConfigStore.config.privacyProtocol }
  }
}

watch(() => snmpConfigStore.config, () => {
  loadConfig()
})

onMounted(() => {
  loadConfig()
})
</script>

<style lang="scss" scoped>
.snmp-config-defaults-panel {
  .main-section {
    max-width: 80em;
    padding: 1.5em;
  }

  .large-spacer {
    height: 1.5rem;
  }

  .label {
    font-weight: 600;
    display: flex;
    align-items: center;
    height: 100%;
  }

  p.section-description {
    max-width: 70em;
    margin-top: 0.5rem;
    margin-bottom: 1.5rem;
  }

  .snmp-config-defaults-input,
  .snmp-config-defaults-select {
    min-width: 20rem;

    :deep(.p-inputtext) {
      width: 100%;
    }
  }

  .onms-row {
    margin-bottom: 1.25rem;
    align-items: center;

    &.header-row {
      border-bottom: 1px solid var(--p-content-border-color);
      padding-bottom: 0.5rem;
      margin-bottom: 1rem;
    }

    &.scv-input-row {
      margin-bottom: 0;
    }

    &.button-row {
      button.p-button {
        font-size: 1rem;
        margin-right: 0.5rem;
        min-width: 8rem;
      }
    }
  }

  .info-icon {
    font-size: 1.5em;
    color: var(--p-text-color);
    margin-left: 0.25em;
    cursor: pointer;
  }

  .onms-col-4 {
    display: flex;
    align-items: center;
  }

  .scv-icon-container {
    padding: 0.2em;
    margin-left: 0.5em;
    display: flex;
    align-items: center;
    cursor: pointer;
  }
}

.message-dialog-content-body {
  max-width: 60em;
  overflow-y: auto;

  p {
    margin-top: 1rem;
  }
}
</style>
