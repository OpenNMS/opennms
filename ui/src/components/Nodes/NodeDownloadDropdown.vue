<template>
  <Button
    text
    title="Download"
    aria-label="Download"
    aria-haspopup="true"
    aria-controls="node-download-menu"
    class="node-download-dropdown"
    data-test="download-button"
    @click="toggle"
  >
    <FeatherIcon
      :icon="downloadIcon"
      class="download-actions-icon"
      title="Download"
    />
  </Button>
  <Menu
    id="node-download-menu"
    ref="menu"
    :model="items"
    popup
  />
</template>

<script setup lang="ts">
import Button from 'primevue/button'
import Menu from 'primevue/menu'
import type { MenuItem } from 'primevue/menuitem'
import { FeatherIcon } from '@featherds/icon'
import Download from '@featherds/icon/action/DownloadFile'
import { markRaw, ref, PropType } from 'vue'

const props = defineProps({
  onCsvDownload: {
    required: true,
    type: Function as PropType<() => void>
  },
  onJsonDownload: {
    required: true,
    type: Function as PropType<() => void>
  }
})

const downloadIcon = markRaw(Download)
const menu = ref()

const items = ref<MenuItem[]>([
  { label: 'Download CSV...', command: () => props.onCsvDownload() },
  { label: 'Download JSON...', command: () => props.onJsonDownload() }
])

const toggle = (event: Event) => {
  menu.value?.toggle(event)
}

defineExpose({ items })
</script>

<style lang="scss" scoped>
.download-actions-icon {
  font-size: 1.1rem;
}
</style>
