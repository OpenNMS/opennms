<template>
  <OnmsIconButton
    text
    title="Download"
    aria-label="Download"
    aria-haspopup="true"
    aria-controls="node-download-menu"
    class="node-download-dropdown"
    data-test="download-button"
    :icon="downloadIcon"
    @click="toggle"
  />
  <Menu
    id="node-download-menu"
    ref="menu"
    :model="items"
    popup
  />
</template>

<script setup lang="ts">
import Menu from 'primevue/menu'
import type { MenuItem } from 'primevue/menuitem'
import Download from '@/components/icons/action/DownloadFile.vue'
import OnmsIconButton from '@/components/Common/OnmsIconButton.vue'
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
