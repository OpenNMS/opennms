<template>
  <div class="form-field">
    <label
      v-if="label"
      :for="controlId"
      class="form-field__label"
    >
      {{ label }}<span
        v-if="required"
        class="form-field__required"
        aria-hidden="true"
      >*</span>
    </label>
    <slot
      :errorId="errorId"
      :invalid="invalid"
    />
    <small
      v-if="error"
      :id="errorId"
      class="field-error"
      role="alert"
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
  label?: string
  for?: string
  required?: boolean
  error?: string
  hint?: string
}>(), {
  label: undefined,
  for: undefined,
  required: false,
  error: undefined,
  hint: undefined
})

// `for` is a reserved word; alias it for use in the template.
const controlId = computed(() => props.for)
const invalid = computed(() => !!props.error)
// Programmatic input→error association for screen readers. Defined only when
// there is both an error to point at and a control id to anchor it to.
const errorId = computed(() => (props.error && props.for ? `${props.for}-error` : undefined))
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

  // The global .field-hint keeps an IftaLabel-era left indent; neutralize it
  // here so the hint aligns flush-left with the label and error inside FormField.
  :deep(.field-hint) {
    padding-left: 0;
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
