<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="`Rename User: ${userId}`"
    class="user-rename-dialog"
    width="min(420px, 95vw)"
    data-test="user-rename-dialog"
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
        label="New User ID"
        for="user-rename-new-id"
        required
        :error="nameProblem || undefined"
        hint="Group memberships follow the rename."
      >
        <OnmsInputText
          id="user-rename-new-id"
          v-model="newUserId"
          :invalid="!!nameProblem"
          data-test="new-user-id-input"
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
        label="Rename"
        :disabled="!newUserId.trim() || !!nameProblem || newUserId.trim() === userId || saving"
        data-test="save-button"
        @click="save"
      />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import { OnmsButton, OnmsDialog, OnmsInputText } from '@opennms/onms-ui'

import FormField from '@/components/Common/FormField.vue'
import { validateAdminName } from '@/lib/adminValidation'
import { useUserAdminStore } from '@/stores/userAdminStore'

const props = defineProps<{
  visible: boolean
  userId: string
}>()

const emit = defineEmits(['update:visible'])

const store = useUserAdminStore()

const newUserId = ref('')
const saving = ref(false)
const errorText = ref('')

const nameProblem = computed(() => validateAdminName(newUserId.value, 'user-id'))

watch(
  () => props.visible,
  (isVisible) => {
    if (isVisible) {
      newUserId.value = ''
      errorText.value = ''
    }
  }
)

const save = async () => {
  saving.value = true
  try {
    const error = await store.renameUser(props.userId, newUserId.value.trim())
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

  :deep(input) {
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
