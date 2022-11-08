<template>
  <div class="content">
    <!-- We can use the diff editor display for a single config -->
    <DCBDiff
      :config1="modalDeviceConfigBackup"
      :config2="modalDeviceConfigBackup"
      :mode="'unified'"
    />
  </div>
</template>

<script setup lang="ts">
import DCBDiff from './DCBDiff.vue'
import { DeviceConfigBackup } from '@/types/deviceConfig'
import { useStore } from 'vuex'

const store = useStore()
const modalDeviceConfigBackup = computed<DeviceConfigBackup>(() => store.state.deviceModule.modalDeviceConfigBackup)
</script>

<style scoped lang="scss">
.content {
  max-width: 1000px;
  max-height: calc(100vh - 400px);
  overflow: auto;
}
</style>
