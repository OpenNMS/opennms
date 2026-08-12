<template>
  <Button
    :label="label"
    :severity="pSeverity"
    :text="variant === 'text' || variant === 'ghost'"
    :outlined="variant === 'outlined' || variant === 'ghost'"
    :disabled="disabled"
    :loading="loading"
    :pt="unsafePt as never"
  >
    <template
      v-if="$slots.default"
      #default
    >
      <slot />
    </template>
  </Button>
</template>

<script setup lang="ts">
import Button from 'primevue/button'
import { computed } from 'vue'

// Seam wrapper (NMS-20029) around PrimeVue Button. Public API is the declared
// props/slot only; class, data-*, aria-* and native events fall through to the
// button root. `unsafePt` is an unsupported escape hatch that will not survive
// a future framework swap.
//
// `variant="ghost"` maps to PrimeVue's `text` AND `outlined` both true — the
// bordered, transparent-background button (the app's "Cancel" style). See the
// `.p-button-outlined.p-button-text` two-class selector in
// ui/src/styles/primevue-overrides.scss for the styling this depends on.
const props = withDefaults(defineProps<{
  label?: string
  variant?: 'filled' | 'outlined' | 'text' | 'ghost'
  severity?: 'primary' | 'danger'
  disabled?: boolean
  loading?: boolean
  unsafePt?: unknown
}>(), {
  label: undefined,
  variant: 'filled',
  severity: 'primary',
  disabled: false,
  loading: false,
  unsafePt: undefined
})

const pSeverity = computed(() => props.severity === 'danger' ? 'danger' : undefined)
</script>
