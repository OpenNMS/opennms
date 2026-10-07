<template>
  <div class="address-range-editor">
    <div class="rows">
      <div
        v-for="(range, index) in modelValue"
        :key="index"
        class="row"
      >
        <FormField label="Begin" :for="`${idPrefix}-begin-${index}`" :error="errorAt(index)">
          <OnmsInputText
            :modelValue="range.begin"
            :inputId="`${idPrefix}-begin-${index}`"
            :data-test="`${idPrefix}-begin-${index}`"
            fluid
            @update:modelValue="onUpdate(index, 'begin', $event)"
          />
        </FormField>
        <FormField label="End" :for="`${idPrefix}-end-${index}`">
          <OnmsInputText
            :modelValue="range.end"
            :inputId="`${idPrefix}-end-${index}`"
            :data-test="`${idPrefix}-end-${index}`"
            fluid
            @update:modelValue="onUpdate(index, 'end', $event)"
          />
        </FormField>
        <OnmsIconButton
          :icon="DeleteIcon"
          severity="danger"
          tooltip="Remove range"
          aria-label="Remove range"
          :data-test="`${idPrefix}-remove-${index}`"
          @click="onRemove(index)"
        />
      </div>
    </div>

    <p v-if="!modelValue.length" class="empty">No ranges configured.</p>

    <OnmsButton variant="text" :data-test="`${idPrefix}-add`" @click="onAdd">Add range</OnmsButton>
  </div>
</template>

<script setup lang="ts">
import FormField from '@/components/Common/FormField.vue'
import { validateAddressRange } from '@/lib/thresholdValidator'
import type { ThreshdAddressRange } from '@/types/thresholdConfig'
import { OnmsButton, OnmsIconButton, OnmsInputText } from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'

const props = defineProps<{
  modelValue: ThreshdAddressRange[]
  idPrefix: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: ThreshdAddressRange[]]
}>()

const errorAt = (index: number) => {
  const range = props.modelValue[index]
  return validateAddressRange(range.begin, range.end)
}

const onUpdate = (index: number, field: 'begin' | 'end', value: unknown) => {
  const next = props.modelValue.map((range, i) =>
    i === index ? { ...range, [field]: String(value ?? '') } : range
  )
  emit('update:modelValue', next)
}

const onAdd = () => {
  emit('update:modelValue', [...props.modelValue, { begin: '', end: '' }])
}

const onRemove = (index: number) => {
  const next = [...props.modelValue]
  next.splice(index, 1)
  emit('update:modelValue', next)
}
</script>

<style lang="scss" scoped>
.address-range-editor {
  .rows {
    display: flex;
    flex-direction: column;
    gap: 0.5em;
    margin-bottom: 0.5em;
  }

  .row {
    display: flex;
    align-items: flex-start;
    gap: 0.75em;
  }

  .empty {
    color: var(--p-text-muted-color);
    margin: 0 0 0.5em 0;
  }
}
</style>
