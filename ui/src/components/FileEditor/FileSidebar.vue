<template>
  <Search />
  <div class="file-sidebar">
    <ul>
      <FileTreeItem class="pointer" :item="treeData" />
    </ul>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { useStore } from 'vuex'
import FileTreeItem from './FileTreeItem.vue'
import Search from './Search.vue'

const store = useStore()
const treeData = computed(() => store.state.fileEditorModule.filesInFolders)
</script>

<style lang="scss" scoped>
ul {
  padding-left: 0px;
}
p {
  margin: 0px;
  padding: 5px;
  padding-left: 10px;
}
.file-sidebar {
  overflow-y: scroll;
  overflow-x: hidden;
  height: calc(100vh - 182px);
  word-break: break-all;
  border: 1px solid var(--feather-border-on-surface);
}
</style>
