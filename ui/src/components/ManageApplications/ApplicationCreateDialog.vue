<template>
  <OnmsDialog
    :visible="visible"
    modal
    header="Add Application"
    class="application-create-dialog"
    width="min(520px, 95vw)"
    data-test="application-create-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-column">
      <div v-if="errorText" class="dialog-error" role="alert" data-test="dialog-error">{{ errorText }}</div>

      <FormField
        label="Name"
        for="application-name"
        required
        :error="nameProblem || undefined"
        :hint="`Up to ${MAX_NAME_LENGTH} characters. It cannot be changed later.`"
      >
        <template #label-suffix>
          <HelpBadge
            content="The name identifies the application on the status pages and in the events OpenNMS sends about it. Services and perspective locations are chosen in the next step."
            ariaLabel="Application name help"
          />
        </template>
        <OnmsInputText
          id="application-name"
          v-model="name"
          :invalid="!!nameProblem"
          :maxlength="MAX_NAME_LENGTH"
          fluid
          autofocus
          data-test="application-name-input"
          @keydown.enter="isValid && !saving && save()"
        />
      </FormField>
    </div>

    <template #footer>
      <OnmsButton variant="ghost" label="Cancel" data-test="cancel-button" @click="emit('update:visible', false)" />
      <OnmsButton
        label="Add Application"
        :disabled="!isValid || saving"
        :loading="saving"
        data-test="save-button"
        @click="save"
      />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import { OnmsButton, OnmsDialog, OnmsInputText, useOnmsToast } from '@opennms/onms-ui'

import FormField from '@/components/Common/FormField.vue'
import HelpBadge from '@/components/Common/HelpBadge.vue'
import { useApplicationAdminStore } from '@/stores/applicationAdminStore'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  created: [name: string, id: number | undefined]
}>()

// applications.name is varchar(32)
const MAX_NAME_LENGTH = 32

const store = useApplicationAdminStore()
const { showToast } = useOnmsToast()

const name = ref('')
const saving = ref(false)
const errorText = ref('')

const nameProblem = computed(() => {
  const trimmed = name.value.trim()
  if (!trimmed) {
    return null
  }
  if (trimmed.length > MAX_NAME_LENGTH) {
    return `The name cannot be longer than ${MAX_NAME_LENGTH} characters.`
  }
  if (store.applications.some(application => application.name === trimmed)) {
    return `An application named '${trimmed}' already exists.`
  }
  return null
})

const isValid = computed(() => !!name.value.trim() && !nameProblem.value)

watch(() => props.visible, (isVisible) => {
  if (isVisible) {
    name.value = ''
    errorText.value = ''
  }
})

const save = async () => {
  const trimmed = name.value.trim()
  saving.value = true
  try {
    const result = await store.createApplication(trimmed)
    if (result.success) {
      showToast({ message: `Application '${trimmed}' created.`, severity: 'success' })
      emit('update:visible', false)
      emit('created', trimmed, result.payload)
    } else {
      errorText.value = result.message
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
}

.dialog-error {
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  border: 1px solid var(--p-red-200, #fecaca);
  background: var(--p-red-50, #fef2f2);
  color: var(--p-red-700, #b91c1c);
  font-size: 0.9rem;
}
</style>
