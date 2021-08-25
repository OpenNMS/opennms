<template>
  <div :class="['p-flex-row input', { 'first': first }]">
    {{ label }}
  </div>
  <div class="p-flex-row input">
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

<style scoped lang="scss"></style>
