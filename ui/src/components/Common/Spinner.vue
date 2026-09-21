<template>
  <transition name="fade">
    <div class="spinner" v-if="isActive">
      <OnmsSpinner />
    </div>
  </transition>
</template>

<script setup lang="ts">
import { OnmsSpinner } from '@opennms/onms-ui'
import useSpinner from '@/composables/useSpinner'

const { isActive } = useSpinner()
</script>

<style scoped lang="scss">
.spinner {
  z-index: 2;
  position: absolute;
  width: 95%;
  height: 90%;
  background: transparent;
  // FeatherSpinner centered itself via flex; replicate so the spinner is centered.
  display: flex;
  justify-content: center;
  align-items: center;
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
