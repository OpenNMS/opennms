<template>
  <TableCard class="http-settings-card">
    <h3>Settings</h3>
    <p class="description">These settings apply to every HTTP collection.</p>
    <FormField
      label="RRD repository"
      for="http-settings-rrd-repository"
      required
      :error="errors.rrdRepository"
      hint="Directory the HTTP collector writes its RRD files to."
    >
      <OnmsInputText
        inputId="http-settings-rrd-repository"
        data-test="http-settings-rrd-repository"
        :modelValue="draft.rrdRepository"
        :invalid="!!errors.rrdRepository"
        :disabled="!store.settings"
        fluid
        @update:modelValue="draft.rrdRepository = String($event ?? '')"
      />
    </FormField>
    <div class="actions">
      <OnmsButton
        variant="outlined"
        label="Reset"
        data-test="http-settings-reset"
        :disabled="!isDirty || isSaving"
        @click="reset"
      />
      <OnmsButton
        label="Save Changes"
        data-test="http-settings-save"
        :disabled="!store.settings || !isDirty || hasErrors(errors) || isSaving"
        @click="onSave"
      />
    </div>
  </TableCard>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import FormField from '@/components/Common/FormField.vue'
import TableCard from '@/components/Common/TableCard.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { hasErrors, validateSettings } from '@/lib/httpCollectionValidator'
import { useHttpCollectionStore } from '@/stores/httpCollectionStore'
import type { HttpCollectionSettings } from '@/types/httpCollectionConfig'
import { OnmsButton, OnmsInputText } from '@opennms/onms-ui'

const store = useHttpCollectionStore()
const { report } = useActionFeedback()

const draft = ref<HttpCollectionSettings>({ rrdRepository: '' })
const isSaving = ref(false)

const reset = () => {
  draft.value = { ...(store.settings ?? { rrdRepository: '' }) }
}

watch(() => store.settings, reset, { immediate: true })

const errors = computed(() => validateSettings(draft.value))
const isDirty = computed(() => (store.settings?.rrdRepository ?? '') !== draft.value.rrdRepository)

const onSave = async () => {
  isSaving.value = true
  const result = await store.saveSettings({ ...draft.value, rrdRepository: draft.value.rrdRepository.trim() })
  isSaving.value = false

  report(result, 'HTTP collector settings saved.')
}
</script>

<style lang="scss" scoped>
.http-settings-card {
  padding: 25px;
  border: 1px solid var(--p-content-border-color);
  border-radius: 5px;
  max-width: 50em;

  h3 {
    margin: 0;
  }

  .description {
    margin: 0.25em 0 1em 0;
    color: var(--p-text-muted-color);
  }

  .actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 1em;
  }
}
</style>
