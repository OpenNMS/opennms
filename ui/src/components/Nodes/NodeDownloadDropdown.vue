<template>
  <FeatherDropdown class="node-download-dropdown">
    <template v-slot:trigger="{ attrs, on }">
      <FeatherButton
        icon="Download"
        v-bind="attrs"
        v-on="on"
      >
        <FeatherIcon :icon="downloadIcon" class="download-actions-icon" title="Download" />
      </FeatherButton>
    </template>
    <FeatherDropdownItem @click="onCsvDownload()">
      <span class="download-menu-item">Download CSV...</span>
    </FeatherDropdownItem>
    <FeatherDropdownItem @click="onJsonDownload()">
      <span class="download-menu-item">Download JSON...</span>
    </FeatherDropdownItem>
  </FeatherDropdown>
</template>

<script setup lang="ts">
import { FeatherButton } from '@featherds/button'
import { FeatherDropdown, FeatherDropdownItem } from '@featherds/dropdown'
import { FeatherIcon } from '@featherds/icon'
import Download from '@featherds/icon/action/DownloadFile'
import { markRaw, PropType } from 'vue'

defineProps({
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
</script>

<style lang="scss" scoped>
.download-menu-item {
  padding: 1em;
}

button.btn.btn-icon .download-actions-icon {
  font-size: 1.1rem;
}

.node-download-dropdown {
  text-align: left;
}
</style>
