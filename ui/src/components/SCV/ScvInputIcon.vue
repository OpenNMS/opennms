<template>
  <OnmsIconButton
    class="scv-edit-icon"
    :aria-label="title ?? tooltipTitle"
    :disabled="disabled"
    v-onms-tooltip="title ?? tooltipTitle"
    :icon="IconSecurity"
    @click="$emit('click')"
  />
</template>

<script setup lang="ts">
import { OnmsIconButton } from '@opennms/onms-ui'
import IconSecurity from '@opennms/onms-ui/icons/network/Security.vue'

defineProps<{
  title?: string
  disabled?: boolean
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
}
</style>
