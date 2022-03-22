<template>
  <div class="topology-side-controls">
    <SemanticZoomLevelControl />

    <FeatherButton class="refresh-btn" icon="Refresh" @click="refreshGraph">
      <FeatherIcon :icon="RefreshIcon" />
    </FeatherButton>
  </div>
</template>

<script setup lang="ts">
import SemanticZoomLevelControl from './SemanticZoomLevelControl.vue'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import RefreshIcon from '@featherds/icon/navigation/Refresh'
import { PropType } from 'vue'

defineProps({
  refreshGraph: {
    required: true,
    type: Function as PropType<(payload: MouseEvent) => void>
  }
})

</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/elevation";

.topology-side-controls {
  @include elevation(2);
  height: calc(100vh - 100px);
  width: 75px;
  position: absolute;
  display: block;
  z-index: 0;
  top: 0;
  right: 0;
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
  .refresh-btn {
    margin-top: 5px !important;
  }
}
</style>
