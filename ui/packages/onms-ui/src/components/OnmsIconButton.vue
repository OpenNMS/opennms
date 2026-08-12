<template>
  <Button
    :title="title"
    :severity="pSeverity"
    :text="variant === 'text' || variant === 'ghost'"
    :outlined="variant === 'outlined' || variant === 'ghost'"
    :disabled="disabled"
    :pt="unsafePt as never"
  >
    <span
      class="onms-icon-button__icon"
      :style="{ fontSize: iconSize }"
    >
      <OnmsIcon
        :icon="icon"
        :title="title"
      />
    </span>
  </Button>
</template>

<script setup lang="ts">
import Button from 'primevue/button'
import { computed } from 'vue'
import OnmsIcon from './OnmsIcon.vue'

// Seam wrapper (NMS-20029) for icon-only buttons: PrimeVue Button + OnmsIcon
// with consistent icon sizing. Defaults to the text variant (the dominant
// icon-button style). `title` is both the native tooltip and the icon's
// accessible name. class, data-*, aria-* and @click fall through to the button.
//
// `variant="ghost"` maps to PrimeVue's `text` AND `outlined` both true — the
// bordered, transparent-background button (matches OnmsButton's `ghost`
// variant for API consistency across the seam's two button wrappers).
const props = withDefaults(defineProps<{
  icon: object
  iconSize?: string
  title?: string
  variant?: 'text' | 'filled' | 'outlined' | 'ghost'
  severity?: 'primary' | 'danger'
  disabled?: boolean
  unsafePt?: unknown
}>(), {
  iconSize: '1.5rem',
  title: undefined,
  variant: 'text',
  severity: 'primary',
  disabled: false,
  unsafePt: undefined
})

const pSeverity = computed(() => props.severity === 'danger' ? 'danger' : undefined)
</script>

<style lang="scss" scoped>
.onms-icon-button__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}
</style>
