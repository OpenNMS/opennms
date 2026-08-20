<template>
  <div class="editor">
    <div class="toolbar">
      <OnmsIconButton
        v-if="reverseLog"
        :disabled="!selectedLog"
        class="btn"
        aria-label="Display oldest first."
        :icon="KeyboardArrowDown"
        @click="getLog(false)"
      />

      <OnmsIconButton
        v-if="!reverseLog"
        :disabled="!selectedLog"
        class="btn"
        aria-label="Display newest first."
        :icon="KeyboardArrowUp"
        @click="getLog(true)"
      />
    </div>
    <VAceEditor
      v-model:value="content"
      lang="text"
      :theme="theme"
      style="height: 100%"
      :printMargin="false"
      @init="init"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watchEffect } from 'vue'

import { VAceEditor } from 'vue3-ace-editor'
import { OnmsIconButton } from '@opennms/onms-ui'
import { onKeyStroke } from '@vueuse/core'
import KeyboardArrowUp from '@opennms/onms-ui/icons/hardware/KeyboardArrowUp.vue'
import KeyboardArrowDown from '@opennms/onms-ui/icons/hardware/KeyboardArrowDown.vue'
import ace from 'ace-builds'
import 'ace-builds/src-noconflict/mode-text'
import 'ace-builds/src-noconflict/theme-xcode'
import 'ace-builds/src-noconflict/theme-dracula'
import 'ace-builds/src-noconflict/ext-searchbox'
import { useAppStore } from '@/stores/appStore'
import { useLogStore } from '@/stores/logStore'

const appStore = useAppStore()
const logStore = useLogStore()
const reverseLog = ref(false)
const content = ref('')
const logString = computed(() => logStore.log)
const selectedLog = computed(() => logStore.selectedLog)
const editorRef = ref()

const theme = computed(() => {
  const theme = appStore.theme
  if (theme === 'open-dark') {
    return 'dracula'
  }

  return 'xcode'
})

onKeyStroke('f', (e) => {
  if (e.ctrlKey || e.metaKey) {
    e.preventDefault()
    editorRef.value.searchBox.show()
  }
})

const getLog = (reverse: boolean) => {
  logStore.setReverseLog(reverse)
  logStore.getLog(selectedLog.value)
  reverseLog.value = reverse
}

watchEffect(() => content.value = logString.value)
const init = (editor: any) => {
  // activate and hide seach box
  ace.config.loadModule('ace/ext/searchbox', (m: any) => m.Search(editor))
  editor.searchBox.hide()

  editor.setFontSize(15)
  editor.setOptions({ readOnly: true })
  editor.renderer.setShowGutter(false)
  editor.renderer.$cursorLayer.element.style.display = 'none'

  editorRef.value = editor
}
</script>

<style lang="scss" scoped>
@import "@/styles/onms-tokens";
// Fit the height the app shell leaves for page content, or the page pushes the
// footer past the bottom of the window and clips it. The shell takes the masthead
// (--onms-header-height) off the top and the footer band (41px: a 1.5rem line,
// 0.5rem of padding either side and a 1px top border) off the bottom.
//
// This page also stacks a breadcrumb row (51px) above the card the editor sits
// in, and that card pads it by 15px top and bottom.
.editor {
  height: calc(100vh - var(--onms-header-height, 3.75rem) - 41px - 51px - 30px);
  display: flex;
  flex-direction: column;
  border: 1px solid var(--p-content-border-color);

  .toolbar {
    display: block;
    width: 100%;
    height: 30px;
    background: var($shade-3);

    .btn {
      margin: 0px;
      float: right;
      height: 25px !important;
      width: 25px !important;
      min-width: 25px !important;
      margin-right: 5px;
      margin-top: 2px;
    }
  }
}
</style>
