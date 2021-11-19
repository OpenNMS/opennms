<template>
  <Search />
  <div class="sidebar-relative-container">
    <div class="file-tools">
      <FeatherButton
        v-if="changedFilesOnly"
        class="btn"
        icon="Show all files"
        @click="getFles(false)"
      >
        <FeatherIcon :icon="UnfoldMore" />
      </FeatherButton>

      <FeatherButton
        v-if="!changedFilesOnly"
        class="btn"
        icon="Show modified files only"
        @click="getFles(true)"
      >
        <FeatherIcon :icon="UnfoldLess" />
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
import { computed, ref } from 'vue'
import { useStore } from 'vuex'
import FileTreeItem from './FileTreeItem.vue'
import Search from './Search.vue'
import { FeatherIcon } from '@featherds/icon'
import { FeatherButton } from "@featherds/button"
import UnfoldLess from '@featherds/icon/navigation/UnfoldLess'
import UnfoldMore from '@featherds/icon/navigation/UnfoldMore'
import SupportCenter from '@featherds/icon/action/SupportCenter'

const store = useStore()
const changedFilesOnly = ref(false)
const treeData = computed(() => store.state.fileEditorModule.filesInFolders)
const selectedFileName = computed(() => store.state.fileEditorModule.selectedFileName)
const getFles = (changedOnly: boolean) => {
  store.dispatch('fileEditorModule/setChangedFilesOnly', changedOnly)
  store.dispatch('fileEditorModule/getFileNames')
  changedFilesOnly.value = changedOnly
}
const scrollToSelectedFile = () => {
  const selected = document.getElementById("selected")
  if (selected) {
    selected.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}
</script>

<style lang="scss" scoped>
.sidebar-relative-container {
  position: relative;

  .file-sidebar {
    overflow-y: scroll;
    overflow-x: hidden;
    height: calc(100vh - 212px);
    word-break: break-all;
    border: 1px solid var(--feather-border-on-surface);

    ul {
      padding-left: 0px;
      margin-top: 5px;
    }
  }
  .file-tools {
    position: sticky;
    width: 100%;
    height: 30px;
    background: var(--feather-shade-4);

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
    }
  }
}
</style>
