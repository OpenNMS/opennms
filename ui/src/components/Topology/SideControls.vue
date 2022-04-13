<template>
  <div class="topology-side-controls">
    <div class="controls">
      <CtrlSemanticZoomLevel />

      <FeatherButton class="refresh-btn" icon="Refresh" @click="refreshGraph">
        <FeatherIcon :icon="RefreshIcon" />
      </FeatherButton>

      <CtrlLayers />
      <CtrlHighlightFocusedNode />
    </div>
    <TopologyRightDrawer />
  </div>
</template>

<script setup lang="ts">
import CtrlSemanticZoomLevel from './CtrlSemanticZoomLevel.vue'
import CtrlHighlightFocusedNode from './CtrlHighlightFocusedNodes.vue'
import CtrlLayers from './CtrlLayers.vue'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import RefreshIcon from '@featherds/icon/navigation/Refresh'
import TopologyRightDrawer from './TopologyRightDrawer.vue'
import { PropType } from 'vue'
import { useStore } from 'vuex'

const store = useStore()

defineProps({
  refreshGraph: {
    required: true,
    type: Function as PropType<(payload: MouseEvent) => void>
  }
})

const width = computed<string>(() => store.state.topologyModule.isRightDrawerOpen ? '250px' : '65px')
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/themes/variables";


.topology-side-controls {
  @include elevation(2);
  display: flex;
  height: calc(100vh - 100px);
  width: v-bind(width);
  position: absolute;
  z-index: 0;
  top: 0;
  right: 0;

  .controls {
    display: block;
    width: 75px;
    border-right: 1px solid var($primary);
    padding-right: 15px;
  }
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
