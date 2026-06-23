<template>
  <div
    class="config-details-box"
    data-test="profile-details-box"
  >
    <div class="section-header">Profile Details</div>
    <div class="config-row">
      <div class="config-field">
        <span class="field-label">Name:</span>
        <template v-if="isCreateMode">
          <div class="settings-input">
            <PInputText
              :modelValue="configDetails.name"
              @update:modelValue="update('name', String($event))"
              :invalid="!!errors.name"
              placeholder="Profile Name"
              data-test="profile-name-input"
              fluid
            />
            <span
              v-if="errors.name"
              class="field-error"
            >{{ errors.name }}</span>
          </div>
        </template>
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
          <PTag
            v-if="configDetails.enabled"
            class="enabled-tag"
            value="Enabled"
            data-test="status-tag"
          />
          <PTag
            v-if="!configDetails.enabled"
            class="disabled-tag"
            value="Disabled"
            data-test="status-tag"
          />
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
        <div class="settings-input">
          <PInputNumber
            :modelValue="configDetails.maxVarsPerPdu === '' ? null : Number(configDetails.maxVarsPerPdu)"
            @update:modelValue="update('maxVarsPerPdu', $event == null ? '' : String($event))"
            :useGrouping="false"
            :min="0"
            :invalid="!!errors.maxVarsPerPdu"
            data-test="max-vars-per-pdu"
            fluid
          />
          <small
            v-if="errors.maxVarsPerPdu"
            class="field-error"
          >{{ errors.maxVarsPerPdu }}</small>
          <small
            v-else
            class="field-hint"
          >Leave empty or set to 0 to use the default value.</small>
        </div>
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
import { format } from 'date-fns-tz'
import InputNumberComponent from 'primevue/inputnumber'
import InputTextComponent from 'primevue/inputtext'
import SelectComponent from 'primevue/select'
import TagComponent from 'primevue/tag'
import ToggleSwitchComponent from 'primevue/toggleswitch'

const PToggleSwitch = ToggleSwitchComponent
const PSelect = SelectComponent
const PInputText = InputTextComponent
const PInputNumber = InputNumberComponent
const PTag = TagComponent

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
  display: block;
  color: var(--p-red-500);
  font-size: 0.8em;
  margin-top: 0.25em;
}

.field-hint {
  display: block;
  color: var(--p-text-muted-color);
  font-size: 0.8em;
  margin-top: 0.25em;
}

.tag {
  .enabled-tag {
    margin: 0 !important;
    border-radius: 1em;
    background-color: #0B720C1F;
    border: 2px solid #0B720C;

    :deep(.p-tag-label) {
      color: #0B720C !important;
    }
  }

  .disabled-tag {
    margin: 0 !important;
    border-radius: 1em;
    background-color: #7575751F;
    border: 2px solid #757575;

    :deep(.p-tag-label) {
      color: #757575 !important;
    }
  }
}
</style>
