<template>
  <div class="card">
    <router-view />
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/themes/variables";

.card {
  background: var($surface);
  padding: 0px 20px 20px 20px;
}
</style>
