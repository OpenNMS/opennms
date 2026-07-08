<template>
  <div class="snmp-config-definition-details">
    <PCard v-if="props.displayIps" class="ip-range-card">
      <template #title>
        <h4>Add more IP ranges to the configuration</h4>
      </template>
      <template #content>
        <div class="onms-row">
          <div class="onms-col-6">
            <div class="onms-row">
              <div class="onms-col-6">
                <FormField
                  label="First or Specific IP Address"
                  :for="`${uid}-first-ip`"
                  :error="errors.firstIpAddress"
                  hint="First IP Address in range or specific IP"
                >
                  <PInputText
                    :id="`${uid}-first-ip`"
                    class="ip-input"
                    data-test="snmp-definition-first-ip-address"
                    :invalid="!!errors.firstIpAddress"
                    v-model.trim="firstIpAddress"
                  />
                </FormField>
              </div>
              <div class="onms-col-6">
                <FormField
                  label="Last IP Address (for IP Range)"
                  :for="`${uid}-last-ip`"
                  :error="errors.lastIpAddress"
                  hint="Last IP Address in range (leave blank if not a range)"
                >
                  <PInputText
                    :id="`${uid}-last-ip`"
                    class="ip-input"
                    data-test="snmp-definition-last-ip-address"
                    :invalid="!!errors.lastIpAddress"
                    v-model.trim="lastIpAddress"
                  />
                </FormField>
              </div>
            </div>
          </div>
          <div class="onms-col-6">
            <div class="onms-row">
              <div class="onms-col-6">
                <FormField
                  label="IPLIKE Expression"
                  :for="`${uid}-ipmatch`"
                  :error="errors.ipMatch"
                  hint="IPLIKE Expression (cannot be used with First/Last IP)"
                >
                  <PInputText
                    :id="`${uid}-ipmatch`"
                    class="ip-input"
                    data-test="snmp-definition-ipmatch-expression"
                    :invalid="!!errors.ipMatch"
                    v-model.trim="ipMatchValue"
                  />
                </FormField>
              </div>
              <div class="onms-col-6 add-range-col">
                <PButton
                  label="Add"
                  :disabled="!firstIpAddress && !lastIpAddress && !ipMatchValue"
                  @click="onAddRange"
                  data-test="add-definition-range-button"
                />
              </div>
            </div>
          </div>
        </div>
        <div class="onms-row" v-if="errors.invalidRangeConfig">
          <div class="onms-col-12">
            <span class="label text-danger">{{ errors.invalidRangeConfig }}</span>
          </div>
        </div>
      </template>
    </PCard>

    <div class="onms-row">
      <div class="onms-col-6" v-if="!props.suppressMonitoringLocation">
        <FormField
          label="Monitoring Location"
          for="snmp-monitoring-location-select"
        >
          <PSelect
            inputId="snmp-monitoring-location-select"
            class="dropdown-select"
            data-test="snmp-monitoring-location-select"
            optionLabel="_text"
            :options="monitoringLocations"
            :modelValue="selectedMonitoringLocation"
            @update:modelValue="(val: any) => selectedMonitoringLocation = val"
          />
        </FormField>
      </div>
      <div :class="!props.suppressMonitoringLocation ? 'onms-col-6' : 'onms-col-12'">
        <div class="dropdown">
          <FormField
            label="Version"
            for="snmp-definition-version"
          >
            <PSelect
              inputId="snmp-definition-version"
              class="dropdown-select"
              data-test="snmp-definition-version"
              optionLabel="_text"
              :options="SnmpVersions"
              :modelValue="snmpVersion"
              @update:modelValue="onSnmpVersionUpdated"
            />
          </FormField>
        </div>
      </div>
    </div>

    <div class="large-spacer"></div>

    <TogglePanel
      v-if="displaySnmp2Params"
      class="snmp-config-expansion-panel"
      header="SNMP v1/v2c Parameters"
      :collapsed="!snmpV2Expanded"
      @update:collapsed="v => snmpV2Expanded = !v"
    >
      <div class="large-spacer"></div>
      <SnmpConfigPairedFieldInputs
        :fieldInfo="snmpV2Fields"
        :config="formConfig"
        :validationErrors="errors"
        @update="onFieldUpdate"
        @scvSearch="onScvButtonClick"
      />
    </TogglePanel>

    <TogglePanel
      v-if="displaySnmp3Params"
      class="snmp-config-expansion-panel"
      header="SNMP v3 Parameters"
      :collapsed="!snmpV3Expanded"
      @update:collapsed="v => snmpV3Expanded = !v"
    >
      <div class="large-spacer"></div>
      <SnmpConfigPairedFieldInputs
        :fieldInfo="snmpV3Fields"
        :config="formConfig"
        :validationErrors="errors"
        @update="onFieldUpdate"
        @scvSearch="onScvButtonClick"
      />

      <div class="large-spacer"></div>

      <div class="show-context-fields-row">
        <PCheckbox
          inputId="snmp-definition-show-context-fields-checkbox"
          data-test="snmp-definition-show-context-fields-checkbox"
          binary
          v-model="displaySnmpV3ContextFields"
        />
        <label
          for="snmp-definition-show-context-fields-checkbox"
          class="show-context-fields-label"
        >Show Context Fields</label>
      </div>

      <div class="large-spacer"></div>

      <SnmpConfigPairedFieldInputs
        v-if="displaySnmpV3ContextFields"
        :fieldInfo="snmpV3ContextFields"
        :config="formConfig"
        :validationErrors="errors"
        @update="onFieldUpdate"
        @scvSearch="onScvButtonClick"
      />
    </TogglePanel>

    <div class="large-spacer"></div>

    <div class="onms-row">
      <div class="onms-col-12">
        <h4>General Parameters</h4>
      </div>
    </div>

    <SnmpConfigPairedFieldInputs
      :fieldInfo="generalParamFields"
      :config="formConfig"
      :validationErrors="errors"
      @update="onFieldUpdate"
      @scvSearch="onScvButtonClick"
    />

    <div class="large-spacer"></div>

    <TogglePanel
      class="snmp-config-expansion-panel"
      header="Advanced Parameters"
      :collapsed="!displayAdvancedConfig"
      @update:collapsed="v => displayAdvancedConfig = !v"
    >
      <div class="large-spacer"></div>
      <SnmpConfigPairedFieldInputs
        :fieldInfo="advancedConfigOptions"
        :config="formConfig"
        :validationErrors="errors"
        @update="onFieldUpdate"
        @scvSearch="onScvButtonClick"
      />
    </TogglePanel>

    <div class="large-spacer"></div>

    <div class="onms-row">
      <div class="onms-col-12">
        <div class="action-container">
          <PButton
            :label="isCreate ? 'Create Definition' : 'Save Changes'"
            @click="handleSave"
            data-test="save-definition-button"
          />
          <PButton
            outlined
            label="Cancel"
            @click="handleCancel"
            data-test="cancel-snmp-definition-button"
          />
        </div>
      </div>
    </div>

    <ScvSearchDrawer
      :isOpen="scvSearchDrawerOpen"
      @hidden="scvSearchDrawerOpen = false"
      @itemSelected="scvItemSelected"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, useId, watch } from 'vue'

