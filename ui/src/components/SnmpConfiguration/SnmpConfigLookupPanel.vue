<template>
  <div class="snmp-config-lookup-panel">
    <div class="title-container">
      <h3>Lookup by IP</h3>
    </div>
    <div>
      <p>Find the SNMP configuration that exists for a particular IP.</p>
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
            <SnmpConfigMonitoringLocationsDropdown
              :monitoringLocation="lookupMonitoringLocationValue"
              @update:modelValue="lookupMonitoringLocation = $event"
            />
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
import { ISelectItemType } from '@featherds/select'
import useSnackbar from '@/composables/useSnackbar'
import { SnmpLookupEditMode, useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { SnmpAgentConfig } from '@/types/snmpConfig'
import SnmpConfigMonitoringLocationsDropdown from './SnmpConfigMonitoringLocationsDropdown.vue'
import { DEFAULT_MONITORING_LOCATION } from '@/lib/constants'

const snackbar = useSnackbar()
const store = useSnmpConfigStore()
const lookupIpAddress = ref('')
const lookupConfig = ref<SnmpAgentConfig>()
const lookupMonitoringLocation = ref<ISelectItemType>()
const isLoading = ref(false)

// lookup config response individual parameters to edit
const ipAddress = ref('')

const emit = defineEmits<{
  (e: 'lookup-complete', config: SnmpAgentConfig, ipAddress: string): void
}>()
 
const lookupMonitoringLocationValue = computed<string>(() => {
  return String(lookupMonitoringLocation.value?._value ?? DEFAULT_MONITORING_LOCATION)
})

const resetValues = () => {
  lookupIpAddress.value = ''
  lookupConfig.value = undefined
  const defaultLoc = store.monitoringLocations.find(loc => loc.name === DEFAULT_MONITORING_LOCATION)
  lookupMonitoringLocation.value = defaultLoc ? { _text: defaultLoc.name, _value: defaultLoc.name } : undefined
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

onMounted(() => {
  resetValues()
})
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';

.snmp-config-lookup-panel {
  background: var(variables.$surface);
  width: 100%;
  padding: 25px;
  border-radius: 5px;
  margin-top: 10px;

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
