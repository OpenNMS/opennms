<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="onms-row">
    <div class="onms-col-12">
      <div class="card">
        <TopBar v-if="isHelpOpen" />

        <div class="onms-row">
          <transition name="fade">
            <div class="onms-col-3" v-if="!isHelpOpen">
              <FileSidebar />
            </div>
          </transition>

          <div :class="`onms-col-${isHelpOpen ? 8 : 9}`">
            <Editor />
          </div>

          <transition name="fade">
            <div class="onms-col-4" v-if="isHelpOpen">
              <Help />
            </div>
          </transition>

          <PButton
            v-if="!isHelpOpen && snippets"
            class="help-btn"
            text
            @click="triggerHelp">
            Help
          </PButton>
        </div>
      </div>
    </div>
  </div>
  <ConfirmationDialog
    :visible="Boolean(fileEditorStore.fileToDelete)"
    title="Delete confirmation"
    action-button-text="Confirm"
    @cancel="fileEditorStore.setFileToDelete(null)"
    @ok="fileEditorStore.deleteFile(fileEditorStore.fileToDelete?.fullPath || '')"
  >
    <template #content>
      <p>Delete {{ fileEditorStore.fileToDelete?.name }}?</p>
    </template>
  </ConfirmationDialog>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'

import Button from 'primevue/button'
import ConfirmationDialog from '@/components/Common/ConfirmationDialog.vue'
import Editor from '@/components/FileEditor/Editor.vue'
import FileSidebar from '@/components/FileEditor/FileSidebar.vue'
import Help from '@/components/FileEditor/Help.vue'
import TopBar from '@/components/FileEditor/TopBar.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { useFileEditorStore } from '@/stores/fileEditorStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'

const PButton = Button

const fileEditorStore = useFileEditorStore()
const menuStore = useMenuStore()

const isHelpOpen = computed(() => fileEditorStore.isHelpOpen)
const snippets = computed(() => fileEditorStore.snippets)
const triggerHelp = () => fileEditorStore.setIsHelpOpen(true)

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'File Editor', to: '#', position: 'last' }
  ]
})

onMounted(() => {
  fileEditorStore.getFileNames()
  fileEditorStore.getFileExtensions()
})
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/mixins/elevation";

.card {
  @include elevation(2);
  background: var(--p-content-background);
  padding: 15px;
  position: relative;
}
.help-btn {
  position: absolute;
  right: 30px;
  top: 0px;
}
.fade-enter-active {
  transition: opacity 0.7s ease;
}

.fade-enter-from {
  opacity: 0;
}
</style>
