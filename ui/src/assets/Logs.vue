<template>
  <div class="logs">
    <Logs />
  </div>
</template>

<script setup lang="ts">
import Logs from './Logs.svg'
</script>

<style scoped lang="scss">
@import "@/styles/onms-tokens";
.logs {
  color: var($secondary-text-on-surface);
}
</style>
