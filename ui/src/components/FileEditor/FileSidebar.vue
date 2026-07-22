<template>
  <Search />
  <div class="sidebar-relative-container">
    <div class="file-tools">
     <PButton
        v-if="changedFilesOnly"
        text
        class="btn"
        aria-label="Click to show all files."
        v-tooltip="'Click to show all files.'"
        @click="getFiles(false)"
      >
        <OnmsIcon :icon="FilterAlt" />
      </PButton>

      <PButton
        v-if="!changedFilesOnly"
        text
        class="btn unfiltered"
        aria-label="Click to show modified files only."
        v-tooltip="'Click to show modified files only.'"
        @click="getFiles(true)"
      >
        <OnmsIcon :icon="FilterAlt" />
      </PButton>

      <PButton
        text
        class="btn"
        :disabled="!selectedFileName"
        aria-label="Scroll to selected file."
        v-tooltip="'Scroll to selected file.'"
        @click="scrollToSelectedFile"
      >
        <OnmsIcon :icon="SupportCenter" />
      </PButton>

      <PButton
        text
        class="btn"
        aria-label="Click for info."
        v-tooltip="'Click for info.'"
        @click="showInfo"
      >
        <OnmsIcon
          :icon="InfoIcon"
          class="info-icon"
        />
      </PButton>
    </div>
    <div class="file-sidebar">
      <ul>
        <FileTreeItem class="pointer" :item="treeData" />
      </ul>
    </div>
  </div>
  <MessageDialog
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
  </MessageDialog>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue'
import OnmsIcon from '@/components/icons/OnmsIcon.vue'
import FilterAlt from '@/components/icons/action/FilterAlt.vue'
import SupportCenter from '@/components/icons/action/SupportCenter.vue'
import InfoIcon from '@/components/icons/action/Info.vue'
import Button from 'primevue/button'
import MessageDialog from '../Common/MessageDialog.vue'
import { useFileEditorStore } from '@/stores/fileEditorStore'
import FileTreeItem from './FileTreeItem.vue'
import Search from './Search.vue'

const PButton = Button

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
      svg {
        font-size: 2em !important;
      }

      &.unfiltered {
        color: var(--p-text-muted-color);
      }
    }
  }

  .info-icon {
    cursor: pointer;
    font-size: 1.5em;
    margin-left: 0.5em;

    &:hover {
      opacity: 0.8;
    }
  }
}
</style>
