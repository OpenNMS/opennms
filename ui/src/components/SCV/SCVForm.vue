<template>
  <div class="form-container" id="scv">
    <p class="title">Add Alias</p>
    <FeatherInput
      :disabled="isEditing"
      label="Alias"
      @update:modelValue="updateAlias"
      :modelValue="credentials.alias"
      :error="aliasError"
      class="input"
    />

    <form autocomplete="off" class="row">
      <FeatherInput
        autocomplete="new-username"
        label="Username"
        @update:modelValue="updateUsername"
        :modelValue="credentials.username"
        class="input"
      />

      <FeatherInput
        autocomplete="new-password"
        label="Password"
        @update:modelValue="updatePassword"
        :modelValue="credentials.password"
        :error="passwordError"
        class="input"
      />
    </form>

    <div class="add-btn" @click="addAttribute">
      <FeatherIcon :icon="Add" aria-hidden="true" focusable="false" />
      Add attribute
    </div>

    <SCVAttribute
      v-for="(value, key, index) in credentials.attributes" 
      :key="key" :attributeKey="key" 
      :attributeValue="value" 
      :attributeIndex="index"
      @set-key-error="setKeyError"
    />

    <div class="btns">
      <FeatherButton
        v-if="!isEditing"
        :disabled="disabled"
        primary 
        @click="addCredentials">
          Add Alias
      </FeatherButton>

      <FeatherButton 
        v-if="isEditing"
        :disabled="disabled"
        primary 
        @click="updateCredentials">
          Update Alias
      </FeatherButton>

      <FeatherButton
        primary 
        @click="clearCredentials">
          Clear Form
      </FeatherButton>
    </div>
  </div>
</template>

<script setup lang=ts>
import { useStore } from 'vuex'
import { FeatherInput } from '@featherds/input'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Add from '@featherds/icon/action/Add' 
import { SCVCredentials } from '@/types/scv'
import SCVAttribute from './SCVAttribute.vue'

const store = useStore()
const keyError = ref(false)
const credentials = computed<SCVCredentials>(() => store.state.scvModule.credentials)
const dbCredentials = computed<SCVCredentials>(() => store.state.scvModule.dbCredentials)
const aliases = computed<string[]>(() => store.state.scvModule.aliases)
const isEditing = computed<boolean>(() => store.state.scvModule.isEditing)
const disabled = computed<boolean>(() => Boolean(!credentials.value.alias || aliasError.value || passwordError.value || keyError.value))

const isMasked = (password: string) => {
  for (const char of password) {
    if (char !== '*') return false
  }
  return true
}

// if the username has changed and the password is masked
// warn the user that the password must also be updated
const passwordError = computed<string | null>(() => {
  if (
    dbCredentials.value.username && credentials.value.password &&
    credentials.value.username !== dbCredentials.value.username && 
    isMasked(credentials.value.password)) {

    return 'Password cannot be masked with updated usernames.'  
  }
  return null
})

// Error if alias name is not unique.
const aliasError = computed<string | null>(() => {
  if (
    !isEditing.value && 
    credentials.value.alias && 
    aliases.value.includes(credentials.value.alias.toLowerCase())) {
    return 'Alias already in use.'
  }
  return null
})

const setKeyError = (val: boolean) => keyError.value = val
const updateAlias = (val: string) => store.dispatch('scvModule/setValue', { alias: val }) 
const updateUsername = (val: string) => store.dispatch('scvModule/setValue', { username: val })
const updatePassword = (val: string) => store.dispatch('scvModule/setValue', { password: val }) 
const addCredentials = () => store.dispatch('scvModule/addCredentials')
const updateCredentials = () => store.dispatch('scvModule/updateCredentials')
const clearCredentials = () => store.dispatch('scvModule/clearCredentials')
const addAttribute = () => store.dispatch('scvModule/addAttribute')
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
  }

  .row {
    display: flex;
    flex-direction: row;
    gap: 10px;
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
