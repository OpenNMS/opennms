<template>
  <div class="strategy-editor">
    <FormField :label="label" :for="`${idPrefix}-class`" required :error="classError" :info="info">
      <OnmsAutoComplete
        :inputId="`${idPrefix}-class`"
        :modelValue="modelValue.className"
        :suggestions="suggestions"
        :invalid="!!classError"
        :data-test="`${idPrefix}-class`"
        dropdown
        fluid
        @complete="onComplete"
        @update:modelValue="onClassName"
      />
    </FormField>
    <div class="parameters">
      <span class="parameters-label">Parameters</span>
      <ParameterListEditor
        :modelValue="modelValue.parameters"
        :idPrefix="`${idPrefix}-parameter`"
        @update:modelValue="emit('update:modelValue', { ...modelValue, parameters: $event })"
      />
      <small v-if="parametersError" class="field-error" role="alert" :data-test="`${idPrefix}-parameters-error`">{{ parametersError }}</small>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import FormField from '@/components/Common/FormField.vue'
import ParameterListEditor from '@/components/ThresholdConfiguration/Common/ParameterListEditor.vue'
import type { ResourceTypeStrategy } from '@/types/resourceTypesConfig'
import { OnmsAutoComplete } from '@opennms/onms-ui'

// A strategy class with its parameters. The class is free text: the options are the classes shipped with
// OpenNMS, but a plugin may bring its own.
const props = defineProps<{
  modelValue: ResourceTypeStrategy
  label: string
  idPrefix: string
  options: string[]
  classError?: string
  parametersError?: string
  info?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: ResourceTypeStrategy]
}>()

const suggestions = ref<string[]>([])

const onComplete = (query: string) => {
  const term = (query ?? '').trim().toLowerCase()
  suggestions.value = term ? props.options.filter(option => option.toLowerCase().includes(term)) : [...props.options]
}

const onClassName = (value: unknown) => {
  emit('update:modelValue', { ...props.modelValue, className: String(value ?? '') })
}
</script>

<style lang="scss" scoped>
.strategy-editor {
  display: flex;
  flex-direction: column;
  gap: 0.75em;

  .parameters {
    display: flex;
    flex-direction: column;
    gap: 0.5em;
    padding-left: 1em;
    border-left: 2px solid var(--p-content-border-color);
  }

  .parameters-label {
    font-weight: 600;
  }
}
</style>
