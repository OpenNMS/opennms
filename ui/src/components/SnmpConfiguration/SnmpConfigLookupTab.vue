<template>
  <div class="snmp-config-lookup-tab">
    <div class="main-section">
      <SnmpConfigLookupPanel
        v-if="store.snmpLookupEditMode === SnmpLookupEditMode.Lookup"
        @lookup-complete="onLookupComplete"
      />
      <div class="snmp-config-details" v-if="store.snmpLookupEditMode === SnmpLookupEditMode.Edit">
        <div>
          <FeatherBackButton
            data-test="back-button"
            @click="handleBackButtonClick"
          >
            Return to Lookup
          </FeatherBackButton>
        </div>
        <h3>SNMP Configuration Found</h3>
        <div class="large-spacer"></div>
        <div class="section-content">
          <SnmpConfigDefinitionDetails
            :isCreate="false"
            :firstIp="ipAddress"
            :config="lookupConfig"
            @cancel="onDetailsCancel"
            @validation-error="onDetailsValidationError"
            @save="onDetailsSave"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { FeatherBackButton } from '@featherds/back-button'
import useSnackbar from '@/composables/useSnackbar'
import { SnmpLookupEditMode, useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { SnmpAgentConfig } from '@/types/snmpConfig'
import SnmpConfigLookupPanel from './SnmpConfigLookupPanel.vue'
import SnmpConfigDefinitionDetails from './SnmpConfigDefinitionDetails.vue'

const snackbar = useSnackbar()
const store = useSnmpConfigStore()

const lookupConfig = ref<SnmpAgentConfig>()

// lookup config response individual parameters to edit
const ipAddress = ref('')

const resetValues = () => {
  lookupConfig.value = undefined
  ipAddress.value = ''
}

const handleBackButtonClick = () => {
  resetValues()
  store.setSnmpLookupEditMode(SnmpLookupEditMode.Lookup)
}

const onLookupComplete = (config: SnmpAgentConfig, ip: string) => {
  lookupConfig.value = config
  ipAddress.value = ip
}

const onDetailsCancel = () => {
  store.setSnmpLookupEditMode(SnmpLookupEditMode.Lookup)
}

const onDetailsValidationError = () => {
  snackbar.showSnackBar({ msg: 'Save failed. Please fix invalid values.', error: true })
}

const onDetailsSave = async (config: SnmpAgentConfig, firstIp?: string, lastIp?: string) => {
  const resp = await store.saveDefinition(config, firstIp, lastIp)

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
    padding: 20px;

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
