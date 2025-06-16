<template>
  <div class="help-bar">
    <FeatherIcon :icon="Close" @click="triggerHelp" class="pointer" />
    <div class="snippets" v-html="snippets"></div>
  </div>
</template>

<script setup lang="ts">
import { useFileEditorStore } from '@/stores/fileEditorStore'
import { FeatherIcon } from '@featherds/icon'
import Close from '@featherds/icon/navigation/Cancel'

const fileEditorStore = useFileEditorStore()
const snippets = computed(() => fileEditorStore.snippets)
const triggerHelp = () => fileEditorStore.setIsHelpOpen(false)
</script>

<style lang="scss" scoped>
@import "@featherds/styles/themes/variables";
.help-bar {
  height: calc(100vh - 200px);
  border: 1px solid var($border-on-surface);
  padding: 10px;
  overflow: auto;
  .feather-icon {
    position: absolute;
    right: 35px;
    color: var($primary-text-on-surface)
  }
  .snippets {
    padding: 15px;
    color: var($primary-text-on-surface);
  }
}
</style>
