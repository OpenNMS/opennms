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
              <FeatherSelect
                label="Location"
                data-test=""
                hint="Select the location"
                :options="monitoringLocations"
                v-model="selectedMonitoringLocation"
              >
                <FeatherIcon :icon="MoreVert" />
              </FeatherSelect>
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
        <div class="spacer"></div>
        <hr />
        <div class="spacer"></div>
        <div class="section-content">
          <div class="feather-row">
            <div class="feather-col-4">
              <label class="label">IP Address:</label>
            </div>

            <div class="feather-col-8">
              <label class="label">{{ ipAddress ?? '' }}</label>
            </div>
          </div>

          <div class="feather-row">
            <div class="feather-col-4">
              <label class="label">Read Community:</label>
            </div>

            <div class="feather-col-8">
              <FeatherInput
                label=""
                data-test="snmp-lookup-read-community"
                v-model.trim="readCommunity"
                hint="Read community string"
              >
              </FeatherInput>
            </div>
          </div>

          <div class="feather-row">
            <div class="feather-col-4">
              <label class="label">Write Community:</label>
            </div>

            <div class="feather-col-8">
              <FeatherInput
                label=""
                data-test="snmp-lookup-write-community"
                v-model.trim="writeCommunity"
                hint="Write community string"
              >
              </FeatherInput>
            </div>
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
import MoreVert from '@featherds/icon/navigation/MoreVert'
import useSnackbar from '@/composables/useSnackbar'
import { useSnmpConfigStore } from '@/stores/snmpConfigStore'
import { SnmpAgentConfig } from '@/types/snmpConfig'

const snackbar = useSnackbar()
const store = useSnmpConfigStore()
const lookupIpAddress = ref('')
const lookupConfig = ref<SnmpAgentConfig>()
const selectedMonitoringLocation = ref<ISelectItemType>()

// lookup config response individual parameters to edit
const ipAddress = ref('')
const readCommunity = ref('')
const writeCommunity = ref('')

const monitoringLocations = computed<ISelectItemType[]>(() => {
  return store.monitoringLocations.map(loc => {
    return {
      _text: loc.name,
      _value: loc.name
    }
  })
})

const populateEditParams = () => {
  readCommunity.value = lookupConfig.value?.readCommunity ?? ''
  writeCommunity.value = lookupConfig.value?.writeCommunity ?? ''
}

const onLookup = async () => {
  const location = String(selectedMonitoringLocation.value?._value ?? '')

  if (!lookupIpAddress.value || !location) {
    snackbar.showSnackBar({
      msg: 'Must enter IP address and location',
      error: true
    })

    return
  }

  const resp = await store.lookupIpAddress(lookupIpAddress.value, location)

  if (!resp) {
    snackbar.showSnackBar({
      msg: 'Error looking up SNMP Configuration',
      error: true
    })

    return
  }

  lookupConfig.value = resp

  populateEditParams()

  snackbar.showSnackBar({
    msg: 'Found SNMP Configuration'
  })
}
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@import "@featherds/table/scss/table";

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

    .section {
      gap: 10px;
    }
  }
}
</style>
