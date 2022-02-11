<template>
  <div class="wrapper" @mouseleave="timeoutOut">
    <div class="inner-short" @mouseenter="timeoutIn">{{ shortText }}</div>
    <div class="inner-float" :class="hover && 'hovering'">
      <div class="floating" ref="floating">{{ props.text }}</div>
      <div class="button">
        <FeatherButton icon="Edit" @click="copyURLToClipboard">
          <FeatherIcon :icon="Edit" class="edit-icon"></FeatherIcon>
        </FeatherButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, reactive } from 'vue'

import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'

import Edit from '@featherds/icon/action/Edit'

import { useConfigurationToast } from './hooks/configurationToast'

/**
 * Props
 */
const props = defineProps({
  text: String
})

/**
 * Hooks
 */
const { updateToast } = useConfigurationToast()

/**
 * Local State
 */
const hover = ref(false)
const floating = ref<HTMLElement | null>(null)
let timeout = reactive({ value: -1 })

const shortText = computed(() => {
  const len = props?.text?.length || 0
  return len > 30 ? props?.text?.slice(0, 30) + '...' : props.text
})

/**
 * Copies the full Requisition Definition URL to the clipboard.
 */
const copyURLToClipboard = () => {
  if (floating.value && props.text) {
    navigator.clipboard.writeText(props.text)
    updateToast({
      basic: `Copied: ${props.text.length > 70 ? props.text.substring(0, 70) + '...' : props.text}`,
      hasErrors: false
    })
  }
}

/**
 * Hides the copy/paste display
 */
const timeoutOut = () => {
  clearTimeout(timeout.value)
  hover.value = false
}

/**
 * Waits 250 milliseconds before showing the copy/paste display
 * as to prevent accidental triggers.
 */
const timeoutIn = () => {
  clearTimeout(timeout.value)
  timeout.value = window.setTimeout(() => {
    hover.value = true
  }, 250)
}
</script>

<style lang="scss">
.inner-float .btn-icon {
  margin: 0;
}
</style>
<style lang="scss" scoped>
@import "@featherds/styles/mixins/elevation";
.inner-short {
  cursor: pointer;
}
.inner-float {
  position: absolute;
  background-color: var(--feather-background);
  display: flex;
  @include elevation(2);
  padding: 20px;
  opacity: 0;
  transition: opacity ease-in-out 0.1s;
  pointer-events: none;
  align-items: center;
  max-width: 25vw;
}

.inner-float.hovering {
  opacity: 1;
  pointer-events: all;
  z-index: 2;
}
</style>