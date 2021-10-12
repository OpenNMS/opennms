<template>
  <div :class="['feather-row input', { 'first': first }]">
    {{ label }}
  </div>
  <div class="feather-row input">
    <slot></slot>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  props: {
    first: {
      type: Boolean,
      default: false
    },
    label: {
      type: String
    }
  }
})
</script>
