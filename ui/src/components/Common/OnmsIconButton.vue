<template>
  <Button :title="title">
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
// eslint-disable-next-line no-restricted-imports
import Button from 'primevue/button'
import OnmsIcon from '@/components/icons/OnmsIcon.vue'

// Seam wrapper around a PrimeVue Button + OnmsIcon for icon-only buttons.
// Bakes in consistent icon sizing (default 1.5rem). Button is the single
// template root, so @click, data-test, aria-label and all PrimeVue Button
// props (text, outlined, rounded, severity, disabled, ...) fall through to it.
// `title` is applied both to the button (native hover tooltip) and forwarded
// to OnmsIcon as its accessible name; when undefined it renders no attribute.
withDefaults(defineProps<{
  icon: object
  iconSize?: string
  title?: string
}>(), {
  iconSize: '1.5rem',
  title: undefined
})
</script>

<style lang="scss" scoped>
.onms-icon-button__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}
</style>
