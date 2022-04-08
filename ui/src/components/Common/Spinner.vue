<template>
  <transition name="fade">
    <div class="spinner" v-if="spinnerState">
      <FeatherSpinner />
    </div>
  </transition>
</template>

<script setup lang="ts">
import { useStore } from 'vuex'
import { FeatherSpinner } from '@featherds/progress'

const store = useStore()
const spinnerState = computed(() => store.state.spinnerModule.spinnerState)
</script>

<style scoped lang="scss">
.spinner {
  z-index: 2;
  position: absolute;
  width: 95%;
  height: 90%;
  background: transparent;
}
.spinner-container {
  height: 75%;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s;
}
.fade-enter,
.fade-leave-to {
  opacity: 0;
}
</style>
