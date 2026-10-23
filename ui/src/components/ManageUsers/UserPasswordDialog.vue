<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="`Change Password: ${userId}`"
    class="user-password-dialog"
    width="min(420px, 95vw)"
    data-test="user-password-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-column">
      <div
        v-if="errorText"
        class="dialog-error"
        role="alert"
        data-test="dialog-error"
      >{{ errorText }}</div>
      <FormField
        label="New Password"
        for="user-password-new"
        required
      >
        <OnmsPassword
          v-model="password"
          inputId="user-password-new"
          :feedback="false"
          toggleMask
          fluid
          data-test="new-password-input"
        />
      </FormField>
      <FormField
        label="Confirm Password"
        for="user-password-confirm"
        required
        :error="confirmation && password !== confirmation ? 'Passwords do not match' : undefined"
      >
        <OnmsPassword
          v-model="confirmation"
          inputId="user-password-confirm"
          :feedback="false"
          toggleMask
          fluid
          data-test="confirm-password-input"
        />
      </FormField>
    </div>

    <template #footer>
      <OnmsButton
        variant="text"
        label="Cancel"
        data-test="cancel-button"
        @click="emit('update:visible', false)"
      />
      <OnmsButton
        label="Change Password"
        :disabled="!password || password !== confirmation || saving"
        data-test="save-button"
        @click="save"
      />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

import { OnmsButton, OnmsDialog, OnmsPassword } from '@opennms/onms-ui'

import FormField from '@/components/Common/FormField.vue'
import { useUserAdminStore } from '@/stores/userAdminStore'

const props = defineProps<{
  visible: boolean
  userId: string
}>()

const emit = defineEmits(['update:visible'])

const store = useUserAdminStore()

const password = ref('')
const confirmation = ref('')
const saving = ref(false)
const errorText = ref('')

watch(
  () => props.visible,
  (isVisible) => {
    if (isVisible) {
      password.value = ''
      confirmation.value = ''
      errorText.value = ''
    }
  }
)

const save = async () => {
  saving.value = true
  try {
    const error = await store.setPassword(props.userId, password.value)
    if (error === null) {
      emit('update:visible', false)
    } else {
      errorText.value = error
    }
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.form-column {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding-top: 0.5rem;

  :deep(.p-password) {
    width: 100%;
  }
}

.dialog-error {
  padding: 0.5rem 0.75rem;
  border-radius: 4px;
  border-left: 3px solid var(--p-red-500, #ef4444);
  background: color-mix(in srgb, var(--p-red-500, #ef4444) 10%, transparent);
  color: var(--p-red-600, #dc2626);
  font-size: 0.9rem;
}
</style>
