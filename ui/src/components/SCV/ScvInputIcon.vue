<template>
  <PButton
    text
    :aria-label="title ?? tooltipTitle"
    class="scv-edit-icon"
    v-tooltip="title ?? tooltipTitle"
    @click="$emit('click')"
  >
    <FeatherIcon
      :icon="IconSecurity"
      class="scv-icon"
    />
  </PButton>
</template>

<script setup lang="ts">
import Button from 'primevue/button'
import { FeatherIcon } from '@featherds/icon'
import IconSecurity from '@featherds/icon/network/Security'

const PButton = Button

defineProps<{
  title?: string
}>()

defineEmits<{
  (e: 'click'): void
}>()

const tooltipTitle = 'Use SCV to populate this field'
</script>

<style scoped lang="scss">
.scv-edit-icon {
  border: 2px solid var(--p-text-color);
  border-radius: 100%;
  height: 2rem;
  width: 2rem;
  min-width: 2rem;
  padding: 0;

  :deep(svg.scv-icon) {
    cursor: pointer;
    font-size: 1.25em !important;
    width: 1.5em;
    height: 1.5em;
  }
}
</style>
