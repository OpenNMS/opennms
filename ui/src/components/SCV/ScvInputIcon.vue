<template>
  <FeatherTooltip
    :title="title ?? tooltipTitle"
    v-slot="{ attrs, on }">
      <FeatherIcon
        v-bind="attrs" v-on="on"
        :icon="IconSecurity"
        class="scv-icon"
        @click="$emit('click')"
      />
  </FeatherTooltip>
</template>

<script setup lang="ts">
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
.scv-icon {
  cursor: pointer;
}
</style>
