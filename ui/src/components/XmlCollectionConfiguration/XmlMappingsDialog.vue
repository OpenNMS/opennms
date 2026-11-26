<template>
  <OnmsDialog
    :visible="visible"
    :header="`Value mappings of '${objectName || 'new object'}'`"
    :closable="false"
    width="36rem"
    data-test="xml-mappings-dialog"
  >
    <p class="hint">{{ XML_FIELD_HINTS.mappings }}</p>

    <div class="rows">
      <div
        v-for="(mapping, index) in draft"
        :key="index"
        class="row"
      >
        <FormField label="From" :for="`xml-mapping-from-${index}`">
          <OnmsInputText
            :modelValue="mapping.from ?? ''"
            :inputId="`xml-mapping-from-${index}`"
            :data-test="`xml-mapping-from-${index}`"
            placeholder="any other value"
            fluid
            @update:modelValue="draft[index].from = String($event ?? '')"
          />
        </FormField>
        <FormField label="To" :for="`xml-mapping-to-${index}`" required>
          <OnmsInputText
            :modelValue="mapping.to"
            :inputId="`xml-mapping-to-${index}`"
            :data-test="`xml-mapping-to-${index}`"
            fluid
            @update:modelValue="draft[index].to = String($event ?? '')"
          />
        </FormField>
        <OnmsIconButton
          :icon="DeleteIcon"
          severity="danger"
          tooltip="Remove mapping"
          aria-label="Remove mapping"
          :data-test="`xml-mapping-remove-${index}`"
          @click="draft.splice(index, 1)"
        />
      </div>
    </div>

    <p v-if="!draft.length" class="empty">No value mappings; collected values are stored as they are.</p>
    <small v-if="error" class="field-error" role="alert" data-test="xml-mappings-error">{{ error }}</small>

    <OnmsButton variant="text" data-test="xml-mapping-add" @click="draft.push({ from: '', to: '' })">Add mapping</OnmsButton>

    <template #footer>
      <OnmsButton variant="outlined" label="Cancel" data-test="xml-mappings-cancel" @click="emit('cancel')" />
      <OnmsButton label="Apply" :disabled="!!error" data-test="xml-mappings-apply" @click="onApply" />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import FormField from '@/components/Common/FormField.vue'
import { XML_FIELD_HINTS } from '@/lib/xmlCollectionHelpText'
import { validateObject } from '@/lib/xmlCollectionValidator'
import type { XmlMapping } from '@/types/xmlCollectionConfig'
import { OnmsButton, OnmsDialog, OnmsIconButton, OnmsInputText } from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'

const props = defineProps<{
  visible: boolean
  objectName: string
  mappings: XmlMapping[]
}>()

const emit = defineEmits<{
  apply: [mappings: XmlMapping[]]
  cancel: []
}>()

// Edited on a copy, so Cancel discards. The parent mounts the dialog per edit, so copying once is enough.
const draft = ref<XmlMapping[]>(props.mappings.map(mapping => ({ from: mapping.from ?? '', to: mapping.to })))

const error = computed(() => validateObject({ name: 'x', type: 'gauge', xpath: 'x', mappings: draft.value }).mappings)

const onApply = () => {
  // A blank "from" is the default mapping; the server stores it without the attribute.
  emit('apply', draft.value.map(mapping => ({ from: mapping.from?.trim() ? mapping.from : null, to: mapping.to })))
}
</script>

<style lang="scss" scoped>
.hint {
  margin-top: 0;
  color: var(--p-text-muted-color);
}

.rows {
  display: flex;
  flex-direction: column;
  gap: 0.5em;
  margin-bottom: 0.5em;
}

.row {
  display: grid;
  grid-template-columns: 1fr 1fr auto;
  align-items: end;
  gap: 0.5em;
}

.empty {
  color: var(--p-text-muted-color);
}

.field-error {
  display: block;
  font-size: 0.875rem;
  color: var(--p-red-500);
}
</style>
