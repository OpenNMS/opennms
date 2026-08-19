<template>
  <Search />
  <div class="sidebar-relative-container">
    <div class="file-tools">
     <OnmsIconButton
        v-if="changedFilesOnly"
        class="btn"
        aria-label="Click to show all files."
        v-onms-tooltip="'Click to show all files.'"
        :icon="FilterAlt"
        :icon-size="'2em'"
        @click="getFiles(false)"
      />

      <OnmsIconButton
        v-if="!changedFilesOnly"
        class="btn unfiltered"
        aria-label="Click to show modified files only."
        v-onms-tooltip="'Click to show modified files only.'"
        :icon="FilterAlt"
        :icon-size="'2em'"
        @click="getFiles(true)"
      />

      <OnmsIconButton
        class="btn"
        :disabled="!selectedFileName"
        aria-label="Scroll to selected file."
        v-onms-tooltip="'Scroll to selected file.'"
        :icon="SupportCenter"
        :icon-size="'2em'"
        @click="scrollToSelectedFile"
      />

      <OnmsIconButton
        class="btn info-icon"
        aria-label="Click for info."
        v-onms-tooltip="'Click for info.'"
        :icon="InfoIcon"
        :icon-size="'2em'"
        @click="showInfo"
      />
    </div>
    <div class="file-sidebar">
      <ul>
        <FileTreeItem class="pointer" :item="treeData" />
      </ul>
    </div>
  </div>
  <OnmsMessageDialog
    :visible="isMessageDialogVisible"
    :relative="true"
    maxHeight="22em"
    maxWidth="50em"
    title="File System Editor"
    @close="isMessageDialogVisible = false"
  >
    <template #content>
      <div>
        <p>This is the file system editor. Here you can view and manage your files.</p>
        <br />
        <p><strong>Access</strong></p>
        <p>You must have the <code>ROLE_FILESYSTEM_EDITOR</code> role to access the file system editor.</p>
        <br />
        <p><strong>Show modified files only</strong></p>
        <p>This toggles whether modified or non-modified files are displayed.</p>
        <p>To count as "modified", the file must exist in the OpenNMS <code>etc-pristine</code> folder (usually under <code>share</code>), and differ from the pristine version.</p>
        <br />
        <p><strong>Save/Reset</strong></p>
        <p>Use the Save button to save changes to the selected file. Use the Reset button to discard changes and revert the file to its previous state.</p>
      </div>
    </template>
  </OnmsMessageDialog>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue'
import { OnmsIconButton, OnmsMessageDialog } from '@opennms/onms-ui'
import FilterAlt from '@opennms/onms-ui/icons/action/FilterAlt.vue'
import SupportCenter from '@opennms/onms-ui/icons/action/SupportCenter.vue'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'
import { useFileEditorStore } from '@/stores/fileEditorStore'
import FileTreeItem from './FileTreeItem.vue'
import Search from './Search.vue'

const isMessageDialogVisible = ref(false)
const fileEditorStore = useFileEditorStore()
const changedFilesOnly = ref(false)
const treeData = computed(() => fileEditorStore.filesInFolders)
const selectedFileName = computed(() => fileEditorStore.selectedFileName)

const getFiles = (changedOnly: boolean) => {
  fileEditorStore.setChangedFilesOnly(changedOnly)
  fileEditorStore.getFileNames()

  changedFilesOnly.value = changedOnly
}

const showInfo = () => {
  isMessageDialogVisible.value = true
}

const scrollToSelectedFile = () => {
  const selected = document.getElementById('selected')

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
    border: 1px solid var(--p-content-border-color);

    ul {
      padding-left: 0px;
      margin-top: 5px;
    }
  }

  .file-tools {
    position: sticky;
    width: 100%;
    height: 2.8em;
    background: var(--p-datatable-header-cell-background);

    .btn {
      margin: 0px;
      float: right;
      height: 2em !important;
      width: 2.8em !important;
      min-width: 2.8em !important;
      margin-top: 2px;

      &.unfiltered {
        color: var(--p-text-muted-color);
      }
    }
  }

  .info-icon {
    cursor: pointer;
    margin-left: 0.5em;

    &:hover {
      opacity: 0.8;
    }
  }
}
</style>
