<template>
  <div class="main-content">
    <div class="header">
      <div>
        <FeatherBackButton
          data-test="back-button"
          @click="onDetailsCancel"
        >
          Go Back
        </FeatherBackButton>
      </div>
      <div>
        <h3>
          {{ isCreate ? 'Create New SNMP Definition' : 'Edit SNMP Definition Details' }}
        </h3>
      </div>
    </div>
    <div class="spacer"></div>
    <div class="basic-info">
      <div class="section-content">
        <h4>IP Ranges:</h4>
        <div class="feather-row" v-if="(currentDefinition?.ranges?.length ?? 0) > 0">
          <div class="feather-col-12">
            <div class="ip-address-badge-wrapper">
              <FeatherTextBadge
                v-for="range of currentDefinition?.ranges" :key="range.begin"
                :type="BadgeTypes.info"
                class="ip-address-badge"
                @click="() => onBadgeClicked(range.begin, range.end)"
              >
                {{ `${range.begin} - ${range.end}` }}
              </FeatherTextBadge>
            </div>
          </div>
        </div>

        <div class="feather-row" v-if="(currentDefinition?.specifics?.length ?? 0) > 0">
          <div class="feather-col-12">
            <div class="ip-address-badge-wrapper">
              <FeatherTextBadge
                v-for="ipAddr of currentDefinition?.specifics" :key="ipAddr"
                :type="BadgeTypes.info"
                class="ip-address-badge"
                @click="() => onBadgeClicked(ipAddr)"
              >
                {{ ipAddr }}
              </FeatherTextBadge>
            </div>
          </div>
        </div>

        <div class="feather-row" v-if="(currentDefinition?.ipMatches?.length ?? 0) > 0">
          <div class="feather-col-12">
            <div class="ip-address-badge-wrapper">
              <FeatherTextBadge
                v-for="ipAddr of currentDefinition?.ipMatches" :key="ipAddr"
                :type="BadgeTypes.info"
                class="ip-address-badge"
                @click="() => onBadgeClicked(ipAddr)"
              >
                {{ ipAddr }}
              </FeatherTextBadge>
            </div>
          </div>
        </div>

        <div class="large-spacer"></div>

        <SnmpConfigDetailsPanel
          v-if="snmpAgentConfig"
          ref="detailsPanel"
          :displayIps="true"
          :isCreate="false"
          :firstIp="firstIpAddress"
          :lastIp="lastIpAddress"
          :config="snmpAgentConfig"
          :errors="errors"
          @cancel="onDetailsCancel"
          @validation-error="onDetailsValidationError"
          @save="onDetailsSave"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { FeatherBackButton } from '@featherds/back-button'
import { FeatherTextBadge, BadgeTypes } from '@featherds/badge'
import { DEFAULT_MONITORING_LOCATION } from '@/lib/constants'
import { convertSnmpVersionToString } from '@/services/snmpConfigService'
import { getDefaultSnmpDefinition } from '@/stores/snmpConfigStore'
import { SnmpAgentConfig, SnmpDefinition, SnmpConfigFormErrors } from '@/types/snmpConfig'
import SnmpConfigDetailsPanel from './SnmpConfigDetailsPanel.vue'

const props = defineProps<{
  isCreate: boolean,
  definition: SnmpDefinition | null
}>()

const emit = defineEmits<{
  (e: 'cancel'): void
  (e: 'save', definition: SnmpDefinition, firstIp?: string, lastIp?: string): void
  (e: 'validation-error', errors: SnmpConfigFormErrors): void
}>()

const isValid = ref(false)
const errors = ref<SnmpConfigFormErrors>({})
const currentDefinition = ref<SnmpDefinition>()
const firstIpAddress = ref('')
const lastIpAddress = ref('')
const detailsPanel = ref()

const snmpAgentConfig = computed(() => {
  const config = {
    version: convertSnmpVersionToString(currentDefinition.value?.version ?? 'v2c'),
    location: currentDefinition.value?.location ?? DEFAULT_MONITORING_LOCATION,
    readCommunity: currentDefinition.value?.readCommunity ?? '',
    writeCommunity: currentDefinition.value?.writeCommunity ?? '',
    timeout: currentDefinition.value?.timeout ?? undefined,
    retry: currentDefinition.value?.retry ?? undefined,
    port: currentDefinition.value?.port ?? undefined,
    proxyHost: currentDefinition.value?.proxyHost ?? '',
    maxRequestSize: currentDefinition.value?.maxRequestSize ?? undefined,
    maxVarsPerPdu: currentDefinition.value?.maxVarsPerPdu ?? undefined,
    maxRepetitions: currentDefinition.value?.maxRepetitions ?? undefined,
    ttl: currentDefinition.value?.ttl ?? undefined,
    securityName: currentDefinition.value?.securityName ?? '',
    securityLevel: currentDefinition.value?.securityLevel ?? undefined,
    authPassphrase: currentDefinition.value?.authPassphrase ?? '',
    authProtocol: currentDefinition.value?.authProtocol ?? '',
    engineId: currentDefinition.value?.engineId ?? '',
    contextEngineId: currentDefinition.value?.contextEngineId ?? '',
    contextName: currentDefinition.value?.contextName ?? '',
    privacyPassphrase: currentDefinition.value?.privacyPassphrase ?? '',
    privacyProtocol: currentDefinition.value?.privacyProtocol ?? '',
    enterpriseId: currentDefinition.value?.enterpriseId ?? ''
  } as SnmpAgentConfig

  return config
})

