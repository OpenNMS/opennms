<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="card">
        <TopBar v-if="isHelpOpen" />

        <div class="feather-row">
          <transition name="fade">
            <div class="feather-col-3" v-if="!isHelpOpen">
              <FileSidebar />
            </div>
          </transition>

          <div :class="`feather-col-${isHelpOpen ? 8 : 9}`">
            <Editor />
          </div>

          <transition name="fade">
            <div class="feather-col-4" v-if="isHelpOpen">
              <Help />
            </div>
          </transition>

          <FeatherButton
            v-if="!isHelpOpen && snippets"
            class="help-btn"
            text
            @click="triggerHelp">
            Help
          </FeatherButton>
        </div>
      </div>
    </div>
  </div>
  <ConfirmDialog />
</template>

<script setup lang="ts">
import { FeatherButton } from '@featherds/button'
import Editor from '@/components/FileEditor/Editor.vue'
import FileSidebar from '@/components/FileEditor/FileSidebar.vue'
import Help from '@/components/FileEditor/Help.vue'
import TopBar from '@/components/FileEditor/TopBar.vue'
import ConfirmDialog from '@/components/FileEditor/ConfirmDialog.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import { useFileEditorStore } from '@/stores/fileEditorStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'

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
@import "@featherds/styles/themes/variables";

.card {
  @include elevation(2);
  background: var($surface);
  padding: 15px;
  position: relative;
}
.feather-row {
  flex-wrap: nowrap;
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
