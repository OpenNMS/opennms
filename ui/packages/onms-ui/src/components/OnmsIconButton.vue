<template>
  <!--
    The `key` remounts the button when a tooltip appears or disappears, so the
    directive's beforeMount runs with the value present. PrimeVue captures the
    configured tooltip z-index only there (`updated` re-binds the events but
    never sets it), so a tooltip that arrives after mount would otherwise paint
    at the fallback z-index — behind the fixed menubar. Text changes keep the
    same key and don't remount.
  -->
  <Button
    v-onms-tooltip="tooltip"
    :key="tooltip ? 'tooltip' : 'no-tooltip'"
    :title="nativeTitle"
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
        :title="accessibleName"
      />
    </span>
  </Button>
</template>

<script setup lang="ts">
import Button from 'primevue/button'
import { computed } from 'vue'
import vOnmsTooltip from '../directives/OnmsTooltip'
import OnmsIcon from './OnmsIcon.vue'

// Seam wrapper (NMS-20029) for icon-only buttons: PrimeVue Button + OnmsIcon
// with consistent icon sizing. Defaults to the text variant (the dominant
// icon-button style). `title` names the button for assistive tech and, unless
// `tooltip` is also set, is the native browser tooltip too. class, data-*,
// aria-* and @click fall through to the button.
//
// `variant="ghost"` maps to PrimeVue's `text` AND `outlined` both true — the
// bordered, transparent-background button (matches OnmsButton's `ghost`
// variant for API consistency across the seam's two button wrappers).
//
// `tooltip` (NMS-20162) mounts the seam's tooltip directive on the button, so
// call sites declare a tooltip as a prop instead of relying on `v-onms-tooltip`
// falling through to the root element.
const props = withDefaults(defineProps<{
  icon: object
  iconSize?: string
  title?: string
  tooltip?: string
  variant?: 'text' | 'filled' | 'outlined' | 'ghost'
  severity?: 'primary' | 'danger'
  disabled?: boolean
  unsafePt?: unknown
}>(), {
  iconSize: '1.5rem',
  title: undefined,
  tooltip: undefined,
  variant: 'text',
  severity: 'primary',
  disabled: false,
  unsafePt: undefined
})

const pSeverity = computed(() => props.severity === 'danger' ? 'danger' : undefined)

// The rich tooltip replaces the browser's own, rather than stacking on top of
// it: `title` keeps naming the button for assistive tech, but is not rendered as
// the native tooltip attribute while `tooltip` is showing the same information.
const nativeTitle = computed(() => props.tooltip ? undefined : props.title)

// A `tooltip`-only call site still needs an accessible name, and the tooltip
// text is it.
const accessibleName = computed(() => props.title ?? props.tooltip)
</script>

<style lang="scss" scoped>
.onms-icon-button__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}
</style>
