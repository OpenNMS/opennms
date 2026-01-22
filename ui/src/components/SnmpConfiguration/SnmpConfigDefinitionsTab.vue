<template>
  <div class="snmp-config-definitions-tab">
    <div class="main-section">
      <SnmpConfigDefinitionsTable v-if="displayTable" />

      <div
        v-else
        class="snmp-config-definition-details"
      >
        <SnmpConfigDefinitionBasicInformation
          :isCreate="store.definitionCreateEditMode === SnmpConfigEditMode.Create"
          :definition="store.currentDefinition ?? null"
          @cancel="handleBackButtonClick"
          @save="onSave"
          @validationError="onValidationError"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import useSnackbar from '@/composables/useSnackbar'
import { SnmpConfigEditMode, useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { SnmpConfigFormErrors, SnmpDefinition } from '@/types/snmpConfig'
import SnmpConfigDefinitionsTable from './SnmpConfigDefinitionsTable.vue'
import SnmpConfigDefinitionBasicInformation from './SnmpConfigDefinitionBasicInformation.vue'

const snackbar = useSnackbar()
const store = useSnmpConfigStore()

const displayTable = computed(() => {
  return store.definitionCreateEditMode === SnmpConfigEditMode.Table
})

const handleBackButtonClick = () => {
  store.setDefinitionCreateEditMode(SnmpConfigEditMode.Table)
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const onValidationError = (errors: SnmpConfigFormErrors) => {
  snackbar.showSnackBar({ msg: 'Please fix invalid values.', error: true })
}

const onSave = async (definition: SnmpDefinition, firstIp?: string, lastIp?: string) => {
  const resp = await store.saveDefinition(definition, firstIp, lastIp)

  if (resp.success) {
    snackbar.showSnackBar({ msg: 'Configuration saved successfully' })
  } else {
    snackbar.showSnackBar({ msg: `Save failed: ${resp.message}`, error: true })
  }

  // get latest config values after save
  await store.populateSnmpConfig()

  store.setDefinitionCreateEditMode(SnmpConfigEditMode.Table)
}
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/vars.scss';

.snmp-config-definitions-tab {
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
