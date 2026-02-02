<template>
  <div class="snmp-config-definition-details">
    <div v-if="props.displayIps" class="feather-row">
      <div class="feather-col-6">
        <label class="label">First IP Address:</label>
      </div>
      <div class="feather-col-6">
        <label class="label">Last IP Address (for IP Range):</label>
      </div>
    </div>
    <div 
      class="feather-row"
      v-if="props.displayIps"
    >
      <div class="feather-col-6">
        <FeatherInput
          label=""
          data-test="snmp-definition-first-ip-address"
          :error="errors.firstIpAddress"
          v-model.trim="firstIpAddress"
          hint="First IP Address in range"
        >
        </FeatherInput>
      </div>
      <div class="feather-col-6">
        <FeatherInput
          label=""
          data-test="snmp-definition-last-ip-address"
          :error="errors.lastIpAddress"
          v-model.trim="lastIpAddress"
          hint="Last IP Address in range"
        >
        </FeatherInput>
      </div>
    </div>

    <div class="feather-row">
      <div class="feather-col-6" v-if="!props.suppressMonitoringLocation">
        <SnmpConfigMonitoringLocationsDropdown
          :monitoringLocation="selectedMonitoringLocationValue"
          @update:modelValue="selectedMonitoringLocation = $event"
        />
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
            <FeatherIcon :icon="MoreVert" />
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
import { FeatherCheckbox } from '@featherds/checkbox'
import { FeatherExpansionPanel } from '@featherds/expansion'
import MoreVert from '@featherds/icon/navigation/MoreVert'
import { FeatherInput } from '@featherds/input'
import { FeatherSelect, ISelectItemType } from '@featherds/select'
import { DEFAULT_SNMP_V3_SECURITY_LEVEL } from '@/lib/constants'
import { getDefaultSnmpBaseConfiguration, useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { SnmpAgentConfig, SnmpBaseConfiguration, SnmpConfigFormErrors, SnmpFieldInfo } from '@/types/snmpConfig'
import { validateDefinition } from '@/lib/snmpValidator'
import SnmpConfigMonitoringLocationsDropdown from './SnmpConfigMonitoringLocationsDropdown.vue'
import SnmpConfigPairedFieldInputs from './SnmpConfigPairedFieldInputs.vue'
import ScvSearchDrawer from '../SCV/ScvSearchDrawer.vue'
import { ScvSearchItem } from '@/types/scv'

const props = defineProps<{
  isCreate: boolean,
  displayIps: boolean,
  suppressMonitoringLocation?: boolean,
  firstIp: string,
  lastIp?: string,
  config?: SnmpAgentConfig
}>()
 
const emit = defineEmits<{
  (e: 'cancel'): void
  (e: 'save', config: SnmpAgentConfig, firstIp?: string, lastIp?: string): void
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
const selectedMonitoringLocation = ref<ISelectItemType>()
const formConfig = reactive<SnmpBaseConfiguration>(getDefaultSnmpBaseConfiguration())
const scvSearchDrawerOpen = ref(false)
const scvSelectedProperty = ref('')

const snmpV2Expanded = ref(false)
const snmpV3Expanded = ref(false)
const displayAdvancedConfig = ref(false)
const displaySnmpV3ContextFields = ref(false)

const displaySnmp2Params = computed(() => {
  const version = String(snmpVersion.value?._value || '')
  return version === 'v1' || version === 'v2c'
})

const displaySnmp3Params = computed(() => {
  const version = String(snmpVersion.value?._value || '')
  return version === 'v3'
})

// Field metadata for v-for rendering
const generalParamFields: SnmpFieldInfo[] = [
  { key: 'timeout', label: 'Timeout', hint: 'Timeout in milliseconds', dataTest: 'snmp-definition-timeout', isNumeric: true },
  { key: 'retry', label: 'Retries', hint: 'Number of retries', dataTest: 'snmp-definition-retry', isNumeric: true }
]

const advancedConfigOptions: SnmpFieldInfo[] = [
  { key: 'port', label: 'Port', hint: 'SNMP port (default: 161)', dataTest: 'snmp-definition-port', isNumeric: true },
  { key: 'proxyHost', label: 'Proxy Host', hint: 'Proxy host for SNMP communication', dataTest: 'snmp-definition-proxy-host' },
  { key: 'maxRequestSize', label: 'Max Request Size', hint: 'Maximum bytes per PDU request', dataTest: 'snmp-definition-max-request-size', isNumeric: true },
  { key: 'maxVarsPerPdu', label: 'Max Vars Per PDU', hint: 'Variables per SNMP request', dataTest: 'snmp-definition-max-vars-per-pdu', isNumeric: true },
  { key: 'maxRepetitions', label: 'Max Repetitions', hint: 'Repetitions per get-bulk request', dataTest: 'snmp-definition-max-repetitions', isNumeric: true },
  { key: 'ttl', label: 'TTL', hint: 'Time to live', dataTest: 'snmp-definition-ttl', isNumeric: true }
]

const snmpV2Fields: SnmpFieldInfo[] = [
  { key: 'readCommunity', label: 'Read Community String', hint: 'Read community string', dataTest: 'snmp-lookup-read-community', scvEnabled: true },
  { key: 'writeCommunity', label: 'Write Community String', hint: 'Write community string', dataTest: 'snmp-lookup-write-community', scvEnabled: true }
]

const snmpV3Fields: SnmpFieldInfo[] = [
  { key: 'securityName', label: 'Security Name', hint: 'SNMP v3 security name', dataTest: 'snmp-definition-security-name', scvEnabled: true },
  { key: 'securityLevel', label: 'Security Level', hint: 'SNMP v3 security level', dataTest: 'snmp-definition-security-level', isNumeric: true },
  { key: 'authPassphrase', label: 'Auth Passphrase', hint: 'Authentication passphrase', dataTest: 'snmp-definition-auth-passphrase', scvEnabled: true },
  { key: 'authProtocol', label: 'Auth Protocol', hint: 'Authentication protocol', dataTest: 'snmp-definition-auth-protocol' },
  { key: 'privacyPassphrase', label: 'Privacy Passphrase', hint: 'Privacy passphrase', dataTest: 'snmp-definition-privacy-passphrase', scvEnabled: true },
  { key: 'privacyProtocol', label: 'Privacy Protocol', hint: 'Privacy protocol', dataTest: 'snmp-definition-privacy-protocol' }
]

const snmpV3ContextFields: SnmpFieldInfo[] = [
  { key: 'engineId', label: 'Engine ID', hint: 'SNMP engine ID', dataTest: 'snmp-definition-engine-id' },
  { key: 'contextEngineId', label: 'Context Engine ID', hint: 'Context engine ID', dataTest: 'snmp-definition-context-engine-id' },
  { key: 'contextName', label: 'Context Name', hint: 'SNMP context name', dataTest: 'snmp-definition-context-name' },
  { key: 'enterpriseId', label: 'Enterprise ID', hint: 'Enterprise ID', dataTest: 'snmp-definition-enterprise-id' }
]

const selectedMonitoringLocationValue = computed<string>(() => {
  return String(selectedMonitoringLocation.value?._value ?? '')
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
  const matchedLoc = store.monitoringLocations.find(x => x.name === currentConfig.location)
  selectedMonitoringLocation.value = matchedLoc ? { _text: matchedLoc.name, _value: matchedLoc.name } : undefined
  
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
  handleValidate()

  try {
    if (!isValid.value) {
      emit('validation-error', errors.value)
      return
    }

    let configToSave: SnmpAgentConfig = {
      ...formConfig,
      location: String(selectedMonitoringLocation.value?._value ?? ''),
      id: props.config?.id
    }

    emit('save', configToSave, firstIpAddress.value, lastIpAddress.value)
  } catch (error) {
    console.error(error)
  }
}

const updateIpAddresses = (begin: string, end?: string) => {
  firstIpAddress.value = begin || ''
  lastIpAddress.value = end || ''
}

const handleCancel = () => {
  emit('cancel')
}

const handleValidate = () => {
  const version = String(snmpVersion.value?._value || '')
  // if we are not displaying IPs, pass a fake valid IP to avoid validation errors
  const fakeValidIp = '10.0.0.0'

  const currentErrors = validateDefinition(
    formConfig,
    version,
    props.displayIps ? firstIpAddress.value : fakeValidIp,
    lastIpAddress.value
  )

  errors.value = currentErrors as SnmpConfigFormErrors
  isValid.value = Object.getOwnPropertyNames(currentErrors).length === 0

  if (!isValid.value) {
    emit('validation-error', errors.value)
  }
}

defineExpose({
  updateIpAddresses
})

watch([() => props.config, () => props.isCreate], () => {
  isLoading.value = true
  loadInitialValues()
  isLoading.value = false
})

watch([() => props.firstIp, () => props.lastIp], () => {
  if (props.displayIps) {
    firstIpAddress.value = props.firstIp || ''
    lastIpAddress.value = props.lastIp || ''
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
}
</style>
