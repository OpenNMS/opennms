<template>
  <div class="endpoints">
    <Endpoints />
  </div>
</template>

<script setup lang="ts">
import Endpoints from './Endpoints.svg'
</script>

<style scoped lang="scss">
@import "@/styles/onms-tokens";
.endpoints {
  color: var($secondary-text-on-surface);
}
</style>
