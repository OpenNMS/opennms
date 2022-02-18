<template>
  <div class="topology-side-controls">
    <SemanticZoomLevelControl />
  </div>
</template>

<script setup lang="ts">
import SemanticZoomLevelControl from './SemanticZoomLevelControl.vue'
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/elevation";

.topology-side-controls {
  @include elevation(2);
  height: calc(100vh - 120px);
  width: 75px;
}
</style>

<style lang="scss">
.topology-side-controls {
  .btn {
    margin: 0px 0px 0px 15px !important;
  }
  .chip {
    margin: 0px 0px 0px 12px !important;
  }
}
</style>
