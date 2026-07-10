<template>
  <div
    class="config-details-box"
    data-test="profile-details-box"
  >
    <PCard class="snmp-data-collection-profiles-card">
      <template #title>
        <h4>Profile Details</h4>
      </template>
      <template #content>
        <div class="onms-row">
          <div class="onms-col-6">
            <template v-if="isCreateMode">
              <FormField
                label="Profile Name"
                :for="`profile-name-input`"
                :error="errors.name"
                hint=""
              >
                <PInputText
                  id="profile-name-input"
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
              </FormField>
            </template>
            <template v-else>
              <FormField
                label="Profile Name"
                :for="`profile-name-text`"
                :error="errors.name"
                hint=""
              >
                <span
                  class="field-value"
                  id="profile-name-text"
                >{{ store.selectedProfile?.name }}</span>
              </FormField>
            </template>
          </div>
          <div class="onms-col-6">
            <FormField
              label="Status"
              :for="`profile-status-toggle`"
              hint=""
            >
              <PToggleSwitch
                id="profile-status-toggle"
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
            </FormField>
          </div>
        </div>
        <div class="onms-row">
          <div class="onms-col-6">
            <FormField
              label="Creation Date"
              :for="`profile-creation-date`"
              hint=""
            >
              <span>{{
                store.selectedProfile?.createdTime
                  ? format(store.selectedProfile.createdTime, 'MM/dd/yyyy')
                  : '--'
              }}</span>
            </FormField>
          </div>
          <div class="onms-col-6">
            <FormField
              label="Last Modified"
              :for="`profile-last-modified`"
              hint=""
            >
              <span>{{
                store.selectedProfile?.lastModified
                  ? format(store.selectedProfile.lastModified, 'MM/dd/yyyy')
                  : '--'
              }}</span>
            </FormField>
          </div>
        </div>
        <div class="onms-row">
          <div class="onms-col-6">
            <FormField
              label="Max Vars Per PDU"
              :for="`profile-max-vars-per-pdu`"
              hint=""
            >
              <div class="settings-input">
                <PInputNumber
                  id="profile-max-vars-per-pdu"
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
            </FormField>
          </div>
          <div class="onms-col-6">
            <FormField
              label="Storage Flag"
              :for="`profile-storage-flag`"
              hint=""
            >
              <PSelect
                id="profile-storage-flag"
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
              </FormField>
          </div>
        </div>
      </template>
    </PCard>
  </div>
</template>

<script setup lang="ts">
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { SnmpProfileStorageFlagType } from '@/types/snmpDataCollection'
import type { ConfigDetailsModel, ProfileFormErrors } from '@/types/snmpDataCollection'
import { format } from 'date-fns-tz'
import Card from 'primevue/card'
import InputNumberComponent from 'primevue/inputnumber'
import InputTextComponent from 'primevue/inputtext'
import SelectComponent from 'primevue/select'
import TagComponent from 'primevue/tag'
import ToggleSwitchComponent from 'primevue/toggleswitch'
import FormField from '@/components/Common/FormField.vue'

const PCard = Card
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
.config-details-box {
  padding: 20px 0;
}

.large-spacer {
  min-height: 1em;
}

.settings-input {
  max-width: 20em;
  min-width: 16em;
}

.settings-select {
  background-color: var(--feather-background);
  color: var(--feather-secondary-text-on-surface);
  max-width: 30em;
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

.onms-row {
  margin-bottom: 1rem;
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
