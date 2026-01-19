<template>
  <div class="main-content">
    <div class="header">
      <div>
        <FeatherBackButton
          data-test="back-button"
          @click="handleBackButtonClick"
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
        <h4>IP Ranges:</h4>

        <div class="feather-row" v-if="(currentDefinition?.ranges?.length ?? 0) > 0">
          <div class="feather-col-12">
            <span>{{ currentDefinition?.ranges?.map(r => `${r.begin}-${r.end}`).join(', ') }}</span>
          </div>
        </div>

        <div class="feather-row" v-if="(currentDefinition?.specifics?.length ?? 0) > 0">
          <div class="feather-col-12">
            <span>{{ currentDefinition?.specifics.join(', ') }}</span>
          </div>
        </div>

        <div class="feather-row" v-if="(currentDefinition?.ipMatches?.length ?? 0) > 0">
          <div class="feather-col-12">
            <span>{{ currentDefinition?.ipMatches.join(', ') }}</span>
          </div>
        </div>

        <div class="large-spacer"></div>

        <div class="section-content">
          <SnmpConfigDefinitionDetails
            v-if="snmpAgentConfig"
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
  </div>
</template>

<script setup lang="ts">
import { FeatherBackButton } from '@featherds/back-button'
import { SnmpAgentConfig, SnmpDefinition, SnmpDefinitionFormErrors } from '@/types/snmpConfig'
import { convertSnmpVersionToString } from '@/services/snmpConfigService'
import { getDefaultSnmpDefinition, useSnmpConfigStore } from '@/stores/snmpConfigStore'
import useSnackbar from '@/composables/useSnackbar'
import SnmpConfigDefinitionDetails from './SnmpConfigDefinitionDetails.vue'

const props = defineProps<{
  isCreate: boolean,
  definition: SnmpDefinition | null
}>()
 
const router = useRouter()
const store = useSnmpConfigStore()
const snackbar = useSnackbar()
const errors = ref<SnmpDefinitionFormErrors>({})

const currentDefinition = ref<SnmpDefinition>()
const firstIpAddress = ref('')
const lastIpAddress = ref('')

const snmpAgentConfig = computed(() => {
  const config = {
    version: convertSnmpVersionToString(currentDefinition.value?.version ?? 'v2c'),
    location: currentDefinition.value?.location ?? 'Default',
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
  lastIpAddress.value = ''
}

const loadInitialValues = () => {
  if (!props.definition) {
    currentDefinition.value = getDefaultSnmpDefinition()
  } else {
    currentDefinition.value = props.definition
  }

  if (currentDefinition.value.ranges && currentDefinition.value.ranges.length > 0) {
    firstIpAddress.value = currentDefinition.value.ranges[0].begin
    lastIpAddress.value = currentDefinition.value.ranges[0].end
  } else if (currentDefinition.value.specifics && currentDefinition.value.specifics.length === 1) {
    firstIpAddress.value = currentDefinition.value.specifics[0]
    lastIpAddress.value = ''
  } else {
    firstIpAddress.value = ''
    lastIpAddress.value = ''
  }
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const onDetailsValidationError = (formErrors: SnmpDefinitionFormErrors) => {
  snackbar.showSnackBar({ msg: 'Save failed. Please fix invalid values.', error: true })
}

const onDetailsSave = async (config: SnmpAgentConfig, firstIp?: string, lastIp?: string) => {
  const resp = await store.saveDefinition(config, firstIp, lastIp)

  if (resp.success) {
    snackbar.showSnackBar({ msg: 'Configuration saved successfully' })
  } else {
    snackbar.showSnackBar({ msg: `Save failed: ${resp.message}`, error: true })
  }

  // get latest config values after save
  await store.populateSnmpConfig()
}

const onDetailsCancel = () => {
  resetValues()
}

const handleBackButtonClick = () => {
  router.push({
    name: 'SNMP Config'
  })
}

watch([() => props.definition, () => props.isCreate], () => {
  loadInitialValues()
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
      width: 80%;
    }

    .dropdown {
      width: 50%;
    }
  }

  .large-spacer {
    min-height: 1em;
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
