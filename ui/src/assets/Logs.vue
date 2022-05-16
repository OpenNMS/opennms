<template>
  <div class="logs">
    <Logs />
  </div>
</template>

<script setup lang=ts>
import Logs from './Logs.svg'
</script>

<style scoped lang="scss">
@import "@featherds/styles/themes/variables";
.logs {
  color: var($secondary-text-on-surface);
}
</style>
