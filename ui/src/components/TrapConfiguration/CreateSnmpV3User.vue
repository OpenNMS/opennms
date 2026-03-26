<template>
  <TableCard
    class="snmpv3-user-management-container"
    data-test="create-snmpv3-user"
    v-if="store.createUserDrawerState.visible"
  >
    <div class="header">
      <div class="section-left">
        <div class="title">
          <FeatherButton
            icon="Back"
            data-test="text-button"
            @click="store.closeCreateUserDrawer"
          >
            <FeatherIcon :icon="ChevronLeft"> </FeatherIcon>
          </FeatherButton>
          <h3>New SNMPv3 User Management</h3>
        </div>
      </div>
    </div>
    <div class="content">
      <div class="username-version-row">
        <div class="left">
          <FeatherInput
            label="Secuirity Name"
            data-test="security-name-input"
            v-model="securityName"
            :error="error.securityName"
          />
        </div>
        <div class="right">
          <FeatherInput
            label="Engine ID"
            data-test="engine-id-input"
            v-model="engineId"
            :error="error.engineId"
          />
        </div>
      </div>
      <div class="row">
        <h1>Credential properties</h1>
      </div>
      <div class="properties-row">
        <div class="left">
          <FeatherSelect
            label="Security Level"
            v-model="securityLevel"
            :clear="'true'"
            :options="SECURITY_LEVEL_OPTIONS"
            :error="error.securityLevel"
          />
        </div>
        <div class="right"></div>
      </div>
      <div
        class="row"
        v-if="authProtocolVisible"
      >
        <div class="left">
          <FeatherSelect
            label="Auth Protocol"
            v-model="authProtocol"
            :clear="'true'"
            :options="AUTH_PROTOCOL_OPTIONS"
            :error="error.authProtocol"
          />
        </div>
        <div class="right">
          <FeatherInput
            label="Auth Passphrase"
            type="password"
            data-test="auth-passphrase-input"
            v-model="authPassphrase"
            :error="error.authPassphrase"
          />
          <FeatherButton
            icon="Save"
            data-test="auth-passphrase-save-button"
            @click="store.openCredentialDrawer"
          >
            <FeatherIcon :icon="Security"> </FeatherIcon>
          </FeatherButton>
        </div>
      </div>
      <div
        class="row"
        v-if="privacyProtocolVisible"
      >
        <div class="left">
          <FeatherSelect
            label="Privacy Protocol"
            v-model="privacyProtocol"
            :clear="'true'"
            :options="PRIVACY_PROTOCOL_OPTIONS"
            :error="error.privacyProtocol"
          />
        </div>
        <div class="right">
          <FeatherInput
            label="Privacy Passphrase"
            type="password"
            data-test="privacy-passphrase-input"
            v-model="privacyPassphrase"
            :error="error.privacyPassphrase"
          />
          <FeatherButton
            icon="Save"
            data-test="privacy-passphrase-save-button"
            @click="store.openCredentialDrawer"
          >
            <FeatherIcon :icon="Security"> </FeatherIcon>
          </FeatherButton>
        </div>
      </div>
    </div>
    <div class="footer">
      <FeatherButton
        secondary
        data-test="cancel-button"
        @click="store.closeCreateUserDrawer"
      >
        Cancel
      </FeatherButton>
      <FeatherButton
        primary
        data-test="create-user-button"
        @click="saveUser"
        :disabled="isSaveDisabled || isSaving"
      >
        {{ store.createUserDrawerState.mode === CreateEditMode.Create ? 'Create User' : 'Update User' }}
      </FeatherButton>
    </div>
    <SearchExistingCredential />
  </TableCard>
</template>

