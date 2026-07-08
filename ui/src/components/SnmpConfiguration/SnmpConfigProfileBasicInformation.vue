<template>
  <div class="main-content">
    <div class="header">
      <div>
        <PButton
          text
          class="back-button"
          data-test="back-button"
          @click="onDetailsCancel"
        >
          <FeatherIcon :icon="ArrowBack" />
          Go Back
        </PButton>
      </div>
      <div>
        <h3>
          {{ isCreate ? 'Create New SNMP Profile' : 'Edit SNMP Profile Details' }}
        </h3>
      </div>
    </div>
    <div class="spacer"></div>
    <div class="basic-info">
      <div class="section-content">
        <FormField
          class="profile-field"
          label="Label"
          for="snmp-profile-label"
          :error="errors.label"
          hint="Label"
        >
          <PInputText
            id="snmp-profile-label"
            class="profile-input"
            data-test="snmp-profile-label"
            :invalid="!!errors.label"
            v-model.trim="label"
          />
        </FormField>

        <FormField
          class="profile-field"
          label="Filter Expression"
          for="snmp-profile-filter-expression"
          :error="errors.filterExpression"
          hint="Filter expression"
        >
          <PInputText
            id="snmp-profile-filter-expression"
            class="profile-input"
            data-test="snmp-profile-filter-expression"
            :invalid="!!errors.filterExpression"
            v-model.trim="filterExpression"
          />
        </FormField>

        <div class="large-spacer"></div>

        <SnmpConfigDetailsPanel
          v-if="snmpAgentConfig"
          :displayIps="false"
          :suppressMonitoringLocation="true"
          :isCreate="false"
          :firstIp="firstIpAddress"
          :lastIp="lastIpAddress"
          :config="snmpAgentConfig"
          :errors="errors"
          @cancel="onDetailsCancel"
          @validation-error="onDetailsValidationError"
          @save="onDetailsSave"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'

import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import { FeatherIcon } from '@featherds/icon'
import ArrowBack from '@featherds/icon/navigation/ArrowBack'
import useSnackbar from '@/composables/useSnackbar'
import { useSnmpConfigStore, getDefaultSnmpProfile } from '@/stores/snmpConfigStore'
import { SnmpAgentConfig, SnmpConfigFormErrors, SnmpProfile, SnmpProfileFormErrors } from '@/types/snmpConfig'
import SnmpConfigDetailsPanel from './SnmpConfigDetailsPanel.vue'
import FormField from '@/components/Common/FormField.vue'
import { DEFAULT_SNMP_VERSION } from '@/lib/constants'
import { validateProfile } from '@/lib/snmpValidator'

const PButton = Button
const PInputText = InputText

const props = defineProps<{
  isCreate: boolean,
  profileLabel: string
}>()

const emit = defineEmits<{
  (e: 'cancel'): void
  (e: 'save', profile: SnmpProfile): void
  (e: 'validation-error', formErrors: SnmpProfileFormErrors): void
}>()

const store = useSnmpConfigStore()
const snackbar = useSnackbar()
const isValid = ref(false)
const errors = ref<SnmpProfileFormErrors>({})

const currentProfile = ref<SnmpProfile>()
const snmpAgentConfig = ref<SnmpAgentConfig>()
const label = ref('')
const filterExpression = ref('')
// fake ip addresses for the details panel
const firstIpAddress = ref('')
const lastIpAddress = ref('')

const resetValues = () => {
  label.value = ''
  filterExpression.value = ''
}

const loadInitialValues = () => {
  if (props.isCreate || !props.profileLabel) {
    currentProfile.value = getDefaultSnmpProfile()
  } else {
    currentProfile.value = store.config.profiles?.profile?.find(p => p.label === props.profileLabel) ?? getDefaultSnmpProfile()
  }

  snmpAgentConfig.value = {
    port: currentProfile.value.port,
    retry: currentProfile.value.retry,
    timeout: currentProfile.value.timeout,
    readCommunity: currentProfile.value.readCommunity,
    writeCommunity: currentProfile.value.writeCommunity,
    proxyHost: currentProfile.value.proxyHost,
    version: currentProfile.value.version || DEFAULT_SNMP_VERSION,
    maxVarsPerPdu: currentProfile.value.maxVarsPerPdu,
    maxRepetitions: currentProfile.value.maxRepetitions,
    maxRequestSize: currentProfile.value.maxRequestSize,
    securityName: currentProfile.value.securityName,
    securityLevel: currentProfile.value.securityLevel,
    authPassphrase: currentProfile.value.authPassphrase,
    authProtocol: currentProfile.value.authProtocol,
    privacyPassphrase: currentProfile.value.privacyPassphrase,
    privacyProtocol: currentProfile.value.privacyProtocol,
    engineId: currentProfile.value.engineId,
    contextEngineId: currentProfile.value.contextEngineId,
    contextName: currentProfile.value.contextName,
    enterpriseId: currentProfile.value.enterpriseId,
    ttl: currentProfile.value.ttl
  }

  label.value = currentProfile.value.label ?? ''
  filterExpression.value = currentProfile.value.filter ?? ''
}

const onDetailsSave = (config: SnmpAgentConfig) => {
  // just need to validate profile fields here, other validation done in SnmpConfigDetailsPanel
  const profileErrors = validateProfile(
    label.value,
    filterExpression.value
  )

  isValid.value = Object.keys(profileErrors).length === 0

  if (!isValid.value) {
    snackbar.showSnackBar({ msg: 'Invalid values', error: true })

    emit('validation-error', profileErrors)
    return
  }

  try {
    const profileToSave: SnmpProfile = {
      ...config,
      label: label.value,
      filter: filterExpression.value
    }

    emit('save', profileToSave)
  } catch (error) {
    console.error(error)
  }
}

const onDetailsCancel = () => {
  resetValues()
  emit('cancel')
}

const onDetailsValidationError = (formErrors: SnmpConfigFormErrors) => {
  const profileErrors = validateProfile(
    label.value,
    filterExpression.value
  )

  const allErrors = {
    ...formErrors,
    label: profileErrors.label,
    filterExpression: profileErrors.filterExpression
  }

  isValid.value = Object.keys(allErrors).length === 0
  errors.value = allErrors as SnmpProfileFormErrors

  emit('validation-error', errors.value)
}

watch([() => props.profileLabel, () => props.isCreate], () => {
  loadInitialValues()
})

onMounted(() => {
  loadInitialValues()
})
</script>

<style scoped lang="scss">
.main-content {
  padding: 0.2em;
  margin: 0.2em;

  border-radius: 8px;
  background-color: var(--p-content-background);

  .header {
    display: flex;
    align-items: center;
    gap: 20px;

    .back-button {
      gap: 0.4em;
    }
  }

  .basic-info {
    border-width: 1px;
    border-style: solid;
    border-color: var(--p-content-border-color);
    padding: 1em;
    border-radius: 8px;

    .label {
      font-weight: 600;
    }

    .section-content {
      width: 50%;
    }

    .dropdown {
      width: 50%;
    }

    .profile-field {
      margin-bottom: 1rem;
    }

    .profile-input {
      width: 100%;
    }

  }

  .large-spacer {
    min-height: 1em;
  }

  .action-container {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
}
</style>
