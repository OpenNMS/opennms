<template>
  <transition name="fade">
    <div class="spinner" v-if="isActive">
      <PProgressSpinner
        :style="{ width: '2.5rem', height: '2.5rem' }"
        strokeWidth="4"
      />
    </div>
  </transition>
</template>

<script setup lang="ts">
import ProgressSpinner from 'primevue/progressspinner'
import useSpinner from '@/composables/useSpinner'

const PProgressSpinner = ProgressSpinner
const { isActive } = useSpinner()

</script>

<style scoped lang="scss">
.spinner {
  z-index: 2;
  position: absolute;
  width: 95%;
  height: 90%;
  background: transparent;
  // FeatherSpinner centered itself via flex; replicate so ProgressSpinner is centered.
  display: flex;
  justify-content: center;
  align-items: center;
}

// FeatherSpinner used a single primary-colored stroke. PrimeVue's ProgressSpinner
// cycles through four colors via the p-progressspinner-color animation; pin the
// stroke to the primary color and drop the color-cycle animation (keep the dash
// animation for the spinning arc; rotation lives on .p-progressspinner-spin).
:deep(.p-progressspinner-circle) {
  stroke: var(--p-primary-color);
  animation: p-progressspinner-dash 1.5s ease-in-out infinite;
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
