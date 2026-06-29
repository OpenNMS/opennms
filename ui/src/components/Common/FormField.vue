<template>
  <div
    ref="rootEl"
    class="form-field"
  >
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
import { computed, nextTick, onMounted, ref, watch } from 'vue'

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

// Associate the error message with the slotted control for assistive tech.
// FormField can't set attributes on slot content from the template, and a
// declarative `:aria-describedby` on the control is unreliable across PrimeVue
// types (e.g. InputNumber merges fallthrough attrs onto its wrapper <span>, not
// the inner <input>). So target the real focusable input/textarea directly.
// Additive and idempotent: controls that already wire `errorId` themselves
// (the `v-slot="{ errorId }"` pattern) resolve to the same id. Plain Selects
// render no input and are simply skipped (the error keeps its role="alert").
const rootEl = ref<HTMLElement | null>(null)

const syncAriaDescribedby = () => {
  const control = rootEl.value?.querySelector<HTMLElement>('input, textarea')
  if (!control) {
    return
  }
  if (errorId.value) {
    control.setAttribute('aria-describedby', errorId.value)
  } else if (props.for && control.getAttribute('aria-describedby') === `${props.for}-error`) {
    control.removeAttribute('aria-describedby')
  }
}

onMounted(syncAriaDescribedby)
watch(errorId, () => nextTick(syncAriaDescribedby))
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

  // MultiSelect grows with its chip display, so normalize via min-height to
  // match the other controls when empty while still allowing it to grow.
  :deep(.p-multiselect) {
    min-height: 3rem;
  }

  :deep(.p-inputtext),
  :deep(.p-inputnumber),
  :deep(.p-select),
  :deep(.p-multiselect) {
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
