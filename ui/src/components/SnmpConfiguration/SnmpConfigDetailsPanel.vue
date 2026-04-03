<template>
  <div class="snmp-config-definition-details">
    <FeatherCard v-if="props.displayIps" title="Add more IP ranges to the configuration" class="ip-range-card">
      <div class="feather-row">
        <div class="feather-col-6">
          <div class="feather-row">
            <div class="feather-col-6">
              <label class="label">First or Specific IP Address:</label>
            </div>
            <div class="feather-col-6">
              <label class="label">Last IP Address (for IP Range):</label>
            </div>
          </div>
        </div>
        <div class="feather-col-6">
          <label class="label">IPLIKE Expression:</label>
        </div>
      </div>
      <div class="feather-row">
        <div class="feather-col-6">
          <div class="feather-row">
            <div class="feather-col-6">
              <FeatherInput
                label=""
                data-test="snmp-definition-first-ip-address"
                :error="errors.firstIpAddress ?? errors.invalidRangeConfig"
                v-model.trim="firstIpAddress"
                hint="First IP Address in range or specific IP"
              >
              </FeatherInput>
            </div>
            <div class="feather-col-6">
              <FeatherInput
                label=""
                data-test="snmp-definition-last-ip-address"
                :error="errors.lastIpAddress ?? errors.invalidRangeConfig"
                v-model.trim="lastIpAddress"
                hint="Last IP Address in range (leave blank if not a range)"
              >
              </FeatherInput>
            </div>
          </div>
        </div>
        <div class="feather-col-6">
          <div class="feather-row">
            <div class="feather-col-6">
              <FeatherInput
                label=""
                data-test="snmp-definition-ipmatch-expression"
                :error="errors.ipMatch ?? errors.invalidRangeConfig"
                v-model.trim="ipMatchValue"
                hint="IPLIKE Expression (cannot be used with First/Last IP)"
              >
              </FeatherInput>
            </div>
            <div class="feather-col-6">
              <FeatherButton
                primary
                :disabled="!firstIpAddress && !lastIpAddress && !ipMatchValue"
                @click="onAddRange"
                data-test="add-definition-range-button"
              >
                Add
              </FeatherButton>
            </div>
          </div>
        </div>
      </div>
    </FeatherCard>

    <div class="feather-row">
      <div class="feather-col-6" v-if="!props.suppressMonitoringLocation">
        <FeatherSelect
          label="Monitoring Location"
          data-test="snmp-monitoring-location-select"
          hint="Select a monitoring location"
          :options="monitoringLocations"
          :modelValue="selectedMonitoringLocation"
          @update:modelValue="(val: any) => selectedMonitoringLocation = val"
        >
        </FeatherSelect>
      </div>
      <div :class="!props.suppressMonitoringLocation ? 'feather-col-6' : 'feather-col-12'">
        <div class="dropdown">
          <FeatherSelect
            label="Version"
            data-test="snmp-definition-version"
            hint="Select the SNMP version."
            :options="SnmpVersions"
            :modelValue="snmpVersion"
            @update:modelValue="onSnmpVersionUpdated"
          >
          </FeatherSelect>
        </div>
      </div>
    </div>

    <FeatherExpansionPanel
      v-if="displaySnmp2Params"
      class="snmp-config-expansion-panel"
      :modelValue="snmpV2Expanded"
      @update:modelValue="v => snmpV2Expanded = v"
    >
      <template #title>
        <h4>SNMP v1/v2c Parameters</h4>
      </template>
      <template #default>
        <SnmpConfigPairedFieldInputs
          :fieldInfo="snmpV2Fields"
          :config="formConfig"
          :validationErrors="errors"
          @update="onFieldUpdate"
          @scvSearch="onScvButtonClick"
        />
      </template>
    </FeatherExpansionPanel>

    <FeatherExpansionPanel
      v-if="displaySnmp3Params"
      class="snmp-config-expansion-panel"
      :modelValue="snmpV3Expanded"
      @update:modelValue="v => snmpV3Expanded = v"
    >
      <template #title>
        <h4>SNMP v3 Parameters</h4>
      </template>
      <template #default>
        <SnmpConfigPairedFieldInputs
          :fieldInfo="snmpV3Fields"
          :config="formConfig"
          :validationErrors="errors"
          @update="onFieldUpdate"
          @scvSearch="onScvButtonClick"
        />

        <div class="large-spacer"></div>

        <FeatherCheckbox
          label="Show Context Fields"
          data-test="snmp-definition-show-context-fields-checkbox"
          v-model="displaySnmpV3ContextFields"
        />
        <span class="show-context-fields-label">Show Context Fields</span>

        <div class="large-spacer"></div>

        <SnmpConfigPairedFieldInputs
          v-if="displaySnmpV3ContextFields"
          :fieldInfo="snmpV3ContextFields"
          :config="formConfig"
          :validationErrors="errors"
          @update="onFieldUpdate"
          @scvSearch="onScvButtonClick"
        />
      </template>
    </FeatherExpansionPanel>

    <div class="large-spacer"></div>

    <div class="feather-row">
      <div class="feather-col-12">
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

    <FeatherExpansionPanel
      class="snmp-config-expansion-panel"
      :modelValue="displayAdvancedConfig"
      @update:modelValue="v => displayAdvancedConfig = v"
    >
      <template #title>
        <h4>Advanced Parameters</h4>
      </template>
      <template #default>
        <SnmpConfigPairedFieldInputs
          :fieldInfo="advancedConfigOptions"
          :config="formConfig"
          :validationErrors="errors"
          @update="onFieldUpdate"
          @scvSearch="onScvButtonClick"
        />
      </template>
    </FeatherExpansionPanel>

    <div class="large-spacer"></div>

    <div class="feather-row">
      <div class="feather-col-12">
        <div class="action-container">
          <FeatherButton
            primary
            @click="handleSave"
            data-test="save-definition-button"
          >
            {{ isCreate ? 'Create Definition' : 'Save Changes' }}
          </FeatherButton>
          <FeatherButton
            secondary
            @click="handleCancel"
            data-test="cancel-snmp-definition-button"
          >
            Cancel
            </FeatherButton>
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
import { FeatherButton } from '@featherds/button'
import { FeatherCard } from '@featherds/card'
import { FeatherCheckbox } from '@featherds/checkbox'
import { FeatherExpansionPanel } from '@featherds/expansion'
import { FeatherInput } from '@featherds/input'
import { FeatherSelect, ISelectItemType } from '@featherds/select'
import { DEFAULT_MONITORING_LOCATION, DEFAULT_SNMP_V3_SECURITY_LEVEL } from '@/lib/constants'
import { getDefaultSnmpBaseConfiguration, useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { SnmpAgentConfig, SnmpBaseConfiguration, SnmpConfigFormErrors, SnmpFieldInfo } from '@/types/snmpConfig'
import { validateDefinition, SecurityLevelSelectionOptions, SnmpAuthProtocols, SnmpPrivacyProtocols } from '@/lib/snmpValidator'
import { withDefaultHints } from '@/lib/snmpConfigHelpers'
import SnmpConfigPairedFieldInputs from './SnmpConfigPairedFieldInputs.vue'
import ScvSearchDrawer from '../SCV/ScvSearchDrawer.vue'
import { ScvSearchItem } from '@/types/scv'

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
  return store.monitoringLocations.map(loc => {
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

const snmpV3Fields = computed<SnmpFieldInfo[]>(() => withDefaultHints([
  { key: 'securityName', label: 'Security Name', hint: 'SNMP v3 security name', dataTest: 'snmp-definition-security-name', scvEnabled: true },
  {
    key: 'securityLevel', label: 'Security Level', hint: 'SNMP v3 security level', dataTest: 'snmp-definition-security-level', isNumeric: true,
    isSelect: true, selectOptions: SecurityLevelSelectionOptions
  },
  { key: 'authPassphrase', label: 'Auth Passphrase', hint: 'Authentication passphrase', dataTest: 'snmp-definition-auth-passphrase', scvEnabled: true,
    skipDefaultHint: true
  },
  {
    key: 'authProtocol', label: 'Auth Protocol', hint: 'Authentication protocol', dataTest: 'snmp-definition-auth-protocol',
    isSelect: true, selectOptions: SnmpAuthProtocols.map(protocol => ({ _text: protocol, _value: protocol }))
  },
  { key: 'privacyPassphrase', label: 'Privacy Passphrase', hint: 'Privacy passphrase', dataTest: 'snmp-definition-privacy-passphrase', scvEnabled: true, 
    skipDefaultHint: true
  },
  {
    key: 'privacyProtocol', label: 'Privacy Protocol', hint: 'Privacy protocol', dataTest: 'snmp-definition-privacy-protocol',
    isSelect: true, selectOptions: SnmpPrivacyProtocols.map(protocol => ({ _text: protocol, _value: protocol }))
  }
], store.currentDefaults))

const snmpV3ContextFields = computed<SnmpFieldInfo[]>(() => withDefaultHints([
  { key: 'engineId', label: 'Engine ID', hint: 'SNMP engine ID', dataTest: 'snmp-definition-engine-id' },
  { key: 'contextEngineId', label: 'Context Engine ID', hint: 'Context engine ID', dataTest: 'snmp-definition-context-engine-id' },
  { key: 'contextName', label: 'Context Name', hint: 'SNMP context name', dataTest: 'snmp-definition-context-name' },
  { key: 'enterpriseId', label: 'Enterprise ID', hint: 'Enterprise ID', dataTest: 'snmp-definition-enterprise-id' }
], store.currentDefaults))

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
    handleValidate()
  }
}

const onAddRange = () => {
  handleValidate()

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
    authPassphrase: formConfig.authPassphrase ?? undefined,
    authProtocol: formConfig.authProtocol ?? undefined,
    engineId: formConfig.engineId ?? undefined,
    contextEngineId: formConfig.contextEngineId ?? undefined,
    contextName: formConfig.contextName ?? undefined,
    privacyPassphrase: formConfig.privacyPassphrase ?? undefined,
    privacyProtocol: formConfig.privacyProtocol ?? undefined,
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

const handleValidate = (isSaving?: boolean) => {
  const version = String(snmpVersion.value?._value || '')
  // if we are not displaying IPs, or if we are saving, pass a fake valid IP to avoid validation errors
  const fakeValidIp = '10.0.0.0'

  const currentErrors = validateDefinition(
    formConfig,
    version,
    props.displayIps && !isSaving ? firstIpAddress.value : fakeValidIp,
    lastIpAddress.value,
    ipMatchValue.value
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
@use "@featherds/styles/themes/variables";
@use "@featherds/styles/mixins/typography";
@use "@featherds/table/scss/table";

.snmp-config-definition-details {
  .label {
    font-weight: 600;
  }

  .show-context-fields-label {
    margin-left: 0.1rem;
    font-weight: 500;
  }

  .dropdown {
    width: 50%;
  }

  .feather-row {
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
  }
}
</style>
