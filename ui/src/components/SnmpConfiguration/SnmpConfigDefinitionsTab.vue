<template>
  <div class="snmp-config-definitions-tab">
    <div class="main-section">
      <h3>SNMP Configuration Definitions</h3>
      <div class="info-section">
        <div v-if="displayTable">
          <span>SNMP definitions display how IP addresses, ranges, or patterns are currently configured.</span>
          <FeatherIcon
            :icon="InfoIcon"
            class="info-icon"
            @click="isMessageDialogVisible = true"
            data-test="snmp-config-definitions-info-icon"
          />
        </div>
      </div>

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
    <MessageDialog
      :visible="isMessageDialogVisible"
      title="SNMP Definitions"
      @close="isMessageDialogVisible = false"
    >
      <template #content>
        <div class="message-dialog-content-body">
          <p>SNMP definitions display how IP addresses, ranges, or patterns are currently configured.</p>
          <p>You may add new definitions or edit existing ones to customize SNMP settings for specific devices.</p>
          <p>You may also delete definitions, in which case the SNMP configuration for those devices will revert to the system defaults.</p>
          <p>Note that OpenNMS also modifies and optimizes these configurations automatically.</p>
        </div>
      </template>
    </MessageDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

import { FeatherIcon } from '@featherds/icon'
import InfoIcon from '@featherds/icon/action/Info'
import useSnackbar from '@/composables/useSnackbar'
import { SnmpConfigEditMode, useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { SnmpConfigFormErrors, SnmpDefinition } from '@/types/snmpConfig'
import SnmpConfigDefinitionsTable from './SnmpConfigDefinitionsTable.vue'
import SnmpConfigDefinitionBasicInformation from './SnmpConfigDefinitionBasicInformation.vue'
import MessageDialog from '../Common/MessageDialog.vue'

const snackbar = useSnackbar()
const store = useSnmpConfigStore()
const isMessageDialogVisible = ref(false)

const displayTable = computed(() => {
  return store.definitionCreateEditMode === SnmpConfigEditMode.Table
})

const handleBackButtonClick = () => {
  store.setDefinitionCreateEditMode(SnmpConfigEditMode.Table)
  store.resetCurrentDefinition()
}

const onValidationError = (errors: SnmpConfigFormErrors) => {
  const invalidRange = errors.invalidRangeConfig || errors.mixingRangeWithIpMatch || errors.duplicateRangeItem

  if (invalidRange) {
    snackbar.showSnackBar({
      msg: 'Cannot add range. ' + invalidRange,
      error: true
    })

    return
  }

  snackbar.showSnackBar({ msg: 'Please fix invalid values.', error: true })
}

const onSave = async (definition: SnmpDefinition) => {
  const resp = await store.saveDefinition(definition)

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
    gap: 0;
    padding: 1.5em;

    .info-section {
      margin-bottom: 1em;

      .label {
        color: var(variables.$primary-text-on-surface);
      }

      .info-icon {
        cursor: pointer;
        font-size: 1.5em;
        margin-left: 0.5em;
        color: var(variables.$primary);

        &:hover {
          opacity: 0.8;
        }
      }
    }

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
