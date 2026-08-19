<template>
  <IconField>
    <OnmsInputText
      :id="inputId"
      :modelValue="modelValue"
      :placeholder="placeholder"
      :aria-label="ariaLabel"
      :data-test="dataTest"
      @update:modelValue="emit('update:modelValue', $event)"
    />
    <InputIcon>
      <OnmsIcon :icon="SearchGlyph" />
    </InputIcon>
  </IconField>
</template>

<script setup lang="ts">
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import SearchGlyph from '../icons/action/Search.vue'
import OnmsIcon from './OnmsIcon.vue'
import OnmsInputText from './OnmsInputText.vue'

// Seam composite (NMS-20081): the OpenNMS search field — text input with a
// trailing search icon. Replaces every hand-rolled IconField/InputIcon/
// search-icon arrangement. Input-targeting attrs are declared props because
// fallthrough lands on the container div, not the <input>.
withDefaults(defineProps<{
  modelValue?: string
  placeholder?: string
  inputId?: string
  ariaLabel?: string
  dataTest?: string
}>(), {
  modelValue: undefined,
  placeholder: undefined,
  inputId: undefined,
  ariaLabel: undefined,
  dataTest: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: string | undefined]
}>()
</script>
