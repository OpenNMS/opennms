<template>
  <div class="form-container" id="scv">
    <p class="title">{{ isEditing ? 'Update' : 'Add' }} Credentials</p>
    <FeatherInput
      data-test="alias-input"
      :disabled="isEditing"
      label="Alias"
      @update:modelValue="updateAlias"
      :modelValue="scvStore.credentials.alias"
      :error="aliasError"
      class="alias-input"
    />

    <form autocomplete="off" class="row">
      <FeatherInput
        data-test="username-input"
        autocomplete="new-username"
        label="Username"
        @update:modelValue="updateUsername"
        :modelValue="scvStore.credentials.username"
        class="input"
      />

      <FeatherInput
        data-test="password-input"
        autocomplete="new-password"
        label="Password"
        @update:modelValue="updatePassword"
        :modelValue="scvStore.credentials.password"
        :error="passwordError"
        class="input"
      />
    </form>

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

    <div class="btns">
      <FeatherButton
        v-if="!isEditing"
        data-test="add-creds-btn"
        :disabled="disabled"
        primary 
        @click="addCredentials">
          Add Credentials
      </FeatherButton>

      <FeatherButton
        v-if="isEditing"
        data-test="update-creds-btn"
        :disabled="disabled"
        primary 
        @click="updateCredentials">
          Update Credentials
      </FeatherButton>

      <FeatherButton
        primary 
        data-test="clear-btn"
        @click="clearCredentials">
          Clear Form
      </FeatherButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import { FeatherInput } from '@featherds/input'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Add from '@featherds/icon/action/Add' 
import { useScvStore } from '@/stores/scvStore'
import { SCVCredentials } from '@/types/scv'
import { UpdateModelFunction } from '@/types'
import SCVAttribute from './SCVAttribute.vue'

const scvStore = useScvStore()
const keyError = ref(false)
const dbCredentials = computed<SCVCredentials>(() => scvStore.dbCredentials)
const aliases = computed<string[]>(() => scvStore.aliases)
const isEditing = computed<boolean>(() => scvStore.isEditing)
const disabled = computed<boolean>(() => Boolean(!scvStore.credentials.alias || aliasError.value || passwordError.value || keyError.value))

const isMasked = (password: string) => {
  for (const char of password) {
    if (char !== '*') return false
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

// Error if alias name is not unique.
const aliasError = computed<string | undefined>(() => {
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
@import "@featherds/styles/themes/variables";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";

.form-container {
  @include elevation(1);
  background: var($surface);
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
  }

  .alias-input {
    width: calc(50% - 5px);
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
  }
}
</style>

<style lang="scss">
#scv {
  .feather-input-sub-text {
    min-height: 0.4rem !important;
  }
}
</style>
