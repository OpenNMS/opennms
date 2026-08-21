<template>
  <OnmsDrawer
    :visible="visible"
    position="right"
    :header="fileName"
    width="70em"
    data-test="mib-file-viewer"
    @update:visible="(value: boolean) => !value && emit('close')"
  >
    <pre
      class="mib-content"
      data-test="mib-file-content"
    >{{ content }}</pre>
  </OnmsDrawer>
</template>

<script lang="ts" setup>
import { OnmsDrawer } from '@opennms/onms-ui'

defineProps<{
  visible: boolean
  fileName: string
  content: string
}>()

const emit = defineEmits<{
  close: []
}>()
</script>

<style lang="scss" scoped>
.mib-content {
  font-family: monospace;
  font-size: 13px;
  white-space: pre;
  overflow: auto;
  height: 100%;
  margin: 0;
  padding: 10px;
  border: 1px solid var(--p-content-border-color);
  border-radius: 4px;
}
</style>
