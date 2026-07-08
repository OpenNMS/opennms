<template>
  <TableCard
    class="snmpv3-user-management-container"
    data-test="create-snmpv3-user"
    v-if="store.createUserDrawerState.visible"
  >
    <div class="header">
      <div class="section-left">
        <div class="title">
          <PButton
            text
            aria-label="Back"
            data-test="text-button"
            @click="store.closeCreateUserDrawer"
          >
            <FeatherIcon :icon="ChevronLeft" />
          </PButton>
          <h3 v-if="store.createUserDrawerState.mode === CreateEditMode.Create">New SNMPv3 User</h3>
          <h3 v-else-if="store.createUserDrawerState.mode === CreateEditMode.Edit">Edit SNMPv3 User {{ securityName }}</h3>
        </div>
      </div>
    </div>
    <div class="content">
      <div class="username-version-row">
        <div class="left">
          <FormField
            label="Security Name"
            for="security-name"
            :error="error.securityName"
          >
            <PInputText
              id="security-name"
              data-test="security-name-input"
              v-model="securityName"
              :invalid="!!error.securityName"
            />
          </FormField>
        </div>
        <div class="right">
          <FormField
            label="Engine ID"
            for="engine-id"
            :error="error.engineId"
          >
            <PInputText
              id="engine-id"
              data-test="engine-id-input"
              v-model="engineId"
              :invalid="!!error.engineId"
            />
          </FormField>
        </div>
      </div>
      <div class="row">
        <h1>Credential properties</h1>
      </div>
      <div class="properties-row">
        <div class="left">
          <FormField
            label="Security Level"
            for="security-level"
            :error="error.securityLevel"
          >
            <PSelect
              inputId="security-level"
              v-model="securityLevel"
              @update:modelValue="onSecurityLevelChange"
              showClear
              optionLabel="_text"
              :options="SECURITY_LEVEL_OPTIONS"
              :invalid="!!error.securityLevel"
            />
          </FormField>
        </div>
        <div class="right"></div>
      </div>
      <div
        class="row"
        v-if="authProtocolVisible"
      >
        <div class="left">
          <FormField
            label="Auth Protocol"
            for="auth-protocol"
            :error="error.authProtocol"
          >
            <PSelect
              inputId="auth-protocol"
              v-model="authProtocol"
              showClear
              optionLabel="_text"
              :options="AUTH_PROTOCOL_OPTIONS"
              :invalid="!!error.authProtocol"
            />
          </FormField>
        </div>
        <div class="right">
          <FormField
            label="Auth Passphrase"
            for="auth-passphrase"
            :error="error.authPassphrase"
          >
            <PInputText
              id="auth-passphrase"
              type="password"
              data-test="auth-passphrase-input"
              v-model="authPassphrase"
              :invalid="!!error.authPassphrase"
            />
          </FormField>
          <ScvInputIcon
            data-test="auth-passphrase-save-button"
            @click="store.openCredentialDrawer('auth')"
          />
        </div>
      </div>
      <div
        class="row"
        v-if="privacyProtocolVisible"
      >
        <div class="left">
          <FormField
            label="Privacy Protocol"
            for="privacy-protocol"
            :error="error.privacyProtocol"
          >
            <PSelect
              inputId="privacy-protocol"
              v-model="privacyProtocol"
              showClear
              optionLabel="_text"
              :options="PRIVACY_PROTOCOL_OPTIONS"
              :invalid="!!error.privacyProtocol"
            />
          </FormField>
        </div>
        <div class="right">
          <FormField
            label="Privacy Passphrase"
            for="privacy-passphrase"
            :error="error.privacyPassphrase"
          >
            <PInputText
              id="privacy-passphrase"
              type="password"
              data-test="privacy-passphrase-input"
              v-model="privacyPassphrase"
              :invalid="!!error.privacyPassphrase"
            />
          </FormField>
          <ScvInputIcon
            data-test="privacy-passphrase-save-button"
            @click="store.openCredentialDrawer('privacy')"
          />
        </div>
      </div>
    </div>
    <div class="footer">
      <PButton
        text
        data-test="cancel-button"
        label="Cancel"
        @click="store.closeCreateUserDrawer"
      />
      <PButton
        data-test="create-user-button"
        :label="store.createUserDrawerState.mode === CreateEditMode.Create ? 'Create User' : 'Update User'"
        :disabled="isSaveDisabled || isSaving"
        @click="saveUser"
      />
    </div>
    <ScvSearchDrawer
      :isOpen="store.credentialDrawerState.visible"
      @hidden="store.closeCredentialDrawer"
      @itemSelected="scvItemSelected"
    />
  </TableCard>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch, watchEffect } from 'vue'

