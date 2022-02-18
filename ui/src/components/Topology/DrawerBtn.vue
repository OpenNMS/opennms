<template>
  <div class="drawer-btn" v-if="!isOpen">
    <FeatherButton icon="open" @click="openLeftDrawer">
      <FeatherIcon :icon="ChevronRight" />
    </FeatherButton>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { FeatherIcon } from '@featherds/icon'
import { FeatherButton } from '@featherds/button'
import ChevronRight from '@featherds/icon/navigation/ChevronRight'
import { useStore } from 'vuex'

const store = useStore()

const isOpen = computed<boolean>(() => store.state.topologyModule.isLeftDrawerOpen)
const openLeftDrawer = () => store.dispatch('topologyModule/openLeftDrawer')

</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/elevation";

.drawer-btn {
  @include elevation(2);
  position: absolute;
  z-index: 1000;
  border-top-right-radius: 5px;
  border-bottom-right-radius: 5px;
  top: 20px;
  button {
    margin: 5px;
  }
}
</style>