<script setup lang="ts">
import useSnackbar from '@/composables/useSnackbar'
import { AUTH_PROTOCOL_OPTIONS, PRIVACY_PROTOCOL_OPTIONS, SECURITY_LEVEL_OPTIONS, SecurityLevel } from '@/lib/trapdValidator'
import { mapUserToServer } from '@/mappers/trapdConfig.mapper'
import { saveTrapdUser, updateTrapdUser } from '@/services/trapdConfigurationService'
import { useTrapConfigStore } from '@/stores/trapConfigStore'
import { CreateEditMode } from '@/types'
import type { SnmpV3UserError } from '@/types/trapConfig'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Security from '@featherds/icon/hardware/Security'
import ChevronLeft from '@featherds/icon/navigation/ChevronLeft'
import { FeatherInput } from '@featherds/input'
import { FeatherSelect, ISelectItemType } from '@featherds/select'
import TableCard from '../Common/TableCard.vue'
import SearchExistingCredential from './Drawer/SearchExistingCredential.vue'

const store = useTrapConfigStore()
const { showSnackBar } = useSnackbar()
const createEmptySelectItem = (): ISelectItemType => (undefined as unknown as ISelectItemType)
const securityName = ref<string>('')
const engineId = ref<string>('')
const securityLevel = ref<ISelectItemType>(createEmptySelectItem())
const authProtocol = ref<ISelectItemType>(createEmptySelectItem())
const privacyProtocol = ref<ISelectItemType>(createEmptySelectItem())
const authPassphrase = ref<string>('')
const privacyPassphrase = ref<string>('')
const isSaveDisabled = ref<boolean>(true)
const isSaving = ref<boolean>(false)
const error = ref<SnmpV3UserError>({})

const authProtocolVisible = computed(() => {
  const selectedSecurityLevel = Number(securityLevel.value?._value)
  return selectedSecurityLevel === SecurityLevel.AuthNoPriv || selectedSecurityLevel === SecurityLevel.AuthPriv
})

const privacyProtocolVisible = computed(() => {
  const selectedSecurityLevel = Number(securityLevel.value?._value)
  return selectedSecurityLevel === SecurityLevel.AuthPriv
})

watch(securityLevel, (selectedSecurityLevel) => {
  const levelValue = Number(selectedSecurityLevel?._value)

  if (levelValue !== SecurityLevel.AuthNoPriv && levelValue !== SecurityLevel.AuthPriv) {
    authProtocol.value = createEmptySelectItem()
    authPassphrase.value = ''
  }

  if (levelValue !== SecurityLevel.AuthPriv) {
    authProtocol.value = createEmptySelectItem()
    privacyProtocol.value = createEmptySelectItem()
    authPassphrase.value = ''
    privacyPassphrase.value = ''
  }

  error.value = validateInputs()
  isSaveDisabled.value = Object.keys(error.value).length > 0
})

const saveUser = async () => {
  const validationError = validateInputs()
  if (Object.keys(validationError).length > 0) {
    showSnackBar({ msg: 'Please fix validation errors before saving.', error: true })
    return
  }

  if (isSaving.value) {
    return
  }

  const payload = mapUserToServer({
    securityName: securityName.value,
    engineId: engineId.value,
    securityLevel: Number(securityLevel.value?._value),
    authProtocol: String(authProtocol.value?._value),
    privacyProtocol: String(privacyProtocol.value?._value),
    authPassphrase: authPassphrase.value,
    privacyPassphrase: privacyPassphrase.value
  })

  try {
    isSaving.value = true

    if (store.createUserDrawerState.mode === CreateEditMode.Create) {
      await saveTrapdUser(payload)
    } else if (store.createUserDrawerState.mode === CreateEditMode.Edit) {
      const selectedUser = store.SnmpV3Users?.[store.createUserDrawerState.selectedUserIndex]
      if (!selectedUser?.securityName) {
        throw new Error('Unable to determine the selected SNMPv3 user to update.')
      }

      await updateTrapdUser(selectedUser.securityName, payload)
    }

    await store.fetchTrapConfig()
    store.closeCreateUserDrawer()
    const successMsg = store.createUserDrawerState.mode === CreateEditMode.Create
      ? 'SNMPv3 user created successfully.'
      : 'SNMPv3 user updated successfully.'
    showSnackBar({ msg: successMsg })
  } catch (err) {
    const msg = err instanceof Error ? err.message : 'Failed to save SNMPv3 user.'
    showSnackBar({ msg, error: true })
  } finally {
    isSaving.value = false
  }
}

