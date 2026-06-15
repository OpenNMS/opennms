<template>
  <FeatherTooltip
    :title="title ?? tooltipTitle"
    v-slot="{ attrs, on }"
  >
    <FeatherButton
      icon="SCV"
      class="scv-edit-icon"
      @click="$emit('click')"
    >
      <FeatherIcon
        v-bind="attrs"
        v-on="on"
        :icon="IconSecurity"
        class="scv-icon"
      />
    </FeatherButton>
  </FeatherTooltip>
</template>

<script setup lang="ts">
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import { FeatherTooltip } from '@featherds/tooltip'
import IconSecurity from '@featherds/icon/network/Security'

defineProps<{
  title?: string
}>()

defineEmits<{
  (e: 'click'): void
}>()

const tooltipTitle = 'Use SCV to populate this field'
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';

.btn.btn-icon.scv-edit-icon {
  border: 2px solid var(--feather-primary-text-on-surface);
  border-radius: 100%;
  height: 2.0rem;
  width: 2.0rem;
  min-width: 2.0rem;

  svg.scv-icon {
    cursor: pointer;
    font-size: 1.25em !important;
    background-color: var(--feather-background);
    width: 1.5em;
    height: 1.5em;

    &:hover {
      background-color: var(--feather-shade-4);
    }
  }
}
</style>
