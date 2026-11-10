<template>
  <div class="key-value-list-editor">
    <div
      v-for="(entry, index) in modelValue"
      :key="index"
      class="row"
    >
      <FormField :label="keyLabel" :for="`${idPrefix}-key-${index}`">
        <OnmsInputText
          :modelValue="entry[keyField]"
          :inputId="`${idPrefix}-key-${index}`"
          :data-test="`${idPrefix}-key-${index}`"
          :invalid="!entry[keyField]?.trim()"
          fluid
          @update:modelValue="onUpdate(index, keyField, $event)"
        />
      </FormField>
      <FormField label="Value" :for="`${idPrefix}-value-${index}`">
        <OnmsInputText
          :modelValue="entry.value"
          :inputId="`${idPrefix}-value-${index}`"
          :data-test="`${idPrefix}-value-${index}`"
          :invalid="!entry.value?.trim()"
          fluid
          @update:modelValue="onUpdate(index, 'value', $event)"
        />
      </FormField>
      <OnmsIconButton
        :icon="DeleteIcon"
        severity="danger"
        :tooltip="`Remove ${itemLabel}`"
        :aria-label="`Remove ${itemLabel}`"
        :data-test="`${idPrefix}-remove-${index}`"
        @click="onRemove(index)"
      />
    </div>

    <p v-if="!modelValue.length" class="empty">No {{ itemLabel }}s configured.</p>

    <OnmsButton variant="text" :data-test="`${idPrefix}-add`" @click="onAdd">Add {{ itemLabel }}</OnmsButton>
  </div>
</template>

<script setup lang="ts" generic="T extends { value: string } & Record<K, string>, K extends string">
import FormField from '@/components/Common/FormField.vue'
import { OnmsButton, OnmsIconButton, OnmsInputText } from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'

// Edits a list of name/value pairs: URL parameters (key) and request headers (name).
const props = defineProps<{
  modelValue: T[]
  keyField: K
  keyLabel: string
  itemLabel: string
  idPrefix: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: T[]]
}>()

const onUpdate = (index: number, field: string, value: unknown) => {
  const next = props.modelValue.map((entry, i) => (i === index ? { ...entry, [field]: String(value ?? '') } : entry))
  emit('update:modelValue', next)
}

const onAdd = () => {
  emit('update:modelValue', [...props.modelValue, { [props.keyField]: '', value: '' } as T])
}

const onRemove = (index: number) => {
  emit('update:modelValue', props.modelValue.filter((_, i) => i !== index))
}
</script>

<style lang="scss" scoped>
.key-value-list-editor {
  .row {
    display: grid;
    grid-template-columns: 1fr 2fr auto;
    gap: 0.75em;
    align-items: end;
    margin-bottom: 0.5em;
  }

  .empty {
    margin: 0.25em 0;
    color: var(--p-text-muted-color);
  }
}
</style>
