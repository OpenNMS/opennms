<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="`Rename On-Call Role: ${roleName}`"
    class="role-rename-dialog"
    width="min(420px, 95vw)"
    data-test="role-rename-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-column">
      <div
        v-if="errorText"
        class="dialog-error"
        role="alert"
        data-test="dialog-error"
      >{{ errorText }}</div>
      <FormField label="New Role Name" for="role-rename-new-name" required>
        <OnmsInputText
          id="role-rename-new-name"
          v-model="newName"
          :invalid="!!nameProblem"
          fluid
          data-test="new-name-input"
        />
        <small
          v-if="nameProblem"
          class="field-error"
          data-test="name-error"
        >{{ nameProblem }}</small>
        <small class="hint">Destination paths targeting this role by name do not follow the rename; review them afterwards.</small>
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
        :disabled="!newName.trim() || !!nameProblem || newName.trim() === roleName || saving"
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
import { useOnCallRoleAdminStore } from '@/stores/onCallRoleAdminStore'

const props = defineProps<{
  visible: boolean
  roleName: string
}>()

const emit = defineEmits(['update:visible'])

const store = useOnCallRoleAdminStore()

const newName = ref('')
const saving = ref(false)
const errorText = ref('')

const nameProblem = computed(() => validateAdminName(newName.value, 'role name'))

watch(
  () => props.visible,
  (isVisible) => {
    if (isVisible) {
      newName.value = ''
      errorText.value = ''
    }
  }
)

const save = async () => {
  saving.value = true
  try {
    const error = await store.renameRole(props.roleName, newName.value.trim())
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
  border-radius: 6px;
  border: 1px solid var(--p-red-200, #fecaca);
  background: var(--p-red-50, #fef2f2);
  color: var(--p-red-700, #b91c1c);
  font-size: 0.9rem;
}

.field-error {
  display: block;
  margin-top: 0.25rem;
  color: var(--p-red-500, #e24c4c);
}

.hint {
  display: block;
  margin-top: 0.25rem;
  color: var(--p-text-muted-color);
}
</style>
