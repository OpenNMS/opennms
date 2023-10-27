<template>
  <div :class="{ 'help-open': isHelpOpen, 'help-closed': !isHelpOpen }">
    <div class="editor-with-console">
      <VAceEditor
        v-model:value="content"
        @change="change"
        :lang="lang"
        :theme="theme"
        style="height: 100%"
        :printMargin="false"
        :options="{ useWorker: true }"
        @init="init"
      />
      <Console />
    </div>
  </div>
</template>

<script setup lang="ts">
import { getExtensionFromFilenameSafely } from './utils'
import { useAppStore } from '@/stores/appStore'
import { VAceEditor } from 'vue3-ace-editor'
import { onKeyStroke } from '@vueuse/core'
import Console from './Console.vue'
import ace from 'ace-builds'
import 'ace-builds/src-noconflict/mode-xml'
import 'ace-builds/src-noconflict/mode-java'
import 'ace-builds/src-noconflict/mode-properties'
import 'ace-builds/src-noconflict/theme-xcode'
import 'ace-builds/src-noconflict/theme-dracula'
import 'ace-builds/src-noconflict/ext-searchbox'
import workerXmlUrl from 'ace-builds/src-noconflict/worker-xml?url'
import { useFileEditorStore } from '@/stores/fileEditorStore'

ace.config.setModuleUrl('ace/mode/xml_worker', workerXmlUrl)

const appStore = useAppStore()
const fileEditorStore = useFileEditorStore()
const content = ref('')
const reactiveEditor = ref()

const theme = computed(() => {
  const theme = appStore.theme
  if (theme === 'open-dark') {
    return 'dracula'
  }

  return 'xcode'
})

const selectedFileName = computed(() => fileEditorStore.selectedFileName)
const isHelpOpen = computed(() => fileEditorStore.isHelpOpen)
const fileString = computed(() => fileEditorStore.file)

const lang = computed(() => {
  const xml = 'xml', properties = 'properties', drl = 'drl', java = 'java'

  if (selectedFileName.value) {
    const extension = getExtensionFromFilenameSafely(selectedFileName.value)
    if (extension === xml) return xml
    if (extension === drl) return java
  }
  return properties
})

const disableEditor = (editor: any) => {
  editor.setOptions({ readOnly: true })
  editor.renderer.setShowGutter(false)
  editor.renderer.$cursorLayer.element.style.display = 'none'
}

watchEffect(() => content.value = fileString.value)

watch(selectedFileName, (selectedFileName) => {
  // enable editor when file is selected
  if (selectedFileName) {
    reactiveEditor.value.setOptions({ readOnly: false })
    reactiveEditor.value.renderer.setShowGutter(true)
    reactiveEditor.value.renderer.$cursorLayer.element.style.display = 'block'
  } else {
    disableEditor(reactiveEditor.value)
  }
})

onKeyStroke('f', (e) => {
  if (e.ctrlKey || e.metaKey) {
    e.preventDefault()
    reactiveEditor.value.searchBox.show()
  }
})

const change = () => {
  fileEditorStore.setIsFileContentModified(content.value !== fileString.value)
  fileEditorStore.setModifiedFileString(content.value)
}

const init = (editor: any) => {
  // activate and hide seach box
  ace.config.loadModule('ace/ext/searchbox', (m: any) => m.Search(editor))
  editor.searchBox.hide()

  editor.commands.addCommand({
    name: 'save',
    bindKey: { win: 'Ctrl-S', 'mac': 'Cmd-S' },
    exec: () => fileEditorStore.saveModifiedFile()
  })

  editor.setFontSize(15)
  // disable editor on load
  disableEditor(editor)
  reactiveEditor.value = editor
}
</script>

<style lang="scss" scoped>
@import "@featherds/styles/themes/variables";
.editor-with-console {
  height: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid var($border-on-surface);
}
.help-open {
  height: calc(100vh - 180px);
}
.help-closed {
  height: calc(100vh - 120px);
}
</style>
