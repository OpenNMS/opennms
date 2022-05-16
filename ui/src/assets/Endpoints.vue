<template>
  <div class="endpoints">
    <Endpoints />
  </div>
</template>

<script setup lang=ts>
import Endpoints from './Endpoints.svg'
</script>

<style scoped lang="scss">
@import "@featherds/styles/themes/variables";
.endpoints {
  color: var($secondary-text-on-surface);
}
</style>
