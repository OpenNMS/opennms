<template>
  <div class="snmp-config-lookup-panel">
    <div class="title-container">
      <h3>Lookup by IP</h3>
    </div>
    <div>
      <p>Find the SNMP configuration that exists for a particular IP address.</p>
    </div>
    <div class="large-spacer"></div>

    <div class="section">
      <div class="section-content">
        <div class="feather-row">
          <div class="feather-col-4">
            <label class="label">IP Address:</label>
          </div>
          <div class="feather-col-8">
            <FeatherInput
              label=""
              data-test="lookup-ip-address-input"
              v-model.trim="lookupIpAddress"
              hint="Enter IP Address"
            >
            </FeatherInput>
          </div>
        </div>
        <div class="feather-row">
          <div class="feather-col-4">
            <label class="label">Location:</label>
          </div>
          <div class="feather-col-8">
            <FeatherSelect
              label="Location"
              data-test="snmp-monitoring-location-select"
              hint="Select a monitoring location"
              :options="monitoringLocations"
              :modelValue="lookupMonitoringLocation"
              @update:modelValue="(val: any) => lookupMonitoringLocation = val"
            >
            </FeatherSelect>
          </div>
        </div>

        <div class="feather-row">
          <div class="feather-col-12">
            <FeatherButton
              primary
              data-test="refresh-button"
              :disabled="isLoading"
              @click="onLookup"
            >
              Lookup
            </FeatherButton>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { FeatherButton } from '@featherds/button'
import { FeatherInput } from '@featherds/input'
import { FeatherSelect, ISelectItemType } from '@featherds/select'
import useSnackbar from '@/composables/useSnackbar'
import { SnmpLookupEditMode, useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { SnmpAgentConfig } from '@/types/snmpConfig'
import { DEFAULT_MONITORING_LOCATION } from '@/lib/constants'

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

// lookup config response individual parameters to edit
const ipAddress = ref('')

const emit = defineEmits<{
  (e: 'lookup-complete', config: SnmpAgentConfig, ipAddress: string): void
}>()
 
const monitoringLocations = computed<ISelectItemType[]>(() => {
  return store.monitoringLocations.map(loc => {
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
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';

.snmp-config-lookup-panel {
  background: var(variables.$surface);
  width: 100%;
  padding: 1.5em;
  border-radius: 5px;

  .title-container {
    display: flex;
    align-items: center;

    .title {
      @include typography.headline3;
    }
  }

  .feather-row {
    margin-bottom: 0.5rem;
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