import useSnackbar from '@/composables/useSnackbar'
import { DEFAULT_SNMP_V3_AUTH_PROTOCOL, DEFAULT_SNMP_V3_PRIVACY_PROTOCOL } from '@/lib/constants'
import { AUTH_PROTOCOL_OPTIONS, MIN_PASSPHRASE_CHARACTERS, PRIVACY_PROTOCOL_OPTIONS, SECURITY_LEVEL_OPTIONS, SecurityLevel, passphraseByteLength } from '@/lib/trapdValidator'
import { mapUserToServer } from '@/mappers/trapdConfig.mapper'
import { updateTrapdConfiguration } from '@/services/trapdConfigurationService'
import { useScvStore } from '@/stores/scvStore'
import { useTrapdConfigStore } from '@/stores/trapdConfigStore'
import { CreateEditMode } from '@/types'
import type { SnmpV3UserError } from '@/types/trapConfig'
import Button from 'primevue/button'
import FormField from '../Common/FormField.vue'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import { FeatherIcon } from '@featherds/icon'
import ChevronLeft from '@featherds/icon/navigation/ChevronLeft'
import { ISelectItemType } from '@featherds/select'
import TableCard from '../Common/TableCard.vue'
import ScvInputIcon from '../SCV/ScvInputIcon.vue'
import ScvSearchDrawer from '../SCV/ScvSearchDrawer.vue'

const PButton = Button
const PInputText = InputText
const PSelect = Select

const store = useTrapdConfigStore()
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
const scvStore = useScvStore()

const authProtocolVisible = computed(() => {
  const selectedSecurityLevel = Number(securityLevel.value?._value)
  return selectedSecurityLevel === SecurityLevel.AuthNoPriv || selectedSecurityLevel === SecurityLevel.AuthPriv
})

