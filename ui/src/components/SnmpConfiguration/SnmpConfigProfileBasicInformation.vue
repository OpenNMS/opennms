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
          {{ isCreate ? 'Create New SNMP Profile' : 'Edit SNMP Profile Details' }}
        </h3>
      </div>
    </div>
    <div class="spacer"></div>
    <div class="basic-info">
      <div class="section-content">
        <label class="label">ID:</label>
        <div class="spacer"></div>
        <span class="label">{{ profileId }}</span>
        <div class="spacer"></div>
 
        <label class="label">Label:</label>
        <div class="spacer"></div>
        <FeatherInput
          label=""
          data-test="snmp-profile-label"
          :error="errors.label"
          v-model.trim="label"
          hint="Label"
        >
        </FeatherInput>
        <div class="spacer"></div>
        <label class="label">Filter Expression:</label>
        <div class="spacer"></div>
        <FeatherInput
          label=""
          data-test="snmp-profile-filter-expression"
          :error="errors.filterExpression"
          v-model.trim="filterExpression"
          hint="Filter expression"
        >
        </FeatherInput>
  
        <div class="spacer"></div>
        <div class="action-container">
          <FeatherButton
            secondary
            @click="handleCancel"
            data-test="cancel-snmp-profile-button"
          >
            Cancel
          </FeatherButton>
          <FeatherButton
            primary
            @click="handleSaveProfile"
            data-test="save-profile-button"
            :disabled="!isValid"
          >
            {{ isCreate ? 'Create Profile' : 'Save Changes' }}
          </FeatherButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { FeatherBackButton } from '@featherds/back-button'
import { FeatherButton } from '@featherds/button'
import { FeatherInput } from '@featherds/input'
import { SnmpProfile, SnmpProfileFormErrors } from '@/types/snmpConfig'
import { validateProfile } from './snmpValidator'
import { useSnmpConfigStore, getDefaultSnmpProfile } from '@/stores/snmpConfigStore'
import useSnackbar from '@/composables/useSnackbar'

const props = defineProps<{
  isCreate: boolean,
  profileId: number
}>()
 
const router = useRouter()
const store = useSnmpConfigStore()
const snackbar = useSnackbar()
const isValid = ref(false)
const errors = ref<SnmpProfileFormErrors>({})

const currentProfile = ref<SnmpProfile>()
const label = ref('')
const filterExpression = ref('')

const resetValues = () => {
  label.value = ''
  filterExpression.value = ''
}

const loadInitialValues = () => {
  if (props.profileId < 0) {
    currentProfile.value = getDefaultSnmpProfile()
  } else {
    // TODO: ensure profileId is in range
    currentProfile.value = store.config.profiles?.profile?.find(p => p.id === props.profileId) ?? getDefaultSnmpProfile()
  }

  label.value = currentProfile.value.label ?? ''
  filterExpression.value = currentProfile.value.filterExpression ?? ''
}

const handleSaveProfile = async () => {
  handleValidate()

  try {
    if (!isValid.value) {
      snackbar.showSnackBar({ msg: 'Invalid values', error: true })
      return
    }

    // TODO: save values to store and then to Rest API
    snackbar.showSnackBar({ msg: props.isCreate ? 'Profile created successfully' : 'Profile updated successfully', error: false })
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
  const currentErrors = validateProfile(
    label.value,
    filterExpression.value
  )

  isValid.value = Object.keys(currentErrors).length === 0
  errors.value = currentErrors as SnmpProfileFormErrors
}

watch([() => props.profileId, () => props.isCreate], () => {
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
