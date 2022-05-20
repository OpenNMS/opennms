<template>
  <div class="logo">
    <Logo />
  </div>
</template>

<script setup lang=ts>
import Logo from './OpenNMS_Horizontal-Logo_Light-BG.svg'
</script>

<style scoped lang="scss">
@import "@featherds/styles/themes/variables";
.logo {
  width: 8em !important;
  color: var($primary-text-on-color) !important;
}
</style>
