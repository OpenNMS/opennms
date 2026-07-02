<template>
  <div class="snmp-config-lookup-panel">
    <div class="title-container">
      <h3>Lookup by IP</h3>
    </div>

    <div class="info-section">
      <span>Find the SNMP configuration that exists for a particular IP address.</span>
      <FeatherIcon
        :icon="InfoIcon"
        class="info-icon"
        @click="isMessageDialogVisible = true"
        data-test="snmp-config-lookup-info-icon"
      />
    </div>
    <div class="large-spacer"></div>

    <div class="section">
      <div class="section-content">
        <FormField
          class="lookup-field"
          label="IP Address"
          for="snmp-lookup-ip-address"
          hint="Enter IP Address"
        >
          <PInputText
            id="snmp-lookup-ip-address"
            class="lookup-input"
            data-test="lookup-ip-address-input"
            v-model.trim="lookupIpAddress"
          />
        </FormField>
        <FormField
          class="lookup-field"
          label="Location"
          for="snmp-lookup-location"
          hint="Select a monitoring location"
        >
          <PSelect
            inputId="snmp-lookup-location"
            class="lookup-input"
            data-test="snmp-monitoring-location-select"
            optionLabel="_text"
            :options="monitoringLocations"
            :modelValue="lookupMonitoringLocation"
            @update:modelValue="(val: any) => lookupMonitoringLocation = val"
          />
        </FormField>

        <div class="onms-row">
          <div class="onms-col-12">
            <PButton
              label="Lookup"
              data-test="refresh-button"
              :disabled="isLoading"
              @click="onLookup"
            />
          </div>
        </div>
      </div>
    </div>
    <MessageDialog
      :visible="isMessageDialogVisible"
      maxHeight="22em"
      maxWidth="50em"
      title="SNMP Lookup"
      @close="isMessageDialogVisible = false"
    >
      <template #content>
        <div>
          <p>The SNMP Configuration Lookup feature allows you to quickly find the SNMP configuration for a specific IP address.</p>
          <p>Enter an IP address in the lookup field and choose a Monitoring Location, then click the *Lookup* button.
          If you are not using Minions, then the Monitoring Location is always `Default`. If you are using Minions, then the Monitoring Location is determined by the Minion that is responsible for monitoring the IP address.</p>
          <p>The SNMP configuration that applies to that IP address will then be displayed.</p>
        </div>
      </template>
    </MessageDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import { FeatherIcon } from '@featherds/icon'
import InfoIcon from '@featherds/icon/action/Info'
import { ISelectItemType } from '@featherds/select'
import FormField from '@/components/Common/FormField.vue'
import MessageDialog from '../Common/MessageDialog.vue'
import useSnackbar from '@/composables/useSnackbar'
import { SnmpLookupEditMode, useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { SnmpAgentConfig } from '@/types/snmpConfig'
import { DEFAULT_MONITORING_LOCATION } from '@/lib/constants'

const PButton = Button
const PInputText = InputText
const PSelect = Select

const props = defineProps<{
  autoLookupIpAddress?: string
  autoLookupLocation?: string
}>()

const snackbar = useSnackbar()
const store = useSnmpConfigStore()
const lookupIpAddress = ref('')
const lookupConfig = ref<SnmpAgentConfig>()
const lookupMonitoringLocation = ref<ISelectItemType>({ _text: DEFAULT_MONITORING_LOCATION, _value: DEFAULT_MONITORING_LOCATION })
const isLoading = ref(false)
const isMessageDialogVisible = ref(false)

// lookup config response individual parameters to edit
const ipAddress = ref('')

const emit = defineEmits<{
  (e: 'lookup-complete', config: SnmpAgentConfig, ipAddress: string): void
}>()

const monitoringLocations = computed<ISelectItemType[]>(() => {
  return store.monitoringLocations.map((loc) => {
    return {
      _text: loc.name,
      _value: loc.name
    }
  })
})

const resetValues = () => {
  lookupIpAddress.value = ''
  lookupConfig.value = undefined
  lookupMonitoringLocation.value = { _text: DEFAULT_MONITORING_LOCATION, _value: DEFAULT_MONITORING_LOCATION }
  ipAddress.value = ''
}

const performLookup = async () => {
  const location = String(lookupMonitoringLocation.value?._value ?? '')

  if (!lookupIpAddress.value || !location) {
    snackbar.showSnackBar({
      msg: 'Must enter IP address and location',
      error: true
    })

    isLoading.value = false
    return
  }

  lookupConfig.value = undefined

  const resp = await store.lookupIpAddress(lookupIpAddress.value, location)

  if (!resp) {
    snackbar.showSnackBar({
      msg: 'Error looking up SNMP Configuration',
      error: true
    })

    return
  }

  ipAddress.value = resp.address ?? ''
  lookupConfig.value = resp

  snackbar.showSnackBar({
    msg: 'Found SNMP Configuration'
  })

  store.setSnmpLookupEditMode(SnmpLookupEditMode.Edit)
  emit('lookup-complete', resp, ipAddress.value)
}

const onLookup = async () => {
  isLoading.value = true

  try {
    await performLookup()
  } finally {
    isLoading.value = false
  }
}

watch(() => props.autoLookupIpAddress, (ip) => {
  resetValues()

  // auto-lookup mode for url like '/snmp-config/lookup?ipAddress=X&location=Y'
  if (ip) {
    lookupIpAddress.value = ip

    const locationIsDefault = !props.autoLookupLocation || props.autoLookupLocation.localeCompare(DEFAULT_MONITORING_LOCATION, undefined, { sensitivity: 'base' }) === 0
    const locationName = locationIsDefault ? DEFAULT_MONITORING_LOCATION : props.autoLookupLocation
    const matched = monitoringLocations.value.find(loc => loc._value === locationName)

    // If non-default location is provided, check if it matches any of the available monitoring locations.
    // Note, this could also happen if the monitoring locations are still loading and haven't been populated yet.
    // User can then manually select the location and trigger lookup.
    if (!locationIsDefault && !matched) {
      snackbar.showSnackBar({
        msg: `Monitoring location ${locationName} from URL is not valid. Please select a valid monitoring location.`,
        error: true
      })
      return
    }

    lookupMonitoringLocation.value = matched ?? { _text: locationName, _value: locationName }
    onLookup()
  }
}, { immediate: true })
</script>

<style scoped lang="scss">
.snmp-config-lookup-panel {
  background: var(--p-content-background);
  width: 100%;
  padding: 1.5em;
  border-radius: 5px;

  .title-container {
    display: flex;
    align-items: center;
  }

  .info-section {
    margin-bottom: 1em;

    .info-icon {
      cursor: pointer;
      font-size: 1.5em;
      margin-left: 0.5em;
      color: var(--p-primary-color);

      &:hover {
        opacity: 0.8;
      }
    }
  }

  .onms-row {
    margin-bottom: 0.5rem;
  }

  .lookup-field {
    margin-bottom: 1rem;
  }

  .lookup-input {
    width: 100%;
  }

  .large-spacer {
    min-height: 1em;
  }

  .section {
    gap: 10px;
    width: 70em;
  }

  .section-content {
    width: 70%;
  }
}
</style>
