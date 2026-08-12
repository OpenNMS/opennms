<template>
  <OnmsDialog
    :visible="visible"
    :header="title"
    modal
    @update:visible="onVisibleChange"
  >
    <div class="content">
      <slot name="content" />
    </div>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import { OnmsDialog } from '@opennms/onms-ui'
import { useDeviceStore } from '@/stores/deviceStore'

const deviceStore = useDeviceStore()

defineProps({
  visible: {
    required: true,
    type: Boolean
  }
})

const emit = defineEmits(['close'])

const title = computed(() => `Device Name: ${deviceStore.modalDeviceConfigBackup.deviceName}`)

const onVisibleChange = (val: boolean) => {
  if (!val) {
    emit('close')
  }
}
</script>

<style scoped lang="scss">
.content {
  min-height: 300px;
  min-width: 550px;
  position: relative;
}
</style>
