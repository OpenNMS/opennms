<template>
  <div class="snmp-config-definition-details">
    <div class="feather-row">
      <div class="feather-col-12">
        <h4>General Parameters</h4>
      </div>
    </div>
    <div class="feather-row">
      <div class="feather-col-4">
        <label class="label">Version:</label>
      </div>
      <div class="feather-col-8">
        <div class="dropdown">
          <FeatherSelect
            label="Version"
            data-test="snmp-definition-version"
            hint="Select the SNMP version."
            :options="SnmpVersions"
            :modelValue="snmpVersion"
            @update:modelValue="(val: ISelectItemType | undefined) => snmpVersion = val"
          >
            <FeatherIcon :icon="MoreVert" />
          </FeatherSelect>
        </div>
      </div>
    </div>
    <div class="feather-row">
      <div class="feather-col-4">
        <label class="label">First IP Address:</label>
      </div>
      <div class="feather-col-8">
        <FeatherInput
          label=""
          data-test="snmp-definition-first-ip-address"
          :error="errors.firstIpAddress"
          v-model.trim="firstIpAddress"
          hint="First IP Address in range"
        >
        </FeatherInput>
      </div>
    </div>
    <div class="feather-row">
      <div class="feather-col-4">
        <label class="label">Second IP Address:</label>
      </div>
      <div class="feather-col-8">
        <FeatherInput
          label=""
          data-test="snmp-definition-second-ip-address"
          :error="errors.secondIpAddress"
          v-model.trim="secondIpAddress"
          hint="Second IP Address in range"
        >
        </FeatherInput>
      </div>
    </div>

    <div class="feather-row">
      <div class="feather-col-4">
        <label class="label">Location:</label>
      </div>
      <div class="feather-col-8">
        <SnmpConfigMonitoringLocationsDropdown
          :monitoringLocation="selectedMonitoringLocationValue"
          @update:modelValue="selectedMonitoringLocation = $event"
        />
      </div>
    </div>

    <div class="feather-row" v-for="field in generalParamFields" :key="field.key">
      <div class="feather-col-4">
        <label class="label">{{ field.label }}:</label>
      </div>
      <div class="feather-col-8">
        <FeatherInput
          label=""
          :data-test="field.dataTest"
          v-model.trim="(formConfig as any)[field.key]"
          :hint="field.hint"
        >
        </FeatherInput>
      </div>
    </div>

    <div class="spacer"></div>

    <FeatherExpansionPanel
      class="snmp-config-expansion-panel"
      :modelValue="displaySnmp2Params"
      @update:modelValue="v => displaySnmp2Params = v"
    >
      <template #title>
        <h4>SNMP v1/v2c Parameters</h4>
      </template>
      <template #default>
        <div class="feather-row" v-for="field in snmpV2Fields" :key="field.key">
          <div class="feather-col-4">
            <label class="label">{{ field.label }}:</label>
          </div>
          <div class="feather-col-8">
            <FeatherInput
              label=""
              :data-test="field.dataTest"
              v-model.trim="(formConfig as any)[field.key]"
              :hint="field.hint"
            >
            </FeatherInput>
          </div>
        </div>
      </template>
    </FeatherExpansionPanel>

    <div class="large-spacer"></div>

    <FeatherExpansionPanel
      class="snmp-config-expansion-panel"
      :modelValue="displaySnmp3Params"
      @update:modelValue="v => displaySnmp3Params = v"
    >
      <template #title>
        <h4>SNMP v3 Parameters</h4>
      </template>
      <template #default>
        <div class="feather-row" v-for="field in snmpV3Fields" :key="field.key">
          <div class="feather-col-4">
            <label class="label">{{ field.label }}:</label>
          </div>
          <div class="feather-col-8">
            <FeatherInput
              label=""
              :data-test="field.dataTest"
              v-model.trim="(formConfig as any)[field.key]"
              :hint="field.hint"
            >
            </FeatherInput>
          </div>
        </div>
      </template>
    </FeatherExpansionPanel>

    <div class="large-spacer"></div>

    <div class="feather-row">
      <div class="feather-col-12">
        <div class="action-container">
          <FeatherButton
            primary
            @click="handleSaveDefinition"
            data-test="save-definition-button"
            :disabled="!isValid"
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
  </div>
</template>

