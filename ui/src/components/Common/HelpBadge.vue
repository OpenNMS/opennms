<template>
  <OnmsIconButton
    :icon="InfoIcon"
    iconSize="1rem"
    class="help-info-button"
    :title="ariaLabel ?? 'Help'"
    :aria-label="ariaLabel ?? 'Help'"
    data-test="help-badge"
    @click="pop?.toggle($event)"
  />
  <OnmsPopover ref="pop">
    <div class="help-popover">{{ content }}</div>
  </OnmsPopover>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { OnmsIconButton, OnmsPopover } from '@opennms/onms-ui'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'

// An info affordance that opens its help text in a click-anchored popover (the
// native title attribute only shows on hover and never on click/touch).
defineProps<{ content: string, ariaLabel?: string }>()

const pop = ref<InstanceType<typeof OnmsPopover>>()
</script>

<style scoped lang="scss">
// Compact: strip the button's default padding so it fits within a field's
// label line. A full-size icon button would make label rows that carry help
// taller than those without, dropping that field's control below its row-mates
// (visible misalignment in multi-field rows). The class falls through to the
// PrimeVue Button root, so styling it directly (scoped attr beats .p-button).
.help-info-button {
  margin-left: 0.25rem;
  vertical-align: middle;
  width: 1.25rem;
  min-width: 1.25rem;
  height: 1.25rem;
  padding: 0;
}

.help-popover {
  max-width: 340px;
  padding: 0.5rem 0.25rem;
  font-size: 0.85rem;
  line-height: 1.45;
  white-space: normal;
}
</style>