const validateInputs = () => {
  const newError: SnmpV3UserError = {}

  if (!securityName.value) {
    newError.securityName = 'Security Name is required'
  }

  if (!securityLevel.value) {
    newError.securityLevel = 'Security Level is required'
  }

  if (authProtocolVisible.value && !authProtocol.value) {
    newError.authProtocol = 'Auth Protocol is required for selected security level'
  }

  if (privacyProtocolVisible.value && !privacyProtocol.value) {
    newError.privacyProtocol = 'Privacy Protocol is required for selected security level'
  }

  if (authProtocolVisible.value && authProtocol.value && !authPassphrase.value) {
    newError.authPassphrase = 'Auth Passphrase is required for selected auth protocol'
  }

  if (privacyProtocolVisible.value && privacyProtocol.value && !privacyPassphrase.value) {
    newError.privacyPassphrase = 'Privacy Passphrase is required for selected privacy protocol'
  }
  return newError
}

const loadUserData = (drawerState: typeof store.createUserDrawerState) => {
  if (drawerState.mode === CreateEditMode.Edit && drawerState.selectedUserIndex > -1) {
    const selectedUser = store.SnmpV3Users ? store.SnmpV3Users[drawerState.selectedUserIndex] : null

    if (selectedUser) {
      const selectedSecurityLevel = Number(selectedUser.securityLevel)
      securityLevel.value = SECURITY_LEVEL_OPTIONS.find(option => option._value === String(selectedSecurityLevel)) ?? createEmptySelectItem()
      authProtocol.value = (selectedSecurityLevel === SecurityLevel.AuthNoPriv || selectedSecurityLevel === SecurityLevel.AuthPriv)
        ? AUTH_PROTOCOL_OPTIONS.find(option => option._value === selectedUser.authProtocol) ?? createEmptySelectItem()
        : createEmptySelectItem()
      privacyProtocol.value = selectedSecurityLevel === SecurityLevel.AuthPriv
        ? PRIVACY_PROTOCOL_OPTIONS.find(option => option._value === selectedUser.privacyProtocol) ?? createEmptySelectItem()
        : createEmptySelectItem()
      securityName.value = selectedUser.securityName
      engineId.value = selectedUser.engineId || ''
      authPassphrase.value = selectedUser.authPassphrase || ''
      privacyPassphrase.value = selectedUser.privacyPassphrase || ''
    }
  } else {
    securityLevel.value = createEmptySelectItem()
    authProtocol.value = createEmptySelectItem()
    privacyProtocol.value = createEmptySelectItem()
    securityName.value = ''
    engineId.value = ''
    authPassphrase.value = ''
    privacyPassphrase.value = ''
  }
}
watchEffect(() => {
  error.value = validateInputs()
  isSaveDisabled.value = Object.keys(error.value).length > 0
})

watch(
  () => store.createUserDrawerState, () => {
    loadUserData(store.createUserDrawerState)
  }, { deep: true, immediate: true }
)
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';

.snmpv3-user-management-container {
  margin-top: 10px;
  padding: 25px;
  border: 1px solid var(--feather-border-on-surface);

  .header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;

    .section-left {
      .title {
        display: flex;
        align-items: center;
        gap: 10px;

        h3 {
          @include typography.headline3;
          color: var(--feather-text-primary);
        }
      }
    }
  }

  .content {
    .username-version-row {
      display: flex;
      gap: 20px;
      margin-bottom: 20px;
      width: 50%;

      .left {
        width: 70%;
      }

      .right {
        width: 30%;
      }
    }

    .row {
      display: flex;
      gap: 20px;
      margin-bottom: 20px;
      width: 50%;

      h1 {
        @include typography.headline4;
        color: var(--feather-text-primary);
      }

      div {
        flex: 1;
      }

      .right {
        display: flex;
        align-items: flex-start;
        gap: 10px;
      }
    }

    .properties-row {
      display: flex;
      gap: 20px;
      margin-bottom: 20px;
      width: 50%;

      div {
        flex: 1;
      }
    }
  }

  .footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
}
</style>

