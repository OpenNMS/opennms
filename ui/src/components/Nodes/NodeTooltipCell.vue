<template>
  <span
    v-if="text"
    v-tooltip.top="text"
    class="pointer"
  >{{ ellipsify(text, 30) }}</span>
</template>

<script setup lang="ts">
import { ellipsify } from '@/lib/utils'

defineProps({
  text: {
    required: true,
    type: String
  }
})
</script>

<style lang="scss" scoped>
.pointer {
  cursor: pointer;
}
</style>
