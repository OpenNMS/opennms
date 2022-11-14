<template>
  <div class="speed">
    <Speed />
  </div>
</template>

<script setup lang="ts">
import Speed from './Speed.svg'
</script>

<style scoped lang="scss">
@import "@featherds/styles/themes/variables";
.speed {
  color: var($primary);

  svg {
    margin-bottom: -2px;
  }
}
</style>
