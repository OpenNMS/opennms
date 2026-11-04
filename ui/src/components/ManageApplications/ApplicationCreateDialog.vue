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
      <OnmsMessage v-if="errorText" severity="error" data-test="dialog-error">{{ errorText }}</OnmsMessage>

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

import { OnmsButton, OnmsDialog, OnmsInputText, OnmsMessage } from '@opennms/onms-ui'

import useActionFeedback from '@/composables/useActionFeedback'
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
const { report } = useActionFeedback()

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
      report(result, `Application '${trimmed}' created.`)
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
</style>
