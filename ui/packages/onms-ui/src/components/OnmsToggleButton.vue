<template>
  <ToggleButton
    :modelValue="modelValue"
    :onLabel="onLabel ?? label ?? ''"
    :offLabel="offLabel ?? label ?? ''"
    :disabled="disabled"
    :size="size"
    :fluid="fluid"
    :pt="unsafePt as never"
    @update:modelValue="onUpdate"
  >
    <template
      v-if="onIcon || offIcon"
      #icon="{ value, class: iconClass }"
    >
      <OnmsIcon
        v-if="value ? onIcon : offIcon"
        :icon="value ? onIcon : offIcon"
        :class="iconClasses(value, iconClass)"
      />
    </template>
  </ToggleButton>
</template>

<script setup lang="ts">
import ToggleButton from 'primevue/togglebutton'
import OnmsIcon from './OnmsIcon.vue'

// Seam wrapper around PrimeVue ToggleButton: a button that stays pressed (on)
// or not (off). Boolean-only. data-* / aria-* fall through to the <button>.
//
// - `label` is a shortcut for a button whose text does not change; `onLabel` /
//   `offLabel` override it per state. With none of them the button has no text
//   (PrimeVue's own defaults are "Yes" / "No"), so give it an aria-label.
// - `onIcon` / `offIcon` take an icon component (from
//   '@opennms/onms-ui/icons/...'), not PrimeVue's CSS class string. The icon
//   gets `onms-toggle-button-icon` plus `--on` / `--off`; the wrapper sets no
//   icon color, so a consumer colors those classes.
// - `change` emits the new value directly, not PrimeVue's DOM event, matching
//   OnmsSelectButton.
withDefaults(defineProps<{
  modelValue?: boolean
  label?: string
  onLabel?: string
  offLabel?: string
  onIcon?: object
  offIcon?: object
  disabled?: boolean
  size?: 'small' | 'large'
  fluid?: boolean
  unsafePt?: unknown
}>(), {
  modelValue: false,
  label: undefined,
  onLabel: undefined,
  offLabel: undefined,
  onIcon: undefined,
  offIcon: undefined,
  disabled: false,
  size: undefined,
  fluid: undefined,
  unsafePt: undefined
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  change: [value: boolean]
}>()

const onUpdate = (value: boolean) => {
  emit('update:modelValue', value)
  emit('change', value)
}

// OnmsIcon takes its class as a plain string. PrimeVue's own icon class comes
// first, so the icon keeps PrimeVue's layout and default color.
const iconClasses = (on: boolean, iconClass: unknown) =>
  [iconClass, 'onms-toggle-button-icon', on ? 'onms-toggle-button-icon--on' : 'onms-toggle-button-icon--off']
    .filter(c => typeof c === 'string' && c.length > 0)
    .join(' ')
</script>
