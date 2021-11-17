<template>
  <Search />
  <div class="file-sidebar">
    <div class="file-tools">
      <FeatherButton
        v-if="changedFilesOnly"
        class="btn"
        icon="Show all files"
        @click="getChangedFilesOnly(false)"
      >
        <FeatherIcon :icon="UnfoldMore" />
      </FeatherButton>

      <FeatherButton
        v-if="!changedFilesOnly"
        class="btn"
        icon="Show modified files only"
        @click="getChangedFilesOnly(true)"
      >
        <FeatherIcon :icon="UnfoldLess" />
      </FeatherButton>
    </div>
    <ul>
      <FileTreeItem class="pointer" :item="treeData" />
    </ul>
  </div>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue'
import { useStore } from 'vuex'
import FileTreeItem from './FileTreeItem.vue'
import Search from './Search.vue'
import { FeatherIcon } from '@featherds/icon'
import UnfoldLess from '@featherds/icon/navigation/UnfoldLess'
import UnfoldMore from '@featherds/icon/navigation/UnfoldMore'
import { FeatherButton } from "@featherds/button"

const store = useStore()
const changedFilesOnly = ref(false)
const treeData = computed(() => store.state.fileEditorModule.filesInFolders)
const getChangedFilesOnly = (changedOnly: boolean) => {
  store.dispatch('fileEditorModule/setChangedFilesOnly', changedOnly)
  store.dispatch('fileEditorModule/getFileNames')
  changedFilesOnly.value = changedOnly
}
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
.file-tools {
  display: block;
  height: 30px;
  background: var(--feather-shade-4);
}
.btn {
  margin: 0px;
  float: right;
  height: 25px !important;
  width: 25px !important;
  min-width: 25px !important;
  margin-right: 5px;
  margin-top: 2px;
  svg {
    font-size: 20px !important;
  }
}
</style>
