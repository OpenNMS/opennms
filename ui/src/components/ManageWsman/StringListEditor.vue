<template>
  <div class="string-list" :data-test="dataTest">
    <ul class="items">
      <li v-for="(item, i) in modelValue" :key="`${i}-${item}`" class="item">
        <code class="value">{{ item }}</code>
        <OnmsIconButton :icon="Delete" severity="danger" :title="`Remove ${item}`" :aria-label="`Remove ${item}`" :data-test="`${dataTest}-remove`" @click="removeAt(i)" />
      </li>
    </ul>
    <div class="add-row">
      <OnmsInputText v-model="draft" :placeholder="placeholder" :invalid="!!draft && !accepts(draft)" fluid :data-test="`${dataTest}-input`" @keydown.enter.prevent="add" />
      <OnmsButton variant="outlined" label="Add" :disabled="!draft.trim() || !accepts(draft)" :data-test="`${dataTest}-add`" @click="add" />
    </div>
    <small v-if="hint" class="hint">{{ hint }}</small>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { OnmsButton, OnmsIconButton, OnmsInputText } from '@opennms/onms-ui'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'

const props = withDefaults(defineProps<{
  modelValue: string[]
  placeholder?: string
  hint?: string
  dataTest: string
  // returns true when a draft may be added
  validator?: (value: string) => boolean
}>(), {
  placeholder: undefined,
  hint: undefined,
  validator: undefined
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string[]): void
}>()

const draft = ref('')

const accepts = (value: string) => (props.validator ? props.validator(value) : true)

const add = () => {
  const v = draft.value.trim()
  if (v && accepts(v) && !props.modelValue.includes(v)) {
    emit('update:modelValue', [...props.modelValue, v])
  }
  draft.value = ''
}

const removeAt = (i: number) => {
  emit('update:modelValue', props.modelValue.filter((_, idx) => idx !== i))
}
</script>

<style lang="scss" scoped>
.items {
  list-style: none;
  margin: 0 0 0.4rem 0;
  padding: 0;
}

.item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.value {
  flex: 1 1 auto;
  word-break: break-all;
}

.add-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.hint {
  color: var(--p-text-muted-color);
}
</style>
