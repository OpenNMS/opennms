<template>
  <div
    class="config-details-box"
    data-test="profile-details-box"
  >
    <div class="section-header">Profile Details</div>
    <div class="config-row">
      <div class="config-field">
        <span class="field-label">Name:</span>
        <FeatherInput
          v-if="isCreateMode"
          label="Profile Name"
          :modelValue="configDetails.name"
          @update:modelValue="update('name', String($event))"
          :error="errors.name"
          data-test="profile-name-input"
          class="settings-input"
        />
        <span
          v-else
          class="field-value"
        >{{ store.selectedProfile?.name }}</span>
      </div>
      <div class="config-field switch-field">
        <span class="field-label">Status:</span>
        <PToggleSwitch
          :modelValue="configDetails.enabled"
          @update:modelValue="update('enabled', Boolean($event))"
          data-test="profile-enabled-switch"
        />
        <div class="tag">
          <FeatherChip
            v-if="configDetails.enabled"
            class="enabled-tag"
            data-test="status-tag"
          >
            Enabled
          </FeatherChip>
          <FeatherChip
            v-if="!configDetails.enabled"
            class="disabled-tag"
            data-test="status-tag"
          >
            Disabled
          </FeatherChip>
        </div>
      </div>
    </div>
    <div class="config-row">
      <div class="config-field">
        <span class="field-label">Creation Date:</span>
        <span class="field-value">{{
          store.selectedProfile?.createdTime
            ? format(store.selectedProfile.createdTime, 'MM/dd/yyyy')
            : '--'
        }}</span>
      </div>
      <div class="config-field">
        <span class="field-label">Last Modified:</span>
        <span class="field-value">{{
          store.selectedProfile?.lastModified
            ? format(store.selectedProfile.lastModified, 'MM/dd/yyyy')
            : '--'
        }}</span>
      </div>
    </div>
    <div class="config-row">
      <div class="config-field">
        <span class="field-label">Max Vars Per PDU:</span>
        <FeatherInput
          label="Max Vars Per PDU"
          hint="Leave empty or set to 0 to use the default value."
          :modelValue="configDetails.maxVarsPerPdu"
          @update:modelValue="update('maxVarsPerPdu', String($event))"
          type="number"
          :error="errors.maxVarsPerPdu"
          data-test="max-vars-per-pdu"
          class="settings-input"
        />
      </div>
      <div class="config-field">
        <span class="field-label">Storage Flag:</span>
        <PSelect
          :modelValue="configDetails.storageFlag"
          @update:modelValue="update('storageFlag', String($event))"
          :options="storageFlagOptions"
          optionLabel="label"
          optionValue="value"
          :invalid="!!errors.storageFlag"
          data-test="storage-flag-select"
          class="settings-select"
        />
        <span
          v-if="errors.storageFlag"
          class="field-error"
        >{{ errors.storageFlag }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { SnmpProfileStorageFlagType } from '@/types/snmpDataCollection'
import type { ConfigDetailsModel, ProfileFormErrors } from '@/types/snmpDataCollection'
import { FeatherChip } from '@featherds/chips'
import { FeatherInput } from '@featherds/input'
import { format } from 'date-fns-tz'
import ToggleSwitchComponent from 'primevue/toggleswitch'
import SelectComponent from 'primevue/select'

const PToggleSwitch = ToggleSwitchComponent
const PSelect = SelectComponent

const props = defineProps<{
  configDetails: ConfigDetailsModel
  isCreateMode: boolean
  errors: ProfileFormErrors
}>()

const emit = defineEmits<{
  'update:configDetails': [value: ConfigDetailsModel]
}>()

const store = useSnmpDataCollectionStore()

const storageFlagOptions = Object.values(SnmpProfileStorageFlagType).map(v => ({
  label: v.charAt(0).toUpperCase() + v.slice(1),
  value: v
}))

const update = <K extends keyof ConfigDetailsModel>(key: K, value: ConfigDetailsModel[K]) => {
  emit('update:configDetails', { ...props.configDetails, [key]: value })
}
</script>

<style lang="scss" scoped>
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

.config-details-box {
  padding: 20px 0;
}

.section-header {
  @include headline3;
  margin-bottom: 16px;
}

.config-row {
  display: flex;
  align-items: center;

  .config-field {
    display: flex;
    align-items: center;
    flex: 1;
    margin-right: 40px;

    .field-label {
      @include headline4;
      margin-right: 10px;
      color: var(--feather-secondary-text-on-surface);
      min-width: 110px;
    }

    .field-value {
      @include body-large;
    }

    &.switch-field {
      gap: 12px;
    }
  }
}

.settings-input {
  max-width: 20em;
  min-width: 16em;
  margin-top: 1.5em;
}

.settings-select {
  background-color: var(--feather-background);
  color: var(--feather-secondary-text-on-surface);
  min-width: 160px;
}

.field-error {
  color: var(--feather-error);
  font-size: 0.8em;
  margin-left: 8px;
}

.tag {
  .enabled-tag {
    margin: 0 !important;
    border-radius: 1em;
    background-color: #0B720C1F;
    border-color: #0B720C;
    border-width: 2px;

    :deep(span) {
      color: #0B720C !important;
    }
  }

  .disabled-tag {
    margin: 0 !important;
    border-radius: 1em;
    background-color: #7575751F;
    border-color: #757575;
    border-width: 2px;

    :deep(span) {
      color: #757575 !important;
    }
  }
}
</style>
