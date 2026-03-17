<template>
  <div class="snmp-config-lookup-tab">
    <div class="main-section">
      <SnmpConfigLookupPanel
        v-if="store.snmpLookupEditMode === SnmpLookupEditMode.Lookup"
        @lookup-complete="onLookupComplete"
      />
      <div class="snmp-config-details" v-if="store.snmpLookupEditMode === SnmpLookupEditMode.Edit">
        <h3>SNMP Configuration Found</h3>
        <div class="large-spacer"></div>
        <div class="section-content">
          <SnmpConfigDefinitionBasicInformation
            :isCreate="false"
            :definition="currentDefinition"
            @cancel="handleBackButtonClick"
            @save="onSaveDefinition"
            @validationError="onDetailsValidationError"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import useSnackbar from '@/composables/useSnackbar'
import { getDefaultSnmpDefinition, SnmpLookupEditMode, useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { SnmpAgentConfig, SnmpDefinition } from '@/types/snmpConfig'
import SnmpConfigLookupPanel from './SnmpConfigLookupPanel.vue'
import SnmpConfigDefinitionBasicInformation from './SnmpConfigDefinitionBasicInformation.vue'

const snackbar = useSnackbar()
const store = useSnmpConfigStore()

const lookupConfig = ref<SnmpAgentConfig>()
const currentDefinition = ref<SnmpDefinition>(getDefaultSnmpDefinition())

// lookup config response individual parameters to edit
const ipAddress = ref('')

const resetValues = () => {
  lookupConfig.value = undefined
  currentDefinition.value = getDefaultSnmpDefinition()
  ipAddress.value = ''
}

const handleBackButtonClick = () => {
  resetValues()
  store.setSnmpLookupEditMode(SnmpLookupEditMode.Lookup)
}

const onLookupComplete = (config: SnmpAgentConfig, ip: string) => {
  lookupConfig.value = config
  ipAddress.value = ip

  currentDefinition.value = {
    version: config.version,
    port: config.port,
    timeout: config.timeout,
    retry: config.retry,
    maxRequestSize: config.maxRequestSize,
    maxVarsPerPdu: config.maxVarsPerPdu,
    maxRepetitions: config.maxRepetitions,
    ttl: config.ttl,
    readCommunity: config.readCommunity,
    writeCommunity: config.writeCommunity,
    proxyHost: config.proxyHost,
    securityName: config.securityName,
    securityLevel: config.securityLevel,
    authPassphrase: config.authPassphrase,
    authProtocol: config.authProtocol,
    engineId: config.engineId,
    contextEngineId: config.contextEngineId,
    contextName: config.contextName,
    privacyPassphrase: config.privacyPassphrase,
    privacyProtocol: config.privacyProtocol,
    enterpriseId: config.enterpriseId,
    location: config.location,
    profileLabel: config.profileLabel,
    range: [],
    specific: [ip],
    ipMatch: []
  } as SnmpDefinition
}

const onDetailsValidationError = () => {
  snackbar.showSnackBar({ msg: 'Save failed. Please fix invalid values.', error: true })
}

const onSaveDefinition = async (definition: SnmpDefinition) => {
  const resp = await store.saveDefinition(definition)

  if (resp.success) {
    snackbar.showSnackBar({ msg: 'Configuration saved successfully' })
  } else {
    snackbar.showSnackBar({ msg: `Save failed: ${resp.message}`, error: true })
  }

  // get latest config values after save
  await store.populateSnmpConfig()
}

onMounted(() => {
  resetValues()
})
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/vars.scss';

.snmp-config-lookup-tab {
  background: var(variables.$surface);
  width: 100%;
  padding: 0;
  border-radius: 5px;
  margin-top: 0;
  border: 1px solid var(variables.$border-on-surface);

  .main-section {
    display: flex;
    flex-direction: column;
    gap: 20px;

    .snmp-config-details {
      border-radius: 5px;
      padding: 20px;
      width: 80%;

      .section-content {
        display: flex;
        flex-direction: column;
        gap: 20px;
      }
    }
  
    .large-spacer {
      min-height: 1em;
    }
  }
}
</style>
