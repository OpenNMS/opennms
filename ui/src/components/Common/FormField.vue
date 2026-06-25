<template>
  <div class="form-field">
    <label
      :for="controlId"
      class="form-field__label"
    >
      {{ label }}<span
        v-if="required"
        class="form-field__required"
        aria-hidden="true"
      >*</span>
    </label>
    <slot />
    <small
      v-if="error"
      class="field-error"
    >{{ error }}</small>
    <small
      v-else-if="hint"
      class="field-hint"
    >{{ hint }}</small>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  label: string
  for?: string
  required?: boolean
  error?: string
  hint?: string
}>(), {
  for: undefined,
  required: false,
  error: undefined,
  hint: undefined
})

// `for` is a reserved word; alias it for use in the template.
const controlId = computed(() => props.for)
</script>

<style lang="scss" scoped>
.form-field {
  display: flex;
  flex-direction: column;

  // Pilot-only: shorter controls inside FormField. Once every screen is
  // converted off IftaLabel/FloatLabel, promote 3rem to the global
  // .p-inputtext / .p-select rule in primevue-overrides.scss and delete
  // this block.
  :deep(.p-inputtext),
  :deep(.p-select) {
    height: 3rem;
  }

  :deep(.p-inputtext),
  :deep(.p-inputnumber),
  :deep(.p-select) {
    width: 100%;
  }
}

.form-field__label {
  display: block;
  margin-bottom: 0.375rem;
  font-size: 0.875rem;
  font-weight: 700;
  color: var(--p-text-color);
}

.form-field__required {
  margin-left: 0.125rem;
  color: var(--p-red-500);
}

.field-error {
  margin-top: 0.25rem;
  font-size: 0.875rem;
  color: var(--p-red-500);
}
</style>
