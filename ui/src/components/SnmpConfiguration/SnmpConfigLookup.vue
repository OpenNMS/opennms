<template>
  <div class="snmp-config-lookup-tab">
    <div class="main-section">
      <div class="title-container">
        <h2>Lookup SNMP Configuration</h2>
      </div>
      <div class="section">
        <div class="section-content">
          <div class="feather-row">
            <div class="feather-col-4">
              <label class="label">IP Address:</label>
            </div>
            <div class="feather-col-8">
              <FeatherInput
                label=""
                data-test=""
                v-model.trim="lookupIpAddress"
                hint="IP Address to look up"
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
                @click="onLookup"
              >
                Lookup Configuration
              </FeatherButton>
             </div>
          </div>
        </div>
        <div class="large-spacer"></div>
        <hr />
        <div class="large-spacer"></div>
        <div class="section-content">
          <SnmpConfigDefinitionDetails
            v-if="lookupConfig"
            :isCreate="false"
            :ipAddress="ipAddress"
            :config="lookupConfig"
            @cancel="onDetailsCancel"
            @validation-error="onDetailsValidationError"
            @save="onDetailsSave"
          />
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
import { useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { SnmpAgentConfig, SnmpDefinitionFormErrors } from '@/types/snmpConfig'
import SnmpConfigDefinitionDetails from './SnmpConfigDefinitionDetails.vue'
import SnmpConfigMonitoringLocationsDropdown from './SnmpConfigMonitoringLocationsDropdown.vue'

const snackbar = useSnackbar()
const store = useSnmpConfigStore()
const lookupIpAddress = ref('')
const lookupConfig = ref<SnmpAgentConfig>()
const lookupMonitoringLocation = ref<ISelectItemType>()

// lookup config response individual parameters to edit
const ipAddress = ref('')

const lookupMonitoringLocationValue = computed<string>(() => {
  return String(lookupMonitoringLocation.value?._value ?? '')
})

const resetValues = () => {
  lookupIpAddress.value = ''
  lookupConfig.value = undefined
  const defaultLoc = store.monitoringLocations.find(loc => loc.name === 'Default')
  lookupMonitoringLocation.value = defaultLoc ? { _text: defaultLoc.name, _value: defaultLoc.name } : undefined
  ipAddress.value = ''
}

const onLookup = async () => {
  const location = String(lookupMonitoringLocation.value?._value ?? '')

  if (!lookupIpAddress.value || !location) {
    snackbar.showSnackBar({
      msg: 'Must enter IP address and location',
      error: true
    })

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
}

const onDetailsCancel = () => {
  alert('onDetailsCancel')
}

const onDetailsValidationError = (formErrors: SnmpDefinitionFormErrors) => {
  alert(`onDetailsValidationError with firstIp of: ${formErrors.firstIpAddress ?? ''}`)
}

const onDetailsSave = (config: SnmpAgentConfig, firstIp?: string, lastIp?: string) => {
  alert(`onDetailsSave with config: ${config.id ?? ''}, firstIp: ${firstIp}, lastIp: ${lastIp}`)
}

onMounted(() => {
  resetValues()
})
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';

.snmp-config-lookup-tab {
  background: var(variables.$surface);
  width: 100%;
  padding: 25px;
  border-radius: 5px;
  margin-top: 10px;

  .main-section {
    display: flex;
    flex-direction: column;
    gap: 20px;
    padding: 20px;

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
}
</style>
