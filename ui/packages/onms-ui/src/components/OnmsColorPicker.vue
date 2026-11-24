<template>
  <span class="onms-color-picker">
    <button
      type="button"
      class="onms-color-picker__trigger"
      :style="{ background: normalized || 'transparent' }"
      :disabled="disabled"
      :aria-label="triggerLabel"
      aria-haspopup="dialog"
      @click="popoverRef?.toggle($event)"
    />
    <OnmsPopover
      ref="popoverRef"
      :appendTo="appendTo"
      @hide="customOpen = false"
    >
      <div class="onms-color-picker__panel">
        <div
          class="onms-color-picker__grid"
          role="group"
          aria-label="Color swatches"
        >
          <button
            v-for="color in normalizedSwatches"
            :key="color"
            type="button"
            class="onms-color-picker__swatch"
            :class="{ 'is-selected': color === normalized }"
            :style="{ background: color }"
            :title="color"
            :aria-label="color"
            :aria-pressed="color === normalized"
            @click="pick(color)"
          />
        </div>

        <div class="onms-color-picker__footer">
          <span
            class="onms-color-picker__current"
            :style="{ background: normalized || 'transparent' }"
          />
          <span class="onms-color-picker__value">
            {{ normalized || 'none' }}<template v-if="isOffPalette"> (custom)</template>
          </span>
          <button
            type="button"
            class="onms-color-picker__custom-toggle"
            :aria-expanded="customOpen"
            @click="customOpen = !customOpen"
          >
            {{ customOpen ? 'Hide custom' : 'Custom…' }}
          </button>
        </div>

        <!-- The spectrum is the same PrimeVue ColorPicker, inline so it draws
             inside this panel rather than opening a second overlay. -->
        <div
          v-if="customOpen"
          class="onms-color-picker__spectrum"
        >
          <ColorPicker
            :modelValue="normalized"
            inline
            format="hex"
            @update:modelValue="onSpectrum"
          />
        </div>
      </div>
    </OnmsPopover>
  </span>
</template>

<script lang="ts">
// Neutrals, mid tones, then pale tones. The neutrals row carries the shape
// stroke/fill and label defaults the topology map already ships, so a stock
// view reads as on-palette. Override with `swatches` for a different set.
const DEFAULT_SWATCHES = [
  '#000000', '#1d2939', '#334155', '#475569', '#64748b',
  '#9aa7b8', '#cbd5e1', '#e2e8f0', '#f1f5f9', '#ffffff',
  '#ef4444', '#f97316', '#f59e0b', '#eab308', '#22c55e',
  '#14b8a6', '#3b82f6', '#6366f1', '#8b5cf6', '#ec4899',
  '#fca5a5', '#fdba74', '#fcd34d', '#fde047', '#86efac',
  '#5eead4', '#93c5fd', '#a5b4fc', '#c4b5fd', '#f9a8d4'
]
</script>

<script setup lang="ts">
import ColorPicker from 'primevue/colorpicker'
import { computed, ref } from 'vue'
import OnmsPopover from './OnmsPopover.vue'

// Seam composite (NMS-20029) for choosing a color: a swatch grid with a
// spectrum behind a "Custom" toggle, in the shape of the native GTK/Firefox
// color dialog it replaces.
//
// It exists rather than exposing PrimeVue ColorPicker directly for two reasons.
// PrimeVue's picker is spectrum-only (4.5.5 has no swatch or preset surface at
// all), and a native <input type="color"> opens an OS-level dialog the page can
// neither dismiss nor observe, so it outlives whatever it was opened for. This
// panel is an OnmsPopover, which closes on an outside click or Escape.
//
// The seam's value is a CSS hex color (`#rrggbb`) in both directions. PrimeVue
// is asymmetric there: it accepts hex with or without the leading '#' but always
// emits it WITHOUT, and a bare `aabbcc` is not a valid CSS color, so it fails
// silently wherever the value is used as one.
//
// Composite, so no `unsafePt`: there is no single underlying pt root to target.

const props = withDefaults(defineProps<{
  modelValue?: string
  disabled?: boolean
  swatches?: string[]
  appendTo?: string
}>(), {
  modelValue: undefined,
  disabled: false,
  swatches: () => DEFAULT_SWATCHES,
  appendTo: 'body'
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const SIX_DIGIT_HEX = /^#?([0-9a-fA-F]{6})$/

// Anything that is not a six-digit hex is passed through untouched rather than
// swallowed, so a format change upstream surfaces instead of going missing.
const toCssHex = (value: unknown): string => {
  const match = typeof value === 'string' ? value.match(SIX_DIGIT_HEX) : null
  return match ? `#${match[1].toLowerCase()}` : String(value ?? '')
}

const popoverRef = ref<InstanceType<typeof OnmsPopover>>()
const customOpen = ref(false)

const normalized = computed(() => toCssHex(props.modelValue))
// Swatches come from callers and are not constrained to lowercase, so both the
// selection test and the off-palette test go through the same normalisation as
// the model value.
const normalizedSwatches = computed(() => props.swatches.map(toCssHex))
const isOffPalette = computed(() =>
  !!normalized.value && !normalizedSwatches.value.includes(normalized.value))
const triggerLabel = computed(() => `Choose a color, currently ${normalized.value || 'none'}`)

const pick = (color: string) => {
  emit('update:modelValue', toCssHex(color))
  popoverRef.value?.hide()
}

const onSpectrum = (value: unknown) => {
  emit('update:modelValue', toCssHex(value))
}
</script>

<style scoped>
.onms-color-picker {
  display: inline-flex;
}

.onms-color-picker__trigger {
  width: 3rem;
  height: 2rem;
  padding: 0;
  border: 1px solid var(--p-content-border-color);
  border-radius: 4px;
  cursor: pointer;
}

.onms-color-picker__trigger:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.onms-color-picker__panel {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.onms-color-picker__grid {
  display: grid;
  grid-template-columns: repeat(10, 1.25rem);
  gap: 0.25rem;
}

.onms-color-picker__swatch {
  width: 1.25rem;
  height: 1.25rem;
  padding: 0;
  border: 1px solid var(--p-content-border-color);
  border-radius: 3px;
  cursor: pointer;
}

/* Ring rather than a border swap, so the swatch colour is never clipped. */
.onms-color-picker__swatch.is-selected {
  outline: 2px solid var(--p-primary-color);
  outline-offset: 1px;
}

.onms-color-picker__footer {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: var(--p-text-muted-color);
}

.onms-color-picker__current {
  width: 1.25rem;
  height: 1.25rem;
  border: 1px solid var(--p-content-border-color);
  border-radius: 3px;
}

.onms-color-picker__value {
  font-family: monospace;
}

.onms-color-picker__custom-toggle {
  margin-inline-start: auto;
  padding: 0;
  border: 0;
  background: none;
  color: var(--p-primary-color);
  cursor: pointer;
  font-size: 0.8rem;
}
</style>
