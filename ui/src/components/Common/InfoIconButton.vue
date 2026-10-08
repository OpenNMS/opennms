<template>
  <OnmsIcon
    :icon="InfoIcon"
    class="info-icon-button"
    role="button"
    tabindex="0"
    :aria-label="ariaLabel"
    @click="emit('click')"
    @keydown.enter.space.prevent="emit('click')"
  />
</template>

<script setup lang="ts">
import { OnmsIcon } from '@opennms/onms-ui'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'

// Sized in em, so it scales with the title it sits in or beside.
defineProps<{
  ariaLabel: string
}>()

const emit = defineEmits<{
  click: []
}>()
</script>

<style lang="scss" scoped>
.info-icon-button {
  font-size: 1.2em;
  vertical-align: -0.2em;
  margin-left: 0.3em;
  cursor: pointer;
  color: var(--p-text-muted-color);

  &:hover {
    color: var(--p-primary-color);
  }
}
</style>