const resetValues = () => {
  firstIpAddress.value = ''
  lastIpAddress.value = ''
}

const loadInitialValues = () => {
  if (!props.definition) {
    currentDefinition.value = getDefaultSnmpDefinition()
  } else {
    currentDefinition.value = props.definition
  }

  if (currentDefinition.value.ranges && currentDefinition.value.ranges.length > 0) {
    firstIpAddress.value = currentDefinition.value.ranges[0].begin
    lastIpAddress.value = currentDefinition.value.ranges[0].end
  } else if (currentDefinition.value.specifics && currentDefinition.value.specifics.length > 0) {
    firstIpAddress.value = currentDefinition.value.specifics[0]
    lastIpAddress.value = ''
  } else {
    firstIpAddress.value = ''
    lastIpAddress.value = ''
  }
}

const onDetailsValidationError = (formErrors: SnmpConfigFormErrors) => {
  isValid.value = Object.keys(formErrors).length === 0
  errors.value = { ...formErrors}

  emit('validation-error', errors.value)
}

const onBadgeClicked = (begin: string, end?: string) => {
  firstIpAddress.value = begin
  lastIpAddress.value = end ?? ''

  // if user changes IP address inside the details panel, and user clicks a badge
  // that has same IP address as the original prop, we have to force update the details panel
  if (detailsPanel.value) {
    detailsPanel.value.updateIpAddresses(begin, end)
  }
}

const onDetailsSave = async (config: SnmpAgentConfig, firstIp?: string, lastIp?: string) => {
  firstIpAddress.value = firstIp ?? ''
  lastIpAddress.value = lastIp ?? ''

  const specifics = lastIpAddress.value
    ? []
    : firstIpAddress.value ? [firstIpAddress.value] : []

  const ranges = lastIpAddress.value
    ? [{ begin: firstIpAddress.value, end: lastIpAddress.value }]
    : []

  const definitionToSave = {
    ...currentDefinition.value,
    ranges,
    specifics,
    ipMatches: currentDefinition.value?.ipMatches ?? [],
    location: config.location,
    port: config.port,
    retry: config.retry,
    timeout: config.timeout,
    readCommunity: config.readCommunity,
    writeCommunity: config.writeCommunity,
    proxyHost: config.proxyHost,
    version: config.version,
    maxVarsPerPdu: config.maxVarsPerPdu,
    maxRepetitions: config.maxRepetitions,
    maxRequestSize: config.maxRequestSize,
    securityName: config.securityName,
    securityLevel: config.securityLevel,
    authPassphrase: config.authPassphrase,
    authProtocol: config.authProtocol,
    privacyPassphrase: config.privacyPassphrase,
    privacyProtocol: config.privacyProtocol,
    engineId: config.engineId,
    contextEngineId: config.contextEngineId,
    contextName: config.contextName,
    enterpriseId: config.enterpriseId,
    ttl: config.ttl
  } as SnmpDefinition

  emit('save', definitionToSave, firstIpAddress.value, lastIpAddress.value)
}

const onDetailsCancel = () => {
  resetValues()
  emit('cancel')
}

watch([() => props.definition, () => props.isCreate], () => {
  loadInitialValues()
})

onMounted(() => {
  loadInitialValues()
})
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables' as variables;
@use '@featherds/styles/mixins/typography';
@use "@featherds/table/scss/table";

.main-content {
  padding: 0.2em;
  margin: 0.2em;

  border-radius: 8px;
  background-color: var(variables.$surface);

  .header {
    display: flex;
    align-items: center;
    gap: 20px;
  }

  .basic-info {
    border-width: 1px;
    border-style: solid;
    border-color: var(variables.$border-on-surface);
    padding: 1em;
    border-radius: 8px;

    .label {
      font-weight: 600;
    }

    .section-content {
      width: 80%;
    }

    .ip-address-badge {
      cursor: pointer;
    }

    .dropdown {
      width: 50%;
    }
  }

  .large-spacer {
    min-height: 1em;
  }

  .spacer {
    min-height: 0.5em;
  }

  .action-container {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
}
</style>
