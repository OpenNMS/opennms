<template>
  <div class="form-container" id="scv">
    <p class="title">{{ isEditing ? 'Update' : 'Add' }} Credentials</p>
    <FormField
      class="alias-input"
      data-test="alias-input"
      label="Alias"
      for="scv-alias"
      :error="aliasError"
      v-slot="{ errorId, invalid }"
    >
      <PInputText
        id="scv-alias"
        :disabled="isEditing"
        :modelValue="scvStore.credentials.alias"
        @update:modelValue="updateAlias"
        :invalid="invalid"
        :aria-describedby="errorId"
      />
    </FormField>

    <form autocomplete="off" class="row">
      <FormField
        class="input"
        data-test="username-input"
        label="Username"
        for="scv-username"
      >
        <PInputText
          id="scv-username"
          autocomplete="new-username"
          :modelValue="scvStore.credentials.username"
          @update:modelValue="updateUsername"
        />
      </FormField>

      <FormField
        class="input"
        data-test="password-input"
        label="Password"
        for="scv-password"
        :error="passwordError"
        v-slot="{ errorId, invalid }"
      >
        <PInputText
          id="scv-password"
          autocomplete="new-password"
          :modelValue="scvStore.credentials.password"
          @update:modelValue="updatePassword"
          :invalid="invalid"
          :aria-describedby="errorId"
        />
      </FormField>
    </form>

    <div class="large-spacer"></div>
    <div class="add-btn" @click="addAttribute" data-test="add-attr-btn">
      <FeatherIcon :icon="Add" aria-hidden="true" focusable="false" />
      Add attribute
    </div>

    <SCVAttribute
      v-for="(value, key, index) in scvStore.credentials.attributes"
      :key="key" :attributeKey="key"
      :attributeValue="value"
      :attributeIndex="index"
      @set-key-error="setKeyError"
    />

    <div class="large-spacer"></div>
    <div class="btns">
      <PButton
        v-if="!isEditing"
        data-test="add-creds-btn"
        :disabled="disabled"
        label="Add Credentials"
        @click="addCredentials"
      />

      <PButton
        v-if="isEditing"
        data-test="update-creds-btn"
        :disabled="disabled"
        label="Update Credentials"
        @click="updateCredentials"
      />

      <PButton
        data-test="clear-btn"
        label="Clear Form"
        @click="clearCredentials"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import { FeatherIcon } from '@featherds/icon'
import Add from '@featherds/icon/action/Add'
import { SCV_GET_ALL_ALIAS } from '@/lib/constants'
import { useScvStore } from '@/stores/scvStore'
import { SCVCredentials } from '@/types/scv'
import { UpdateModelFunction } from '@/types'
import FormField from '@/components/Common/FormField.vue'
import SCVAttribute from './SCVAttribute.vue'

const PInputText = InputText
const PButton = Button

const scvStore = useScvStore()
const keyError = ref(false)
const dbCredentials = computed<SCVCredentials>(() => scvStore.dbCredentials)
const aliases = computed<string[]>(() => scvStore.aliases)
const isEditing = computed<boolean>(() => scvStore.isEditing)
const disabled = computed<boolean>(() => Boolean(!scvStore.credentials.alias || aliasError.value || passwordError.value || keyError.value))

const isMasked = (password: string) => {
  for (const char of password) {
    if (char !== '*') {
      return false
    }
  }

  return true
}

// if the username has changed and the password is masked
// warn the user that the password must also be updated
const passwordError = computed<string | undefined>(() => {
  if (
    dbCredentials.value.username && scvStore.credentials.password &&
    scvStore.credentials.username !== dbCredentials.value.username &&
    isMasked(scvStore.credentials.password)) {

    return 'Password cannot be masked with updated usernames.'
  }
  return undefined
})

// Error if alias name is not unique or it is reserved
const aliasError = computed<string | undefined>(() => {
  if (
    !isEditing.value &&
    scvStore.credentials.alias?.toLowerCase() === SCV_GET_ALL_ALIAS) {
    return 'Cannot use reserved alias name.'
  }

  if (
    !isEditing.value &&
    scvStore.credentials.alias &&
    aliases.value.includes(scvStore.credentials.alias.toLowerCase())) {
    return 'Alias already in use.'
  }
  return undefined
})

const setKeyError = (val: boolean) => keyError.value = val

const updateAlias: UpdateModelFunction = (val: string) => {
  scvStore.setValue({ alias: val.toLowerCase() })
}

const updateUsername: UpdateModelFunction = (val: string) => scvStore.setValue({ username: val })
const updatePassword: UpdateModelFunction = (val: string) => scvStore.setValue({ password: val })
const addCredentials = () => scvStore.addCredentials()
const updateCredentials = () => scvStore.updateCredentials()
const clearCredentials = () => scvStore.clearCredentials()
const addAttribute = () => scvStore.addAttribute()
</script>

<style lang="scss" scoped>
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";

.form-container {
  @include elevation(1);
  background: var(--p-content-background);
  height: calc(100vh - 149px);
  display: flex;
  flex-direction: column;
  padding: 0px 15px 15px 15px;
  overflow-y: auto;

  .title {
    @include headline3;
    margin-top: 11px;
    margin-bottom: 9px;
  }

  .row {
    display: flex;
    flex-direction: row;
    gap: 10px;
    // vertical spacing above the field row
    margin-top: 2rem;
  }

  .alias-input {
    width: calc(50% - 5px);
    // vertical spacing above the field
    margin-top: 2rem;
  }
  .input {
    width: 50%;
  }

  .add-btn {
    cursor: pointer;
    @include body-small;
    margin-bottom: 10px;
  }

  .btns {
    display: flex;
    flex-direction: row;
    gap: 0.5rem;
  }

  .large-spacer {
    min-height: 1em;
  }
}
</style>
