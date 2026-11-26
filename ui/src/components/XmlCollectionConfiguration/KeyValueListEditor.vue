<template>
  <div class="key-value-list-editor">
    <div class="rows">
      <div
        v-for="(entry, index) in modelValue"
        :key="index"
        class="row"
      >
        <FormField :label="keyLabel" :for="`${idPrefix}-key-${index}`">
          <OnmsInputText
            :modelValue="entry.key"
            :inputId="`${idPrefix}-key-${index}`"
            :data-test="`${idPrefix}-key-${index}`"
            fluid
            @update:modelValue="onUpdate(index, 'key', $event)"
          />
        </FormField>
        <FormField label="Value" :for="`${idPrefix}-value-${index}`">
          <OnmsInputText
            :modelValue="entry.value"
            :inputId="`${idPrefix}-value-${index}`"
            :data-test="`${idPrefix}-value-${index}`"
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
    </div>

    <p v-if="!modelValue.length" class="empty">No {{ itemLabel }}s configured.</p>
    <small v-if="error" class="field-error" role="alert">{{ error }}</small>

    <OnmsButton variant="text" :data-test="`${idPrefix}-add`" @click="onAdd">Add {{ itemLabel }}</OnmsButton>
  </div>
</template>

<script setup lang="ts">
import FormField from '@/components/Common/FormField.vue'
import type { XmlKeyValue } from '@/types/xmlCollectionConfig'
import { OnmsButton, OnmsIconButton, OnmsInputText } from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'

const props = withDefaults(defineProps<{
  modelValue: XmlKeyValue[]
  idPrefix: string
  itemLabel: string
  keyLabel?: string
  error?: string
}>(), {
  keyLabel: 'Name',
  error: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: XmlKeyValue[]]
}>()

const onUpdate = (index: number, field: 'key' | 'value', value: unknown) => {
  emit('update:modelValue', props.modelValue.map((entry, i) => (i === index ? { ...entry, [field]: String(value ?? '') } : entry)))
}

const onAdd = () => {
  emit('update:modelValue', [...props.modelValue, { key: '', value: '' }])
}

const onRemove = (index: number) => {
  emit('update:modelValue', props.modelValue.filter((_, i) => i !== index))
}
</script>

<style lang="scss" scoped>
.key-value-list-editor {
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
    margin: 0 0 0.5em 0;
  }

  .field-error {
    display: block;
    font-size: 0.875rem;
    color: var(--p-red-500);
  }
}
</style>
