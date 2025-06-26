<template>
  <div class="onms-search-result-header" :style="{ backgroundColor: bgColor, color: textColor, padding }">
    <slot />
  </div>
</template>

<script lang="ts" setup>
defineProps({
  label: { type: String, default: '' },
  bgColor: { type: String, default: '#343a40' },
  textColor: { type: String, default: '#ffffff' },
  padding: { type: String, default: '3px 6px' }
})
</script>

<style lang="scss" scoped>

</style>