<template>
  <Dialog
    :visible="visible"
    modal
    :header="`Rename On-Call Role: ${roleName}`"
    class="role-rename-dialog"
    :style="{ width: '420px', maxWidth: '95vw' }"
    data-test="role-rename-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-column">
      <FormField>
        <IftaLabel>
          <InputText
            id="role-rename-new-name"
            v-model="newName"
            data-test="new-name-input"
          />
          <label for="role-rename-new-name">New Role Name *</label>
        </IftaLabel>
        <small class="hint">Destination paths targeting this role by name do not follow the rename; review them afterwards.</small>
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
        label="Rename"
        :disabled="!newName.trim() || newName.trim() === roleName || saving"
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
import InputText from 'primevue/inputtext'

import FormField from '@/components/Common/FormField.vue'
import { useOnCallRoleAdminStore } from '@/stores/onCallRoleAdminStore'

const props = defineProps<{
  visible: boolean
  roleName: string
}>()

const emit = defineEmits(['update:visible'])

const store = useOnCallRoleAdminStore()

const newName = ref('')
const saving = ref(false)

watch(
  () => props.visible,
  (isVisible) => {
    if (isVisible) {
      newName.value = ''
    }
  }
)

const save = async () => {
  saving.value = true
  try {
    const ok = await store.renameRole(props.roleName, newName.value.trim())
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

  :deep(input) {
    width: 100%;
  }
}

.hint {
  display: block;
  margin-top: 0.25rem;
  color: var(--p-text-muted-color);
}
</style>
