<template>
  <Dialog
    :visible="visible"
    modal
    :header="`Rename Group: ${groupName}`"
    class="group-rename-dialog"
    :style="{ width: '420px', maxWidth: '95vw' }"
    data-test="group-rename-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-column">
      <Message
        v-if="errorText"
        severity="error"
        :closable="false"
        data-test="dialog-error"
      >{{ errorText }}</Message>
      <FormField>
        <IftaLabel>
          <InputText
            id="group-rename-new-name"
            v-model="newName"
            :invalid="!!nameProblem"
            data-test="new-name-input"
          />
          <label for="group-rename-new-name">New Group Name *</label>
        </IftaLabel>
        <small
          v-if="nameProblem"
          class="field-error"
          data-test="name-error"
        >{{ nameProblem }}</small>
        <small class="hint">On-call roles referencing this group follow the rename.</small>
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
        :disabled="!newName.trim() || !!nameProblem || newName.trim() === groupName || saving"
        data-test="save-button"
        @click="save"
      />
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import IftaLabel from 'primevue/iftalabel'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'

import FormField from '@/components/Common/FormField.vue'
import { validateAdminName } from '@/lib/adminValidation'
import { useGroupAdminStore } from '@/stores/groupAdminStore'

const props = defineProps<{
  visible: boolean
  groupName: string
}>()

const emit = defineEmits(['update:visible'])

const store = useGroupAdminStore()

const newName = ref('')
const saving = ref(false)
const errorText = ref('')

const nameProblem = computed(() => validateAdminName(newName.value, 'group name'))

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
    const error = await store.renameGroup(props.groupName, newName.value.trim())
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
