<template>
  <div class="content">
    <!-- We can use the diff editor display for a single config -->
    <DCBDiff
      :config1="deviceStore.modalDeviceConfigBackup"
      :config2="deviceStore.modalDeviceConfigBackup"
      :mode="'unified'"
    />
  </div>
</template>

<script setup lang="ts">
import DCBDiff from './DCBDiff.vue'
import { useDeviceStore } from '@/stores/deviceStore'

const deviceStore = useDeviceStore()
</script>

<style scoped lang="scss">
.content {
  max-width: 1000px;
  max-height: calc(100vh - 400px);
  overflow: auto;
}
</style>
