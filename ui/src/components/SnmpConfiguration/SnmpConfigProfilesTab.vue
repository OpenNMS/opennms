<template>
  <div class="snmp-config-profiles-tab">
    <div class="main-section">
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
  </div>
</template>

<script setup lang="ts">
import useSnackbar from '@/composables/useSnackbar'
import { SnmpConfigEditMode, useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { SnmpProfile, SnmpProfileFormErrors } from '@/types/snmpConfig'
import SnmpConfigProfilesTable from './SnmpConfigProfilesTable.vue'
import SnmpConfigProfileBasicInformation from './SnmpConfigProfileBasicInformation.vue'

const snackbar = useSnackbar()
const store = useSnmpConfigStore()

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
