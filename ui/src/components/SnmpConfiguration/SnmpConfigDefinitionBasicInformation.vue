<template>
  <div class="main-content">
    <div class="header">
      <div>
        <FeatherBackButton
          data-test="back-button"
          @click="handleCancel"
        >
          Go Back
        </FeatherBackButton>
      </div>
      <div>
        <h3>
          {{ isCreate ? 'Create New SNMP Definition' : 'Edit SNMP Definition Details' }}
        </h3>
      </div>
    </div>
    <div class="spacer"></div>
    <div class="basic-info">
      <div class="section-content">
        <div class="feather-row" v-if="(currentDefinition?.specifics?.length ?? 0) > 0">
          <div class="feather-col-4">
            <label class="label">Specific IPs:</label>
          </div>
          <div class="feather-col-8">
            <span>{{ currentDefinition?.specifics.join(', ') }}</span>
          </div>
        </div>
        <div class="feather-row" v-if="(currentDefinition?.ranges?.length ?? 0) > 0">
          <div class="feather-col-4">
            <label class="label">IP Ranges:</label>
          </div>
          <div class="feather-col-8">
            <span>{{ currentDefinition?.ranges?.map(r => `${r.begin}-${r.end}`).join(', ') }}</span>
          </div>
        </div>
        <div class="feather-row" v-if="(currentDefinition?.ipMatches?.length ?? 0) > 0">
          <div class="feather-col-4">
            <label class="label">IP Matches:</label>
          </div>
          <div class="feather-col-8">
            <span>{{ currentDefinition?.ipMatches?.join(', ') }}</span>
          </div>
        </div>

        <div class="section-content">
          <SnmpConfigDefinitionDetails
            v-if="snmpAgentConfig"
            :isCreate="false"
            :ipAddress="firstIpAddress"
            :config="snmpAgentConfig"
            @cancel="onDetailsCancel"
            @validation-error="onDetailsValidationError"
            @save="onDetailsSave"
          />
        </div>

        <div class="spacer"></div>
        <hr />
        <div class="spacer"></div>

        <div class="feather-row">
          <div class="feather-col-4">
            <label class="label">First IP Address:</label>
          </div>
          <div class="feather-col-8">
            <FeatherInput
              label=""
              data-test="snmp-definition-first-ip-address"
              :error="errors.firstIpAddress"
              v-model.trim="firstIpAddress"
              hint="First IP Address in range"
            >
            </FeatherInput>
          </div>
        </div>
        <div class="feather-row">
          <div class="feather-col-4">
            <label class="label">Second IP Address:</label>
          </div>
          <div class="feather-col-8">
            <FeatherInput
              label=""
              data-test="snmp-definition-second-ip-address"
              :error="errors.secondIpAddress"
              v-model.trim="secondIpAddress"
              hint="Second IP Address in range"
            >
            </FeatherInput>
          </div>
        </div>
        <div class="feather-row">
          <div class="feather-col-12">
            <div class="action-container">
              <FeatherButton
                primary
                @click="handleSaveDefinition"
                data-test="save-definition-button"
                :disabled="!isValid"
              >
                {{ isCreate ? 'Create Definition' : 'Save Changes' }}
              </FeatherButton>
              <FeatherButton
                secondary
                @click="handleCancel"
                data-test="cancel-snmp-definition-button"
              >
                Cancel
              </FeatherButton>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { FeatherBackButton } from '@featherds/back-button'
import { FeatherButton } from '@featherds/button'
import { FeatherInput } from '@featherds/input'
import { SnmpAgentConfig, SnmpDefinition, SnmpDefinitionFormErrors } from '@/types/snmpConfig'
import { validateDefinition } from './snmpValidator'
import { convertSnmpVersionToString } from '@/services/snmpConfigService'
import { getDefaultSnmpDefinition } from '@/stores/snmpConfigStore'
import useSnackbar from '@/composables/useSnackbar'
import SnmpConfigDefinitionDetails from './SnmpConfigDefinitionDetails.vue'

const props = defineProps<{
  isCreate: boolean,
  definition: SnmpDefinition | null
}>()
 
