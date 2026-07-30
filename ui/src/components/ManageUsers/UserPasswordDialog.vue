<template>
  <Dialog
    :visible="visible"
    modal
    :header="`Change Password: ${userId}`"
    class="user-password-dialog"
    :style="{ width: '420px', maxWidth: '95vw' }"
    data-test="user-password-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-column">
      <FormField>
        <IftaLabel>
          <Password
            v-model="password"
            inputId="user-password-new"
            :feedback="false"
            toggleMask
            fluid
            data-test="new-password-input"
          />
          <label for="user-password-new">New Password *</label>
        </IftaLabel>
      </FormField>
      <FormField>
        <IftaLabel>
          <Password
            v-model="confirmation"
            inputId="user-password-confirm"
            :feedback="false"
            toggleMask
            fluid
            data-test="confirm-password-input"
          />
          <label for="user-password-confirm">Confirm Password *</label>
        </IftaLabel>
        <small
          v-if="confirmation && password !== confirmation"
          class="mismatch"
          data-test="password-mismatch"
        >Passwords do not match</small>
      </FormField>
    </div>

    <template #footer>
      <Button
        text
        label="Cancel"
        data-test="cancel-button"
        @click="emit('update:visible', false)"
      />
      <Button
        label="Change Password"
        :disabled="!password || password !== confirmation || saving"
        data-test="save-button"
        @click="save"
      />
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import IftaLabel from 'primevue/iftalabel'
import Password from 'primevue/password'

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

watch(
  () => props.visible,
  (isVisible) => {
    if (isVisible) {
      password.value = ''
      confirmation.value = ''
    }
  }
)

const save = async () => {
  saving.value = true
  try {
    const ok = await store.setPassword(props.userId, password.value)
    if (ok) {
      emit('update:visible', false)
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

.mismatch {
  color: var(--p-red-500, #e24c4c);
}
</style>
