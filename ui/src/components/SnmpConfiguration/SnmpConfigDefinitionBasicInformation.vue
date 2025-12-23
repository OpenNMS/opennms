<template>
  <div
    class="main-content"
  >
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
        <label class="label">ID:</label>
        <div class="spacer"></div>
        <span class="label">{{ definitionId }}</span>
        <div class="spacer"></div>

        <label class="label">Version:</label>
        <div class="spacer"></div>
        <div class="dropdown">
          <FeatherSelect
            label="Version"
            data-test="snmp-definition-version"
            hint="Select the SNMP version."
            :options="SnmpVersions"
            v-model="snmpVersion"
          >
            <FeatherIcon :icon="MoreVert" />
          </FeatherSelect>
        </div>
  
        <div class="spacer"></div>
        <label class="label">First IP Address:</label>
        <div class="spacer"></div>
        <FeatherInput
          label=""
          data-test="snmp-definition-first-ip-address"
          :error="errors.firstIpAddress"
          v-model.trim="firstIpAddress"
          hint="First IP Address in range"
        >
        </FeatherInput>
        <div class="spacer"></div>
        <label class="label">Second IP Address:</label>
        <div class="spacer"></div>
        <FeatherInput
          label=""
          data-test="snmp-definition-second-ip-address"
          :error="errors.secondIpAddress"
          v-model.trim="secondIpAddress"
          hint="Second IP Address in range"
        >
        </FeatherInput>
  
        <div class="spacer"></div>
        <div class="action-container">
          <FeatherButton
            secondary
            @click="handleCancel"
            data-test="cancel-snmp-definition-button"
          >
            Cancel
          </FeatherButton>
          <FeatherButton
            primary
            @click="handleSaveDefinition"
            data-test="save-definition-button"
            :disabled="!isValid"
          >
            {{ isCreate ? 'Create Definition' : 'Save Changes' }}
          </FeatherButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { FeatherBackButton } from '@featherds/back-button'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import { FeatherInput } from '@featherds/input'
import { FeatherSelect, ISelectItemType } from '@featherds/select'
import { SnmpDefinition, SnmpDefinitionFormErrors } from '@/types/snmpConfig'
import { validateDefinition } from './snmpValidator'
import { useSnmpConfigStore, getDefaultSnmpDefinition } from '@/stores/snmpConfigStore'
import useSnackbar from '@/composables/useSnackbar'
import MoreVert from '@featherds/icon/navigation/MoreVert'

const SnmpVersions: ISelectItemType[] = [
  { _text: 'v1', _value: 'v1' },
  { _text: 'v2c', _value: 'v2c' },
  { _text: 'v3', _value: 'v3' }
]

const props = defineProps<{
  isCreate: boolean,
  definitionId: number
}>()
 
const router = useRouter()
const store = useSnmpConfigStore()
const snackbar = useSnackbar()
const isValid = ref(false)
const errors = ref<SnmpDefinitionFormErrors>({})

const currentDefinition = ref<SnmpDefinition>()
const snmpVersion = ref(SnmpVersions[1])
const firstIpAddress = ref('')
const secondIpAddress = ref('')

const resetValues = () => {
  snmpVersion.value = SnmpVersions[1]
  firstIpAddress.value = ''
  secondIpAddress.value = ''
}

const loadInitialValues = () => {
  if (props.definitionId < 0) {
    currentDefinition.value = getDefaultSnmpDefinition()
  } else {
    // TODO: ensure definitionId is in range
    currentDefinition.value = store.config.definition.find(d => d.id === props.definitionId) ?? getDefaultSnmpDefinition()
  }

  if (currentDefinition.value.version === 'v1') {
    snmpVersion.value = SnmpVersions[0]
  } else if (currentDefinition.value.version === 'v2c') {
    snmpVersion.value = SnmpVersions[1]
  } else if (currentDefinition.value.version === 'v3') {
    snmpVersion.value = SnmpVersions[2]
  }
    
  firstIpAddress.value = currentDefinition.value.range?.[0]?.begin ?? ''
  secondIpAddress.value = currentDefinition.value.range?.[0]?.end ?? ''
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
  const version = String(snmpVersion.value?._value || '')

  const currentErrors = validateDefinition(
    version,
    firstIpAddress.value,
    secondIpAddress.value
  )
  isValid.value = Object.keys(currentErrors).length === 0
  errors.value = currentErrors as SnmpDefinitionFormErrors
}

watch([() => props.definitionId, () => props.isCreate], () => {
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