const router = useRouter()
const snackbar = useSnackbar()
const isValid = ref(false)
const errors = ref<SnmpDefinitionFormErrors>({})

const currentDefinition = ref<SnmpDefinition>()
const firstIpAddress = ref('')
const secondIpAddress = ref('')

const snmpAgentConfig = computed(() => {
  const config = {
    version: convertSnmpVersionToString(currentDefinition.value?.version ?? 'v2c'),
    readCommunity: currentDefinition.value?.readCommunity ?? '',
    writeCommunity: currentDefinition.value?.writeCommunity ?? '',
    timeout: currentDefinition.value?.timeout ?? undefined,
    retry: currentDefinition.value?.retry ?? undefined,
    port: currentDefinition.value?.port ?? undefined,
    proxyHost: currentDefinition.value?.proxyHost ?? '',
    maxRequestSize: currentDefinition.value?.maxRequestSize ?? undefined,
    maxVarsPerPdu: currentDefinition.value?.maxVarsPerPdu ?? undefined,
    maxRepetitions: currentDefinition.value?.maxRepetitions ?? undefined,
    ttl: currentDefinition.value?.ttl ?? undefined,
    securityName: currentDefinition.value?.securityName ?? '',
    securityLevel: currentDefinition.value?.securityLevel ?? undefined,
    authPassphrase: currentDefinition.value?.authPassphrase ?? '',
    authProtocol: currentDefinition.value?.authProtocol ?? '',
    engineId: currentDefinition.value?.engineId ?? '',
    contextEngineId: currentDefinition.value?.contextEngineId ?? '',
    contextName: currentDefinition.value?.contextName ?? '',
    privacyPassphrase: currentDefinition.value?.privacyPassphrase ?? '',
    privacyProtocol: currentDefinition.value?.privacyProtocol ?? '',
    enterpriseId: currentDefinition.value?.enterpriseId ?? ''
  } as SnmpAgentConfig

  return config
})

const resetValues = () => {
  firstIpAddress.value = ''
  secondIpAddress.value = ''
}

const loadInitialValues = () => {
  if (!props.definition) {
    currentDefinition.value = getDefaultSnmpDefinition()
  } else {
    // TODO: ensure definitionId is in range
    // currentDefinition.value = store.config.definitions.find(d => d.id === props.definitionId) ?? getDefaultSnmpDefinition()
    currentDefinition.value = props.definition
  }

  firstIpAddress.value = currentDefinition.value.ranges?.[0]?.begin ?? ''
  secondIpAddress.value = currentDefinition.value.ranges?.[0]?.end ?? ''
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

const handleSaveDefinition = async () => {
  handleValidate()

  try {
    if (!isValid.value) {
      snackbar.showSnackBar({ msg: 'Invalid values', error: true })
      return
    }

    // TODO: save values to store and then to Rest API
    snackbar.showSnackBar({ msg: props.isCreate ? 'Definition created successfully' : 'Definition updated successfully', error: false })
  } catch (error) {
    console.error(error)
  }
}

const handleCancel = () => {
  resetValues()

  router.push({
    name: 'SNMP Config'
  })
}

const handleValidate = () => {
  const currentErrors = validateDefinition(
    convertSnmpVersionToString(currentDefinition.value?.version ?? ''),
    firstIpAddress.value,
    secondIpAddress.value
  )
  isValid.value = Object.keys(currentErrors).length === 0
  errors.value = currentErrors as SnmpDefinitionFormErrors
}

watch([() => props.definition, () => props.isCreate], () => {
  loadInitialValues()
})

watchEffect(() => {
  handleValidate()
})

onMounted(() => {
  loadInitialValues()
})
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use "@featherds/table/scss/table";

.main-content {
  padding: 30px;
  margin: 30px;

  border-radius: 8px;
  background-color: #ffffff;

  .header {
    display: flex;
    align-items: center;
    gap: 20px;
  }

  .basic-info {
    border-width: 1px;
    border-style: solid;
    border-color: var(variables.$border-on-surface);
    padding: 10px;
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
  }

  .spacer {
    min-height: 0.5em;
  }

  .action-container {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
}
</style>
