<template>
  <OnmsDialog
    :visible="visible"
    modal
    header="Edit Agent Defaults"
    class="wsman-defaults-dialog"
    width="min(900px, 95vw)"
    data-test="wsman-defaults-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-column">
      <div v-if="errorText" class="dialog-error" role="alert" data-test="dialog-error">{{ errorText }}</div>
      <p class="dialog-note">
        These settings apply to every WS-Man agent that no definition matches, and fill in whatever a
        definition leaves unset.
      </p>
      <WsmanSettingsFields
        v-model="form"
        idPrefix="wsman-defaults"
        :errors="errors"
        :hasPassword="config.defaults.hasPassword"
        unsetLabel="Not set (built-in default)"
      />
    </div>

    <template #footer>
      <OnmsButton variant="text" label="Cancel" data-test="cancel-button" @click="emit('update:visible', false)" />
      <OnmsButton label="Save" :disabled="hasErrors || saving" data-test="save-button" @click="save" />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { OnmsButton, OnmsDialog } from '@opennms/onms-ui'

import WsmanSettingsFields from './WsmanSettingsFields.vue'
import { definitionToInput, emptySettingsForm, formToInput, settingsToForm, validateSettingsForm } from './wsmanForm'
import { useWsmanAdminStore } from '@/stores/wsmanAdminStore'
import { WsmanConfig } from '@/types/wsmanAdmin'

const props = defineProps<{
  visible: boolean
  config: WsmanConfig
}>()

const emit = defineEmits(['update:visible'])

const store = useWsmanAdminStore()

const form = ref(emptySettingsForm())
const saving = ref(false)
const errorText = ref('')

const errors = computed(() => validateSettingsForm(form.value))
const hasErrors = computed(() => Object.keys(errors.value).length > 0)

watch(
  () => props.visible,
  (isVisible) => {
    if (isVisible) {
      form.value = settingsToForm(props.config.defaults)
      errorText.value = ''
    }
  }
)

const save = async () => {
  saving.value = true
  try {
    // the definitions ride along unchanged; their index keeps their passwords
    const error = await store.saveConfig({
      defaults: formToInput(form.value),
      definitions: props.config.definitions.map((d, i) => definitionToInput(d, i))
    })
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
}

.dialog-note {
  margin: 0;
  font-size: 0.9rem;
  color: var(--p-text-muted-color);
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
