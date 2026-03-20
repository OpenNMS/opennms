<template>
  <div class="snmp-config-profiles-tab">
    <div class="main-section">
      <h3>SNMP Configuration Profiles</h3>
      <div v-if="displayTable" class="info-section">
        <div>
          <span>SNMP profiles provide sets of SNMP configuration that can be applied to devices matching specific filter criteria.</span>
          <FeatherIcon
            :icon="InfoIcon"
            class="info-icon"
            @click="isMessageDialogVisible = true"
            data-test="snmp-config-profiles-info-icon"
          />
        </div>
      </div>
      <SnmpConfigProfilesTable
        v-if="displayTable"
        @deleteProfile="onDeleteProfile"
      />

      <div
        v-else
        class="snmp-config-profile-details"
      >
        <SnmpConfigProfileBasicInformation
          :isCreate="store.snmpProfileEditMode === SnmpConfigEditMode.Create"
          :profileLabel="store.profileLabel"
          @cancel="handleBackButtonClick"
          @save="onSave"
          @validationError="onValidationError"
        />
      </div>
    </div>
    <MessageDialog
      :visible="isMessageDialogVisible"
      title="SNMP Profiles"
      @close="isMessageDialogVisible = false"
    >
      <template #content>
        <div class="message-dialog-content-body">
          <p>SNMP Profiles are prefabricated sets of SNMP configuration that are automatically "fitted" against eligible IP addresses at provisioning time.</p>
          <p>Each profile may have a unique label and an optional filter expression.</p>
          <p>If the filter expression is present, it will be evaluated to check whether a given IP address or reverse-lookup hostname passes the filter.</p>
          <p>A profile with a filter expression will be fitted to a given IP address only if the filter expression evaluates true against that IP address.</p>
        </div>
      </template>
    </MessageDialog>
  </div>
</template>

<script setup lang="ts">
import { FeatherIcon } from '@featherds/icon'
import InfoIcon from '@featherds/icon/action/Info'
import useSnackbar from '@/composables/useSnackbar'
import { SnmpConfigEditMode, useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { SnmpProfile, SnmpProfileFormErrors } from '@/types/snmpConfig'
import SnmpConfigProfilesTable from './SnmpConfigProfilesTable.vue'
import SnmpConfigProfileBasicInformation from './SnmpConfigProfileBasicInformation.vue'
import MessageDialog from '../Common/MessageDialog.vue'

const snackbar = useSnackbar()
const store = useSnmpConfigStore()
const isMessageDialogVisible = ref(false)

const displayTable = computed(() => {
  return store.snmpProfileEditMode === SnmpConfigEditMode.Table
})

const handleBackButtonClick = () => {
  store.setSnmpProfileEditMode(SnmpConfigEditMode.Table)
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const onValidationError = (errors: SnmpProfileFormErrors) => {
  snackbar.showSnackBar({ msg: 'Please fix invalid values.', error: true })
}

const onSave = async (profile: SnmpProfile) => {
  const resp = await store.saveProfile(profile)

  if (resp.success) {
    snackbar.showSnackBar({ msg: 'Profile saved successfully' })
  } else {
    snackbar.showSnackBar({ msg: `Save failed: ${resp.message}`, error: true })
  }

  // get latest config values after save
  await store.populateSnmpConfig()

  store.setSnmpProfileEditMode(SnmpConfigEditMode.Table)
}

const onDeleteProfile = async (label: string) => {
  const resp = await store.deleteProfile(label)

  if (resp.success) {
    snackbar.showSnackBar({ msg: 'Profile deleted successfully' })
  } else {
    snackbar.showSnackBar({ msg: `Delete failed: ${resp.message}`, error: true })
  }

  // get latest config values after delete
  await store.populateSnmpConfig()
}
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/vars.scss';

.snmp-config-profiles-tab {
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
    padding: 20px;

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
