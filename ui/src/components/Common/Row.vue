<template>
  <div :class="`feather-col-${col || 12} headling4`">{{ label }}</div>
  <div class="feather-row">
    <div :class="`feather-col-${col || 12}`">
      <slot></slot>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  props: {
    label: {
      type: String
    },
    col: {
      type: String
    }
  }
})
</script>
