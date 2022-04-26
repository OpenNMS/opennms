<template>
  <div class="form-container">
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

      <FeatherProtectedInput
        autocomplete="new-password"
        label="Password"
        @update:modelValue="updatePassword"
        :modelValue="credentials.password"
        class="input"
      />
    </form>

    <FeatherButton text class="add-attributes-btn" @click="addAttribute">
      <template v-slot:icon>
        <FeatherIcon :icon="Add" aria-hidden="true" focusable="false" />
        Add attribute
      </template>
    </FeatherButton>

    <SCVAttribute v-for="(value, key, index) in credentials.attributes" :key="key" :attributeKey="key" :attributeValue="value" :attributeIndex="index" />

    <div class="btns">
      <FeatherButton
        v-if="!isEditing"
        :disabled="!credentials.alias || Boolean(aliasError)"
        primary 
        @click="addCredentials">
          Add
      </FeatherButton>

      <FeatherButton 
        v-if="isEditing"
        primary 
        @click="updateCredentials">
          Update
      </FeatherButton>

      <FeatherButton
        primary 
        @click="clearCredentials">
          Clear
      </FeatherButton>
    </div>
  </div>
</template>

<script setup lang=ts>
import { useStore } from 'vuex'
import { FeatherInput } from '@featherds/input'
import { FeatherProtectedInput } from '@featherds/protected-input'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Add from '@featherds/icon/action/Add' 
import { SCVCredentials } from '@/types/scv'
import SCVAttribute from './SCVAttribute.vue'

const store = useStore()
const credentials = computed<SCVCredentials>(() => store.state.scvModule.credentials)
const aliases = computed<string[]>(() => store.state.scvModule.aliases)
const isEditing = computed<boolean>(() => store.state.scvModule.isEditing)

// Alias must be unique.
const aliasError = computed<string | null>(() => {
  if (!isEditing.value && credentials.value.alias) {
    if (aliases.value.includes(credentials.value.alias.toLowerCase())) {
      return 'Alias already in use.'
    }
  }
  return null
})

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

.form-container {
  display: flex;
  flex-direction: column;
  border: 2px solid var($primary);
  border-radius: 10px;
  padding: 15px;

  .row {
    display: flex;
    flex-direction: row;
    gap: 10px;
  }
  .input {
    width: 50%;
  }

  .add-attributes-btn {
    justify-content: center;
  }

  .btns {
    margin-top: 40px;
    display: flex;
    flex-direction: row;
  }
}

</style>