const privacyProtocolVisible = computed(() => {
  const selectedSecurityLevel = Number(securityLevel.value?._value)
  return selectedSecurityLevel === SecurityLevel.AuthPriv
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
      const updatedConfig = {
        ...store.trapdConfig,
        snmpv3User: [...(store.trapdConfig.snmpv3User || []), payload]
      }
      await updateTrapdConfiguration(updatedConfig)
    }
    if (store.createUserDrawerState.mode === CreateEditMode.Edit) {
      const selectedUser = store.snmpV3Users?.[store.createUserDrawerState.selectedUserIndex]
      if (!selectedUser) {
        showSnackBar({ msg: 'Unable to determine the selected SNMPv3 user to update.', error: true })
        return
      }
      const updatedUsers = [...(store.trapdConfig.snmpv3User || [])]
      updatedUsers[store.createUserDrawerState.selectedUserIndex] = payload
      const updatedConfig = {
        ...store.trapdConfig,
        snmpv3User: updatedUsers
      }
      await updateTrapdConfiguration(updatedConfig)
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
const scvItemSelected = (item: any) => {
  const scvValue = '${scv:' + item.alias + ':' + item.key + '}'

  if (store.credentialDrawerState.key === 'auth') {
    authPassphrase.value = scvValue
  } else if (store.credentialDrawerState.key === 'privacy') {
    privacyPassphrase.value = scvValue
  }
  store.closeCredentialDrawer()
}

const onSecurityLevelChange = async () => {
  const selectedSecurityLevel = Number(securityLevel.value?._value)
  await nextTick()

  if (selectedSecurityLevel === SecurityLevel.NoAuthNoPriv) {
    authProtocol.value = createEmptySelectItem()
    privacyProtocol.value = createEmptySelectItem()
    authPassphrase.value = ''
    privacyPassphrase.value = ''
  }
  if (selectedSecurityLevel === SecurityLevel.AuthNoPriv) {
    authProtocol.value = AUTH_PROTOCOL_OPTIONS.find(option => option._value === DEFAULT_SNMP_V3_AUTH_PROTOCOL) ?? createEmptySelectItem()
    authPassphrase.value = ''
  }
  if (selectedSecurityLevel === SecurityLevel.AuthPriv) {
    authProtocol.value = AUTH_PROTOCOL_OPTIONS.find(option => option._value === DEFAULT_SNMP_V3_AUTH_PROTOCOL) ?? createEmptySelectItem()
    privacyProtocol.value = PRIVACY_PROTOCOL_OPTIONS.find(option => option._value === DEFAULT_SNMP_V3_PRIVACY_PROTOCOL) ?? createEmptySelectItem()
    authPassphrase.value = ''
    privacyPassphrase.value = ''
  }
}

const validateInputs = () => {
  const newError: SnmpV3UserError = {}
  const levelValue = Number(securityLevel.value?._value)

  if (!securityName.value) {
    newError.securityName = 'Security Name is required'
  }

  // Level 1 (NoAuthNoPriv) must not carry auth or privacy credentials (backend rule)
  if (levelValue === SecurityLevel.NoAuthNoPriv && (authProtocol.value || privacyProtocol.value)) {
    newError.securityLevel = 'Security level 1 does not allow auth or privacy credentials'
  }

  // Level 2 (AuthNoPriv) must not carry privacy credentials (backend rule)
  if (levelValue === SecurityLevel.AuthNoPriv && privacyProtocol.value) {
    newError.privacyProtocol = 'Security level 2 does not allow privacy credentials'
  }

  // authProtocol and authPassphrase must be provided together (backend rule)
  if (authProtocolVisible.value && !authProtocol.value) {
    newError.authProtocol = authPassphrase.value
      ? 'Auth Passphrase requires an Auth Protocol to be selected'
      : 'Auth Protocol is required for selected security level'
  }

  if (authProtocolVisible.value && authProtocol.value && !authPassphrase.value) {
    newError.authPassphrase = 'Auth Passphrase is required for selected auth protocol'
  } else if (authPassphrase.value && passphraseByteLength(authPassphrase.value) < MIN_PASSPHRASE_CHARACTERS) {
    newError.authPassphrase = `Auth Passphrase must be at least ${MIN_PASSPHRASE_CHARACTERS} characters`
  }

  // privacyProtocol and privacyPassphrase must be provided together (backend rule)
  if (privacyProtocolVisible.value && !privacyProtocol.value) {
    newError.privacyProtocol = privacyPassphrase.value
      ? 'Privacy Passphrase requires a Privacy Protocol to be selected'
      : 'Privacy Protocol is required for selected security level'
  }

  if (privacyProtocolVisible.value && privacyProtocol.value && !privacyPassphrase.value) {
    newError.privacyPassphrase = 'Privacy Passphrase is required for selected privacy protocol'
  } else if (privacyPassphrase.value && passphraseByteLength(privacyPassphrase.value) < MIN_PASSPHRASE_CHARACTERS) {
    newError.privacyPassphrase = `Privacy Passphrase must be at least ${MIN_PASSPHRASE_CHARACTERS} characters`
  }
  return newError
}

const loadUserData = async (drawerState: typeof store.createUserDrawerState) => {
  if (drawerState.mode === CreateEditMode.Edit && drawerState.selectedUserIndex > -1) {
    const selectedUser = store.snmpV3Users ? store.snmpV3Users[drawerState.selectedUserIndex] : null

    if (selectedUser) {
      const selectedSecurityLevel = Number(selectedUser.securityLevel)
      securityLevel.value = SECURITY_LEVEL_OPTIONS.find(option => option._value === String(selectedSecurityLevel)) ?? createEmptySelectItem()
      await nextTick()
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
    securityLevel.value = SECURITY_LEVEL_OPTIONS.find(option => option._value === String(SecurityLevel.NoAuthNoPriv)) ?? createEmptySelectItem()
    authProtocol.value = createEmptySelectItem()
    privacyProtocol.value = createEmptySelectItem()
    securityName.value = ''
    engineId.value = ''
    authPassphrase.value = ''
    privacyPassphrase.value = ''
  }
}

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

watchEffect(() => {
  error.value = validateInputs()
  isSaveDisabled.value = Object.keys(error.value).length > 0
})

watch(
  () => store.createUserDrawerState, () => {
    loadUserData(store.createUserDrawerState)
  }, { deep: true, immediate: true }
)

onMounted(() => {
  scvStore.populate()
})
</script>

<style lang="scss" scoped>
@use '@featherds/styles/mixins/typography';

.snmpv3-user-management-container {
  margin-top: 10px;
  padding: 25px;
  border: 1px solid var(--p-content-border-color);

  .header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;

    .section-left {
      .title {
        display: flex;
        align-items: center;
        gap: 10px;

        // enlarge the back-button icon (FeatherIcon scales with font-size)
        :deep(.p-button) {
          font-size: 1.3rem;
        }

        h3 {
          @include typography.headline3;
          color: var(--p-text-color);
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
        color: var(--p-text-color);
      }

      &>div {
        flex: 1;
      }

      .right {
        display: flex;
        align-items: center;
        gap: 10px;

        .form-field {
          flex: 1;
        }
      }
    }

    .properties-row {
      display: flex;
      gap: 20px;
      margin-bottom: 20px;
      width: 50%;

      &>div {
        flex: 1;
      }
    }
  }

  .footer {
    display: flex;
    justify-content: flex-start;
    gap: 10px;
  }
}
</style>
