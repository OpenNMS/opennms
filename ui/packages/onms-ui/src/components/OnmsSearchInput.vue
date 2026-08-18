<template>
  <IconField>
    <InputIcon>
      <OnmsIcon :icon="IconSearch" />
    </InputIcon>
    <OnmsInputText
      ref="inputTextRef"
      :id="inputId"
      :modelValue="modelValue"
      :placeholder="placeholder"
      :aria-label="ariaLabel"
      :data-test="dataTest"
      @update:modelValue="emit('update:modelValue', $event)"
    />
    <!-- Always rendered, even with nothing to clear: PrimeVue sizes the input's
         trailing padding off DOM position (`.p-inputtext:not(:last-child)`), not
         content, so keeping this element in place reserves the space for the
         clear button instead of letting the text reflow on the first keystroke. -->
    <span class="onms-search-input__clear">
      <button
        v-if="hasValue"
        type="button"
        class="onms-search-input__clear-button"
        :aria-label="clearAriaLabel"
        :data-test="dataTest ? `${dataTest}-clear` : undefined"
        @click="clear"
      >
        <OnmsIcon :icon="IconCancel" />
      </button>
    </span>
  </IconField>
</template>

<script setup lang="ts">
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import { computed, nextTick, ref } from 'vue'
import IconCancel from '../icons/navigation/Cancel.vue'
import IconSearch from '../icons/action/Search.vue'
import OnmsIcon from './OnmsIcon.vue'
import OnmsInputText from './OnmsInputText.vue'

// Seam composite (NMS-20081): the OpenNMS search field — a leading search icon,
// the text input, and a trailing clear button that appears once there is
// something to clear (NMS-20160). Replaces every hand-rolled
// IconField/InputIcon/search-icon/clear-button arrangement. Input-targeting
// attrs are declared props because fallthrough lands on the container div, not
// the <input>.
const props = withDefaults(defineProps<{
  modelValue?: string
  placeholder?: string
  inputId?: string
  ariaLabel?: string
  dataTest?: string
  clearAriaLabel?: string
}>(), {
  modelValue: undefined,
  placeholder: undefined,
  inputId: undefined,
  ariaLabel: undefined,
  dataTest: undefined,
  clearAriaLabel: 'Clear search'
})

const emit = defineEmits<{
  'update:modelValue': [value: string | undefined]
  'clear': []
}>()

const inputTextRef = ref<InstanceType<typeof OnmsInputText> | null>(null)

const hasValue = computed(() => (props.modelValue ?? '') !== '')

const inputElement = () => {
  const el = inputTextRef.value?.$el as HTMLElement | null | undefined

  if (!el) {
    return null
  }

  return el instanceof HTMLInputElement ? el : el.querySelector('input')
}

const focus = () => inputElement()?.focus()
const blur = () => inputElement()?.blur()

const clear = async () => {
  emit('update:modelValue', '')
  emit('clear')
  // The button unmounts as soon as the value is empty, which would drop focus
  // to the body; hand it back to the input so typing can continue.
  await nextTick()
  focus()
}

defineExpose({ focus, blur })
</script>

<style lang="scss" scoped>
// IconField anchors its icons to the *field's* edges, so the input has to fill
// the field or the icons drift away from it. PrimeVue leaves that to the caller;
// the seam owns it here, so the field behaves like one control (and every call
// site stops re-declaring these two rules).
.p-iconfield {
  display: block;
  width: 100%;
}

// Re-assert the theme's own input colors (the same tokens PrimeVue's rules use).
// PrimeVue ships its component CSS inside `@layer primevue`, and *unlayered*
// author styles beat any layer no matter how weak the selector — so the legacy
// Bootstrap override in opennms-styles.scss (`input, span, … { color: inherit }`,
// loaded by the menu bundle) silently wins and the field's text and icon inherit
// whatever the surrounding surface uses. On the dark menubar that renders them
// white-on-white (NMS-20160).
.p-inputtext {
  width: 100%;
  color: var(--p-inputtext-color);
}

.p-inputicon {
  color: var(--p-iconfield-icon-color);
}

// Mirrors PrimeVue's own .p-inputicon placement (which we can't reuse: InputIcon
// hard-codes aria-hidden on its wrapper, and a focusable button must not sit
// inside an aria-hidden subtree).
.onms-search-input__clear {
  position: absolute;
  top: 50%;
  inset-inline-end: var(--p-form-field-padding-x, 0.75rem);
  transform: translateY(-50%);
  line-height: 1;
  z-index: 1;
}

.onms-search-input__clear-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: transparent;
  color: var(--p-form-field-icon-color, var(--p-text-muted-color));
  // Matches the leading search glyph, which primevue-overrides.scss normalizes
  // to 1.5rem app-wide; the PrimeVue default (--p-icon-size, 1rem) would leave
  // the two icons in the same field visibly different sizes.
  font-size: 1.5rem;
  line-height: 1;
  cursor: pointer;

  &:hover {
    color: var(--p-text-color);
  }

  &:focus-visible {
    outline: 1px solid var(--p-primary-color);
    outline-offset: 2px;
  }
}
</style>
