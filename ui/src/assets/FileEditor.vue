<template>
  <div class="file-editor">
    <FileEditor />
  </div>
</template>

<script setup lang="ts">
import FileEditor from './FileEditor.svg'
</script>

<style scoped lang="scss">
@import "@featherds/styles/themes/variables";
.file-editor {
  color: var($secondary-text-on-surface);
}
</style>