<script setup lang="ts">
import { FeatherButton } from '@featherds/button'
import { FeatherExpansionPanel } from '@featherds/expansion'
import { FeatherInput } from '@featherds/input'
import { FeatherSelect, ISelectItemType } from '@featherds/select'
import { validateDefinition } from './snmpValidator'
import SnmpConfigMonitoringLocationsDropdown from './SnmpConfigMonitoringLocationsDropdown.vue'
import { getDefaultSnmpBaseConfiguration, useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { SnmpAgentConfig, SnmpDefinitionFormErrors } from '@/types/snmpConfig'
import { isNonEmptyString } from '@/lib/utils'
import MoreVert from '@featherds/icon/navigation/MoreVert'

const props = defineProps<{
  isCreate: boolean,
  ipAddress: string,
  config: SnmpAgentConfig
}>()
 
const emit = defineEmits<{
  (e: 'cancel'): void
  (e: 'save', config: SnmpAgentConfig, firstIp?: string, lastIp?: string): void
  (e: 'validation-error', formErrors: SnmpDefinitionFormErrors): void
}>()
 
const SnmpVersions: ISelectItemType[] = [
  { _text: 'v1', _value: 'v1' },
  { _text: 'v2c', _value: 'v2c' },
  { _text: 'v3', _value: 'v3' }
]

const store = useSnmpConfigStore()
const currentConfig = ref<SnmpAgentConfig>(getDefaultSnmpBaseConfiguration())
const snmpVersion = ref()
const isValid = ref(false)
const errors = ref<SnmpDefinitionFormErrors>({})

// local data for form inputs
const firstIpAddress = ref('')
const secondIpAddress = ref('')
const selectedMonitoringLocation = ref<ISelectItemType>()
const formConfig = reactive(getDefaultSnmpBaseConfiguration())

const displaySnmp2Params = computed(() => {
  const version = String(snmpVersion.value?._value || '')
  return version === 'v1' || version === 'v2c'
})

const displaySnmp3Params = computed(() => {
  const version = String(snmpVersion.value?._value || '')
  return version === 'v3'
})

// Field metadata for v-for rendering
const generalParamFields = [
  { key: 'timeout' as keyof any, label: 'Timeout', hint: 'Timeout in milliseconds', dataTest: 'snmp-definition-timeout' },
  { key: 'retry' as keyof any, label: 'Retry', hint: 'Number of retries', dataTest: 'snmp-definition-retry' },
  { key: 'port' as keyof any, label: 'Port', hint: 'SNMP port (default: 161)', dataTest: 'snmp-definition-port' },
  { key: 'proxyHost' as keyof any, label: 'Proxy Host', hint: 'Proxy host for SNMP communication', dataTest: 'snmp-definition-proxy-host' },
  { key: 'maxRequestSize' as keyof any, label: 'Max Request Size', hint: 'Maximum bytes per PDU request', dataTest: 'snmp-definition-max-request-size' },
  { key: 'maxVarsPerPdu' as keyof any, label: 'Max Vars Per PDU', hint: 'Variables per SNMP request', dataTest: 'snmp-definition-max-vars-per-pdu' },
  { key: 'maxRepetitions' as keyof any, label: 'Max Repetitions', hint: 'Repetitions per get-bulk request', dataTest: 'snmp-definition-max-repetitions' },
  { key: 'ttl' as keyof any, label: 'TTL', hint: 'Time to live', dataTest: 'snmp-definition-ttl' }
]

const snmpV2Fields = [
  { key: 'readCommunity' as keyof any, label: 'Read Community', hint: 'Read community string', dataTest: 'snmp-lookup-read-community' },
  { key: 'writeCommunity' as keyof any, label: 'Write Community', hint: 'Write community string', dataTest: 'snmp-lookup-write-community' }
]

const snmpV3Fields = [
  { key: 'securityName' as keyof any, label: 'Security Name', hint: 'SNMP v3 security name', dataTest: 'snmp-definition-security-name' },
  { key: 'securityLevel' as keyof any, label: 'Security Level', hint: 'SNMP v3 security level', dataTest: 'snmp-definition-security-level' },
  { key: 'authPassphrase' as keyof any, label: 'Auth Passphrase', hint: 'Authentication passphrase', dataTest: 'snmp-definition-auth-passphrase' },
  { key: 'authProtocol' as keyof any, label: 'Auth Protocol', hint: 'Authentication protocol', dataTest: 'snmp-definition-auth-protocol' },
  { key: 'engineId' as keyof any, label: 'Engine ID', hint: 'SNMP engine ID', dataTest: 'snmp-definition-engine-id' },
  { key: 'contextEngineId' as keyof any, label: 'Context Engine ID', hint: 'Context engine ID', dataTest: 'snmp-definition-context-engine-id' },
  { key: 'contextName' as keyof any, label: 'Context Name', hint: 'SNMP context name', dataTest: 'snmp-definition-context-name' },
  { key: 'privacyPassphrase' as keyof any, label: 'Privacy Passphrase', hint: 'Privacy passphrase', dataTest: 'snmp-definition-privacy-passphrase' },
  { key: 'privacyProtocol' as keyof any, label: 'Privacy Protocol', hint: 'Privacy protocol', dataTest: 'snmp-definition-privacy-protocol' },
  { key: 'enterpriseId' as keyof any, label: 'Enterprise ID', hint: 'Enterprise ID', dataTest: 'snmp-definition-enterprise-id' }
]

const selectedMonitoringLocationValue = computed<string>(() => {
  return String(selectedMonitoringLocation.value?._value ?? '')
})

const resetValues = () => {
  snmpVersion.value = SnmpVersions[1]
  firstIpAddress.value = ''
  secondIpAddress.value = ''

  // Reset formConfig to defaults
  Object.assign(formConfig, getDefaultSnmpBaseConfiguration())
}

const loadInitialValues = () => {
  currentConfig.value = props.config ?? getDefaultSnmpBaseConfiguration()

  if (currentConfig.value.version === 'v1') {
    snmpVersion.value = SnmpVersions[0]
  } else if (currentConfig.value.version === 'v2c') {
    snmpVersion.value = SnmpVersions[1]
  } else if (currentConfig.value.version === 'v3') {
    snmpVersion.value = SnmpVersions[2]
  }
    
  // For now, just set firstIpAddress
  // We will handle ranges, etc. later
  firstIpAddress.value = isNonEmptyString(props.ipAddress) ? props.ipAddress : ''
  secondIpAddress.value = ''
  const matchedLoc = store.monitoringLocations.find(x => x.name === currentConfig.value.location)
  selectedMonitoringLocation.value = matchedLoc ? { _text: matchedLoc.name, _value: matchedLoc.name } : undefined
  
  // Load all config fields into formConfig
  Object.assign(formConfig, {
    version: props.config.version ?? '',
    readCommunity: props.config.readCommunity ?? '',
    writeCommunity: props.config.writeCommunity ?? '',
    timeout: props.config.timeout ?? undefined,
    retry: props.config.retry ?? undefined,
    port: props.config.port ?? undefined,
    proxyHost: props.config.proxyHost ?? '',
    maxRequestSize: props.config.maxRequestSize ?? undefined,
    maxVarsPerPdu: props.config.maxVarsPerPdu ?? undefined,
    maxRepetitions: props.config.maxRepetitions ?? undefined,
    ttl: props.config.ttl ?? undefined,
    securityName: props.config.securityName ?? '',
    securityLevel: props.config.securityLevel ?? undefined,
    authPassphrase: props.config.authPassphrase ?? '',
    authProtocol: props.config.authProtocol ?? '',
    engineId: props.config.engineId ?? '',
    contextEngineId: props.config.contextEngineId ?? '',
    contextName: props.config.contextName ?? '',
    privacyPassphrase: props.config.privacyPassphrase ?? '',
    privacyProtocol: props.config.privacyProtocol ?? '',
    enterpriseId: props.config.enterpriseId ?? ''
  })
}

const handleSaveDefinition = async () => {
  handleValidate()

  try {
    if (!isValid.value) {
      emit('validation-error', errors.value)
      // snackbar.showSnackBar({ msg: 'Invalid values', error: true })
      return
    }

    // TODO: save values to store and then to Rest API
    // snackbar.showSnackBar({ msg: props.isCreate ? 'Definition created successfully' : 'Definition updated successfully', error: false })

    emit('save', currentConfig.value)
  } catch (error) {
    console.error(error)
  }
}

const handleCancel = () => {
  resetValues()
  emit('cancel')

  // router.push({
  //   name: 'SNMP Config'
  // })
}

const handleValidate = () => {
  const version = String(snmpVersion.value?._value || '')

  const currentErrors = validateDefinition(
    version,
    firstIpAddress.value,
    secondIpAddress.value
  )
  isValid.value = Object.keys(currentErrors).length === 0
  errors.value = currentErrors as SnmpDefinitionFormErrors
}

watch([() => props.config, () => props.isCreate], () => {
  loadInitialValues()
})

watchEffect(() => {
  handleValidate()
})

onMounted(() => {
  loadInitialValues()
})
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';

.snmp-config-definition-details {
  .label {
    font-weight: 600;
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
