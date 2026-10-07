<template>
  <div class="parameter-list-editor">
    <div class="rows">
      <div
        v-for="(parameter, index) in modelValue"
        :key="index"
        class="row"
      >
        <FormField label="Key" :for="`${idPrefix}-key-${index}`">
          <OnmsInputText
            :modelValue="parameter.key"
            :inputId="`${idPrefix}-key-${index}`"
            :disabled="isLocked(parameter.key)"
            :data-test="`${idPrefix}-key-${index}`"
            fluid
            @update:modelValue="onUpdate(index, 'key', $event)"
          />
        </FormField>
        <FormField label="Value" :for="`${idPrefix}-value-${index}`">
          <OnmsInputText
            :modelValue="parameter.value"
            :inputId="`${idPrefix}-value-${index}`"
            :data-test="`${idPrefix}-value-${index}`"
            fluid
            @update:modelValue="onUpdate(index, 'value', $event)"
          />
        </FormField>
        <OnmsIconButton
          :icon="DeleteIcon"
          severity="danger"
          tooltip="Remove parameter"
          aria-label="Remove parameter"
          :disabled="isLocked(parameter.key)"
          :data-test="`${idPrefix}-remove-${index}`"
          @click="onRemove(index)"
        />
      </div>
    </div>

    <p v-if="!modelValue.length" class="empty">No parameters configured.</p>

    <OnmsButton variant="text" :data-test="`${idPrefix}-add`" @click="onAdd">Add parameter</OnmsButton>
  </div>
</template>

<script setup lang="ts">
import FormField from '@/components/Common/FormField.vue'
import type { ThreshdParameter } from '@/types/thresholdConfig'
import { OnmsButton, OnmsIconButton, OnmsInputText } from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'

const props = withDefaults(defineProps<{
  modelValue: ThreshdParameter[]
  idPrefix: string
  // Parameters edited by a dedicated field elsewhere in the form; shown here but not editable twice.
  lockedKeys?: string[]
}>(), {
  lockedKeys: () => []
})

const emit = defineEmits<{
  'update:modelValue': [value: ThreshdParameter[]]
}>()

const isLocked = (key: string) => props.lockedKeys.includes(key)

const onUpdate = (index: number, field: 'key' | 'value', value: unknown) => {
  const next = props.modelValue.map((parameter, i) =>
    i === index ? { ...parameter, [field]: String(value ?? '') } : parameter
  )
  emit('update:modelValue', next)
}

const onAdd = () => {
  emit('update:modelValue', [...props.modelValue, { key: '', value: '' }])
}

const onRemove = (index: number) => {
  const next = [...props.modelValue]
  next.splice(index, 1)
  emit('update:modelValue', next)
}
</script>

<style lang="scss" scoped>
.parameter-list-editor {
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