import Button from 'primevue/button'
import Card from 'primevue/card'
import Checkbox from 'primevue/checkbox'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import { ISelectItemType } from '@featherds/select'
import { DEFAULT_MONITORING_LOCATION, DEFAULT_SNMP_V3_SECURITY_LEVEL } from '@/lib/constants'
import { getDefaultSnmpBaseConfiguration, useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { SnmpAgentConfig, SnmpBaseConfiguration, SnmpConfigFormErrors, SnmpFieldInfo, SnmpSecurityLevel } from '@/types/snmpConfig'
import { validateDefinition, SecurityLevelSelectionOptions, SnmpAuthProtocols, SnmpPrivacyProtocols } from '@/lib/snmpValidator'
import { withDefaultHints } from '@/lib/snmpConfigHelpers'
import FormField from '@/components/Common/FormField.vue'
import SnmpConfigPairedFieldInputs from './SnmpConfigPairedFieldInputs.vue'
import TogglePanel from '../Common/TogglePanel.vue'
import ScvSearchDrawer from '../SCV/ScvSearchDrawer.vue'
import { ScvSearchItem } from '@/types/scv'

const PButton = Button
const PCard = Card
const PCheckbox = Checkbox
const PInputText = InputText
const PSelect = Select

// Unique per-instance prefix for label `for`/input `id` pairs (multiple detail
// panels stay mounted across PrimeVue tab panels).
const uid = useId()

const props = defineProps<{
  isCreate: boolean,
  displayIps: boolean,
  suppressMonitoringLocation?: boolean,
  firstIp: string,
  lastIp?: string,
  ipMatch?: string,
  config?: SnmpAgentConfig
}>()

const emit = defineEmits<{
  (e: 'cancel'): void
  (e: 'save', config: SnmpAgentConfig, firstIp?: string, lastIp?: string, ipMatch?: string): void
  (e: 'add-range', firstIp: string, lastIp: string, ipMatch: string): void
  (e: 'validation-error', formErrors: SnmpConfigFormErrors): void
}>()

const SnmpVersions: ISelectItemType[] = [
  { _text: 'v1', _value: 'v1' },
  { _text: 'v2c', _value: 'v2c' },
  { _text: 'v3', _value: 'v3' }
]

const store = useSnmpConfigStore()
const snmpVersion = ref()
const isLoading = ref(true)
const isValid = ref(false)
const errors = ref<SnmpConfigFormErrors>({})

// local data for form inputs
const firstIpAddress = ref('')
const lastIpAddress = ref('')
const ipMatchValue = ref('')
const selectedMonitoringLocation = ref<ISelectItemType>({ _text: DEFAULT_MONITORING_LOCATION, _value: DEFAULT_MONITORING_LOCATION })
const formConfig = reactive<SnmpBaseConfiguration>(getDefaultSnmpBaseConfiguration())
const scvSearchDrawerOpen = ref(false)
const scvSelectedProperty = ref('')

const snmpV2Expanded = ref(false)
const snmpV3Expanded = ref(false)
const displayAdvancedConfig = ref(false)
const displaySnmpV3ContextFields = ref(false)

const monitoringLocations = computed<ISelectItemType[]>(() => {
  return store.monitoringLocations.map((loc) => {
    return {
      _text: loc.name,
      _value: loc.name
    }
  })
})

const displaySnmp2Params = computed(() => {
  const version = String(snmpVersion.value?._value || '')
  return version === 'v1' || version === 'v2c'
})

const displaySnmp3Params = computed(() => {
  const version = String(snmpVersion.value?._value || '')
  return version === 'v3'
})

// Field metadata for v-for rendering using SnmpConfigPairedFieldInputs
const generalParamFields = computed<SnmpFieldInfo[]>(() => withDefaultHints([
  { key: 'timeout', label: 'Timeout', hint: 'Timeout in milliseconds', dataTest: 'snmp-definition-timeout', isNumeric: true },
  { key: 'retry', label: 'Retries', hint: 'Number of retries', dataTest: 'snmp-definition-retry', isNumeric: true }
], store.currentDefaults))

const advancedConfigOptions = computed<SnmpFieldInfo[]>(() => withDefaultHints([
  { key: 'port', label: 'Port', hint: 'SNMP port', dataTest: 'snmp-definition-port', isNumeric: true },
  { key: 'proxyHost', label: 'Proxy Host', hint: 'Proxy host for SNMP communication', dataTest: 'snmp-definition-proxy-host' },
  { key: 'maxRequestSize', label: 'Max Request Size', hint: 'Maximum bytes per PDU request', dataTest: 'snmp-definition-max-request-size', isNumeric: true },
  { key: 'maxVarsPerPdu', label: 'Max Vars Per PDU', hint: 'Variables per SNMP request', dataTest: 'snmp-definition-max-vars-per-pdu', isNumeric: true },
  { key: 'maxRepetitions', label: 'Max Repetitions', hint: 'Repetitions per get-bulk request', dataTest: 'snmp-definition-max-repetitions', isNumeric: true },
  { key: 'ttl', label: 'TTL', hint: 'Time to live', dataTest: 'snmp-definition-ttl', isNumeric: true }
], store.currentDefaults))

const snmpV2Fields = computed<SnmpFieldInfo[]>(() => withDefaultHints([
  { key: 'readCommunity', label: 'Read Community String', hint: 'Read community string', dataTest: 'snmp-lookup-read-community', scvEnabled: true },
  { key: 'writeCommunity', label: 'Write Community String', hint: 'Write community string', dataTest: 'snmp-lookup-write-community', scvEnabled: true }
], store.currentDefaults))

// Security-level gating: NoAuthNoPriv (1) disables all auth + privacy fields;
// AuthNoPriv (2) disables only the privacy fields; AuthPriv (3) enables all.
// Reads formConfig.securityLevel reactively, so this drives both the disabled
// state of the fields (on initial load and on Security Level change) and the
// clearing of those fields on save.
const authFieldsDisabled = computed(() => Number(formConfig.securityLevel) === SnmpSecurityLevel.NoAuthNoPriv)
const privacyFieldsDisabled = computed(() => {
  const level = Number(formConfig.securityLevel)
  return level === SnmpSecurityLevel.NoAuthNoPriv || level === SnmpSecurityLevel.AuthNoPriv
})

const snmpV3Fields = computed<SnmpFieldInfo[]>(() => {
  const authDisabled = authFieldsDisabled.value
  const privacyDisabled = privacyFieldsDisabled.value

  return withDefaultHints([
    { key: 'securityName', label: 'Security Name', hint: 'SNMP v3 security name', dataTest: 'snmp-definition-security-name', scvEnabled: true },
    {
      key: 'securityLevel', label: 'Security Level', hint: 'SNMP v3 security level', dataTest: 'snmp-definition-security-level', isNumeric: true,
      isSelect: true, selectOptions: SecurityLevelSelectionOptions
    },
    { key: 'authPassphrase', label: 'Auth Passphrase', hint: 'Authentication passphrase', dataTest: 'snmp-definition-auth-passphrase', scvEnabled: true,
      skipDefaultHint: true, disabled: authDisabled
    },
    {
      key: 'authProtocol', label: 'Auth Protocol', hint: 'Authentication protocol', dataTest: 'snmp-definition-auth-protocol',
      isSelect: true, selectOptions: SnmpAuthProtocols.map(protocol => ({ _text: protocol, _value: protocol })), disabled: authDisabled
    },
    { key: 'privacyPassphrase', label: 'Privacy Passphrase', hint: 'Privacy passphrase', dataTest: 'snmp-definition-privacy-passphrase', scvEnabled: true,
      skipDefaultHint: true, disabled: privacyDisabled
    },
    {
      key: 'privacyProtocol', label: 'Privacy Protocol', hint: 'Privacy protocol', dataTest: 'snmp-definition-privacy-protocol',
      isSelect: true, selectOptions: SnmpPrivacyProtocols.map(protocol => ({ _text: protocol, _value: protocol })), disabled: privacyDisabled
    }
  ], store.currentDefaults)
})

const snmpV3ContextFields = computed<SnmpFieldInfo[]>(() => withDefaultHints([
  { key: 'engineId', label: 'Engine ID', hint: 'SNMP engine ID', dataTest: 'snmp-definition-engine-id' },
  { key: 'contextEngineId', label: 'Context Engine ID', hint: 'Context engine ID', dataTest: 'snmp-definition-context-engine-id' },
  { key: 'contextName', label: 'Context Name', hint: 'SNMP context name', dataTest: 'snmp-definition-context-name' },
  { key: 'enterpriseId', label: 'Enterprise ID', hint: 'Enterprise ID', dataTest: 'snmp-definition-enterprise-id' }
], store.currentDefaults))

const scvEnabledKeys = computed<Set<string>>(() => {
  const allFields = [
    ...snmpV2Fields.value,
    ...snmpV3Fields.value,
    ...snmpV3ContextFields.value,
    ...generalParamFields.value,
    ...advancedConfigOptions.value
  ]
  return new Set(allFields.filter(f => f.scvEnabled).map(f => f.key))
})

const loadInitialValues = () => {
  const currentConfig: SnmpAgentConfig = props.config ?? getDefaultSnmpBaseConfiguration()

  if (currentConfig.version === 'v1') {
    snmpVersion.value = SnmpVersions[0]
  } else if (currentConfig.version === 'v2c') {
    snmpVersion.value = SnmpVersions[1]
  } else if (currentConfig.version === 'v3') {
    snmpVersion.value = SnmpVersions[2]
  }

  firstIpAddress.value = props.firstIp || ''
  lastIpAddress.value =  props.lastIp || ''
  ipMatchValue.value = props.ipMatch || ''
  selectedMonitoringLocation.value = { _text: DEFAULT_MONITORING_LOCATION, _value: DEFAULT_MONITORING_LOCATION }

  // Load all config fields into formConfig
  Object.assign(formConfig, {
    version: currentConfig.version ?? '',
    readCommunity: currentConfig.readCommunity ?? '',
    writeCommunity: currentConfig.writeCommunity ?? '',
    timeout: currentConfig.timeout ?? undefined,
    retry: currentConfig.retry ?? undefined,
    port: currentConfig.port ?? undefined,
    proxyHost: currentConfig.proxyHost ?? '',
    maxRequestSize: currentConfig.maxRequestSize ?? undefined,
    maxVarsPerPdu: currentConfig.maxVarsPerPdu ?? undefined,
    maxRepetitions: currentConfig.maxRepetitions ?? undefined,
    ttl: currentConfig.ttl ?? undefined,
    securityName: currentConfig.securityName ?? '',
    securityLevel: currentConfig.securityLevel || DEFAULT_SNMP_V3_SECURITY_LEVEL,
    authPassphrase: currentConfig.authPassphrase ?? '',
    authProtocol: currentConfig.authProtocol ?? '',
    engineId: currentConfig.engineId ?? '',
    contextEngineId: currentConfig.contextEngineId ?? '',
    contextName: currentConfig.contextName ?? '',
    privacyPassphrase: currentConfig.privacyPassphrase ?? '',
    privacyProtocol: currentConfig.privacyProtocol ?? '',
    enterpriseId: currentConfig.enterpriseId ?? ''
  })

  if (displaySnmp2Params.value) {
    snmpV2Expanded.value = true
  } else if (displaySnmp3Params.value) {
    snmpV3Expanded.value = true
  }
}

const onSnmpVersionUpdated = (val: ISelectItemType | undefined) => {
  snmpVersion.value = val

  if (val?._value === 'v3') {
    snmpV3Expanded.value = true
    snmpV2Expanded.value = false
  } else {
    snmpV2Expanded.value = true
    snmpV3Expanded.value = false
  }

  Object.assign(formConfig, {
    version: String(val?._value || '')
  })
}

const onFieldUpdate = (updatedConfig: SnmpBaseConfiguration) => {
  Object.assign(formConfig, updatedConfig)

  if (!isLoading.value) {
    handleValidate(false)
  }
}

const onAddRange = () => {
  handleValidate(false)

  if (!isValid.value) {
    emit('validation-error', errors.value)
    return
  }

  emit('add-range', firstIpAddress.value, lastIpAddress.value, ipMatchValue.value)
}

const onScvButtonClick = (key: string) => {
  scvSelectedProperty.value = key
  scvSearchDrawerOpen.value = true
}

const scvItemSelected = (item: ScvSearchItem) => {
  const scvValue = '${scv:' + item.alias + ':' + item.key + '}'

  Object.assign(formConfig, {
    [scvSelectedProperty.value]: scvValue
  })

  scvSearchDrawerOpen.value = false
}

const handleSave = async () => {
  handleValidate(true)

  if (!isValid.value) {
    emit('validation-error', errors.value)
    return
  }

  const formToSave: SnmpBaseConfiguration = {
    version: formConfig.version,
    readCommunity: formConfig.readCommunity ?? undefined,
    writeCommunity: formConfig.writeCommunity ?? undefined,
    timeout: formConfig.timeout,
    retry: formConfig.retry,
    port: formConfig.port,
    proxyHost: formConfig.proxyHost ?? undefined,
    maxRequestSize: formConfig.maxRequestSize,
    maxVarsPerPdu: formConfig.maxVarsPerPdu,
    maxRepetitions: formConfig.maxRepetitions,
    ttl: formConfig.ttl,
    securityName: formConfig.securityName ?? undefined,
    securityLevel: formConfig.securityLevel,
    // Clear auth/privacy fields that the current security level disables, so a
    // saved config never carries stale credentials the level doesn't permit.
    authPassphrase: authFieldsDisabled.value ? undefined : (formConfig.authPassphrase ?? undefined),
    authProtocol: authFieldsDisabled.value ? undefined : (formConfig.authProtocol ?? undefined),
    engineId: formConfig.engineId ?? undefined,
    contextEngineId: formConfig.contextEngineId ?? undefined,
    contextName: formConfig.contextName ?? undefined,
    privacyPassphrase: privacyFieldsDisabled.value ? undefined : (formConfig.privacyPassphrase ?? undefined),
    privacyProtocol: privacyFieldsDisabled.value ? undefined : (formConfig.privacyProtocol ?? undefined),
    enterpriseId: formConfig.enterpriseId ?? undefined
  }

  try {
    let configToSave: SnmpAgentConfig = {
      ...formToSave,
      location: String(selectedMonitoringLocation.value?._value ?? ''),
      id: props.config?.id
    }

    emit('save', configToSave, firstIpAddress.value, lastIpAddress.value, ipMatchValue.value)
  } catch (error) {
    console.error(error)
  }
}

const handleCancel = () => {
  emit('cancel')
}

const handleValidate = (isSaving: boolean) => {
  const version = String(snmpVersion.value?._value || '')

  const currentErrors = validateDefinition(
    formConfig,
    version,
    firstIpAddress.value,
    lastIpAddress.value,
    ipMatchValue.value,
    isSaving,
    scvEnabledKeys.value
  )

  errors.value = currentErrors as SnmpConfigFormErrors
  isValid.value = Object.getOwnPropertyNames(currentErrors).length === 0

  if (!isValid.value) {
    emit('validation-error', errors.value)
  }
}

const clearIpFields = () => {
  firstIpAddress.value = ''
  lastIpAddress.value = ''
  ipMatchValue.value = ''
}

defineExpose({
  clearIpFields
})

watch([() => props.config, () => props.isCreate], () => {
  isLoading.value = true
  loadInitialValues()
  isLoading.value = false
})

// this does not always seem to trigger, so parent can call 'clearIpFields' via defineExpose
// to ensure fields are cleared when switching between definitions with different IP configurations
watch([() => props.firstIp, () => props.lastIp, () => props.ipMatch], () => {
  if (props.displayIps) {
    firstIpAddress.value = props.firstIp || ''
    lastIpAddress.value = props.lastIp || ''
    ipMatchValue.value = props.ipMatch || ''
  }
})

onMounted(() => {
  isLoading.value = true
  loadInitialValues()
  isLoading.value = false
})
</script>

<style scoped lang="scss">
.snmp-config-definition-details {
  .label {
    font-weight: 600;
  }

  .text-danger {
    color: var(--p-red-500);
  }

  .show-context-fields-row {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .show-context-fields-label {
    font-weight: 500;
    cursor: pointer;
  }

  .dropdown {
    width: 50%;
  }

  // inputs/selects should fill their grid column
  .ip-input,
  .dropdown-select {
    width: 100%;

    :deep(.p-inputtext) {
      width: 100%;
    }
  }

  // Align the Add button with the IPLIKE input. The input's column also holds a
  // hint below it (which can wrap), so centering against the full cell pushes the
  // button too low. Instead top-align the button and match the input height, so
  // the button and input line up (and their centers coincide).
  .add-range-col {
    display: flex;
    align-items: flex-start;

    :deep(.p-button) {
      height: 3rem;
      min-width: 8rem;
      margin-top: 0.5rem;
    }
  }

  .onms-row {
    margin-bottom: 0.5rem;
  }

  .large-spacer {
    min-height: 1em;
  }

  .spacer {
    min-height: 0.5em;
  }

  .snmp-config-expansion-panel {
    width: 100%;
  }

  .action-container {
    display: flex;
    justify-content: flex-start;
    gap: 10px;
  }

  .ip-range-card {
    margin-top: 0.5rem;
    margin-bottom: 1rem;

    h4 {
      margin: 0;
    }
  }
}
</style>
