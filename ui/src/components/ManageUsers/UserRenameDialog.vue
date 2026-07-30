<template>
  <Dialog
    :visible="visible"
    modal
    :header="`Rename User: ${userId}`"
    class="user-rename-dialog"
    :style="{ width: '420px', maxWidth: '95vw' }"
    data-test="user-rename-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-column">
      <FormField>
        <IftaLabel>
          <InputText
            id="user-rename-new-id"
            v-model="newUserId"
            data-test="new-user-id-input"
          />
          <label for="user-rename-new-id">New User ID *</label>
        </IftaLabel>
        <small class="hint">Group memberships follow the rename.</small>
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
        :disabled="!newUserId.trim() || newUserId.trim() === userId || saving"
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
import { useUserAdminStore } from '@/stores/userAdminStore'

const props = defineProps<{
  visible: boolean
  userId: string
}>()

const emit = defineEmits(['update:visible'])

const store = useUserAdminStore()

const newUserId = ref('')
const saving = ref(false)

watch(
  () => props.visible,
  (isVisible) => {
    if (isVisible) {
      newUserId.value = ''
    }
  }
)

const save = async () => {
  saving.value = true
  try {
    const ok = await store.renameUser(props.userId, newUserId.value.trim())
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
