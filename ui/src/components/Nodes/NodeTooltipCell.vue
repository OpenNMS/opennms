<template>
  <td :class="text ? 'pointer' : ''">
    <FeatherTooltip
      :title="text"
      :alignment="PointerAlignment.left"
      :placement="PopoverPlacement.top"
      v-if="text"
      v-slot="{ attrs, on }">
        <span v-bind="attrs" v-on="on">{{ ellipsify(text, 30) }}</span>
    </FeatherTooltip>
  </td>
</template>

<script setup lang="ts">
import { FeatherTooltip, PointerAlignment, PopoverPlacement } from '@featherds/tooltip'
import { ellipsify } from '@/lib/utils'

defineProps({
  text: {
    required: true,
    type: String
  }
})

</script>
