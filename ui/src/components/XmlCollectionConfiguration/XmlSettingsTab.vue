<template>
  <TableCard class="xml-settings-card">
    <div class="settings">
      <FormField
        label="RRD repository"
        for="xml-rrd-repository"
        required
        :error="error"
        :info="XML_FIELD_HINTS.rrdRepository"
      >
        <OnmsInputText
          inputId="xml-rrd-repository"
          data-test="xml-rrd-repository"
          :modelValue="draft"
          :invalid="!!error"
          fluid
          @update:modelValue="draft = String($event ?? '')"
        />
      </FormField>
      <div class="actions">
        <OnmsButton
          variant="outlined"
          label="Reset"
          :disabled="!isDirty || isSaving"
          data-test="xml-settings-reset"
          @click="draft = store.rrdRepository"
        />
        <OnmsButton
          label="Save"
          :disabled="!isDirty || !!error || isSaving"
          data-test="xml-settings-save"
          @click="onSave"
        />
      </div>
    </div>
  </TableCard>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import FormField from '@/components/Common/FormField.vue'
import TableCard from '@/components/Common/TableCard.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { XML_FIELD_HINTS } from '@/lib/xmlCollectionHelpText'
import { useXmlCollectionConfigStore } from '@/stores/xmlCollectionConfigStore'
import { OnmsButton, OnmsInputText } from '@opennms/onms-ui'

const store = useXmlCollectionConfigStore()
const { report } = useActionFeedback()

const draft = ref(store.rrdRepository)
const isSaving = ref(false)

watch(() => store.rrdRepository, (value) => {
  draft.value = value
})

const isDirty = computed(() => draft.value !== store.rrdRepository)
const error = computed(() => (draft.value.trim() ? undefined : 'The RRD repository is required.'))

const onSave = async () => {
  isSaving.value = true
  const result = await store.saveSettings(draft.value.trim())
  isSaving.value = false

  report(result, 'XML collector settings saved.')
}
</script>

<style lang="scss" scoped>
.xml-settings-card {
  padding: 25px;
  border: 1px solid var(--p-content-border-color);
  border-radius: 5px;
}

.settings {
  display: flex;
  flex-direction: column;
  gap: 1em;
  max-width: 40em;

  .actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
}
</style>
