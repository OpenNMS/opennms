<template>
  <div class="string-list-editor">
    <div class="rows">
      <div
        v-for="(entry, index) in modelValue"
        :key="index"
        class="row"
      >
        <OnmsInputText
          :modelValue="entry"
          :inputId="`${idPrefix}-${index}`"
          :data-test="`${idPrefix}-value-${index}`"
          fluid
          @update:modelValue="onUpdate(index, $event)"
        />
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

    <p v-if="!modelValue.length" class="empty">No {{ itemLabel }} entries.</p>

    <OnmsButton
      variant="text"
      :data-test="`${idPrefix}-add`"
      @click="onAdd"
    >
      Add {{ itemLabel }}
    </OnmsButton>
  </div>
</template>

<script setup lang="ts">
import { OnmsButton, OnmsIconButton, OnmsInputText } from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'

const props = defineProps<{
  modelValue: string[]
  itemLabel: string
  idPrefix: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string[]]
}>()

const onUpdate = (index: number, value: unknown) => {
  const next = [...props.modelValue]
  next.splice(index, 1, String(value ?? ''))
  emit('update:modelValue', next)
}

const onAdd = () => {
  emit('update:modelValue', [...props.modelValue, ''])
}

const onRemove = (index: number) => {
  const next = [...props.modelValue]
  next.splice(index, 1)
  emit('update:modelValue', next)
}
</script>

<style lang="scss" scoped>
.string-list-editor {
  .rows {
    display: flex;
    flex-direction: column;
    gap: 0.5em;
    margin-bottom: 0.5em;
  }

  .row {
    display: flex;
    align-items: center;
    gap: 0.5em;
  }

  .empty {
    color: var(--p-text-muted-color);
    margin: 0 0 0.5em 0;
  }
}
</style>
