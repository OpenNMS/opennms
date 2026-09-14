<template>
  <div class="compare">
    <Compare />
  </div>
</template>

<script setup lang="ts">
import Compare from './Compare.svg'
</script>

<style scoped lang="scss">
@import "@/styles/onms-tokens";
.compare {
  width: 24px;
  height: 24px;

  svg {
    width: 24px;
    height: 24px;
    margin-top: -10px;
  }
}
</style>
