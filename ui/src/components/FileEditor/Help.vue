<template>
  <div class="help-bar">
    <OnmsIcon :icon="Close" @click="triggerHelp" class="pointer" />
    <div class="snippets" v-html="snippets"></div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import { useFileEditorStore } from '@/stores/fileEditorStore'
import OnmsIcon from '@/components/icons/OnmsIcon.vue'
import Close from '@/components/icons/navigation/Cancel.vue'

const fileEditorStore = useFileEditorStore()
const snippets = computed(() => fileEditorStore.snippets)
const triggerHelp = () => fileEditorStore.setIsHelpOpen(false)
</script>

<style lang="scss" scoped>
.help-bar {
  height: calc(100vh - 200px);
  border: 1px solid var(--p-content-border-color);
  padding: 10px;
  overflow: auto;
  .onms-icon {
    position: absolute;
    right: 35px;
    color: var(--p-text-color)
  }
  .snippets {
    padding: 15px;
    color: var(--p-text-color);
  }
}
</style>
