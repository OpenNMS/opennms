<template>
  <Search />
  <div class="sidebar-relative-container">
    <div class="file-tools">
      <FeatherButton
        v-if="changedFilesOnly"
        class="btn"
        icon="Click to show all files."
        @click="getFiles(false)"
      >
        <FeatherIcon :icon="FilterAlt" />
      </FeatherButton>

      <FeatherButton
        v-if="!changedFilesOnly"
        class="btn unfiltered"
        icon="Click to show modified files only."
        @click="getFiles(true)"
      >
        <FeatherIcon :icon="FilterAlt" />
      </FeatherButton>

      <FeatherButton
        class="btn"
        :disabled="!selectedFileName"
        icon="Scroll to selected file."
        @click="scrollToSelectedFile"
      >
        <FeatherIcon :icon="SupportCenter" />
      </FeatherButton>
    </div>
    <div class="file-sidebar">
      <ul>
        <FileTreeItem class="pointer" :item="treeData" />
      </ul>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { FeatherIcon } from '@featherds/icon'
import { FeatherButton } from '@featherds/button'
import FilterAlt from '@featherds/icon/action/FilterAlt'
import SupportCenter from '@featherds/icon/action/SupportCenter'
import { useFileEditorStore } from '@/stores/fileEditorStore'
import FileTreeItem from './FileTreeItem.vue'
import Search from './Search.vue'

const fileEditorStore = useFileEditorStore()
const changedFilesOnly = ref(false)
const treeData = computed(() => fileEditorStore.filesInFolders)
const selectedFileName = computed(() => fileEditorStore.selectedFileName)

const getFiles = (changedOnly: boolean) => {
  fileEditorStore.setChangedFilesOnly(changedOnly)
  fileEditorStore.getFileNames()

  changedFilesOnly.value = changedOnly
}
const scrollToSelectedFile = () => {
  const selected = document.getElementById('selected')

  if (selected) {
    selected.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}
</script>

<style lang="scss" scoped>
@import "@featherds/styles/themes/variables";
.sidebar-relative-container {
  position: relative;

  .file-sidebar {
    overflow-y: scroll;
    overflow-x: hidden;
    height: calc(100vh - 212px);
    word-break: break-all;
    border: 1px solid var($border-on-surface);

    ul {
      padding-left: 0px;
      margin-top: 5px;
    }
  }
  .file-tools {
    position: sticky;
    width: 100%;
    height: 30px;
    background: var($shade-4);

    .btn {
      margin: 0px;
      float: right;
      height: 25px !important;
      width: 25px !important;
      min-width: 25px !important;
      margin-top: 2px;
      svg {
        font-size: 20px !important;
      }

      &.unfiltered {
        color: var($shade-1);
      }
    }
  }
}
</style>
