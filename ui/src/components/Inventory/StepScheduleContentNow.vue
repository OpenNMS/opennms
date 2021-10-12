<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="title">Now pinging devices to identify nodes... Complete to go to Nodes table.</div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({})
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
.title {
  @include headline3();
}
</style>
