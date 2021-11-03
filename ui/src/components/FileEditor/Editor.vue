<template>
  <div :class="{ 'help-open' : isHelpOpen, 'help-closed': !isHelpOpen }">
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
import { ref, computed, watchEffect } from "vue"
import { useStore } from 'vuex'
import { VAceEditor } from 'vue3-ace-editor'
import Console from './Console.vue'
import ace from 'ace-builds'
import 'ace-builds/src-noconflict/mode-xml'
import 'ace-builds/src-noconflict/mode-properties'
import 'ace-builds/src-noconflict/theme-github'
import 'ace-builds/src-noconflict/theme-clouds_midnight'
import workerXmlUrl from 'ace-builds/src-noconflict/worker-xml?url'
ace.config.setModuleUrl('ace/mode/xml_worker', workerXmlUrl)

const theme = computed(() => {
  const theme = store.state.appModule.theme
  if (theme === 'open-dark') return 'clouds_midnight'
  return 'github'
})

const store = useStore()
const content = ref('')

const isHelpOpen = computed(() => store.state.fileEditorModule.isHelpOpen)
const fileString = computed(() => store.state.fileEditorModule.file)
const lang = computed(() => {
  const xml = 'xml', properties = 'properties'
  const selectedFileName = store.state.fileEditorModule.selectedFileName
  if (selectedFileName) {
    const splitSelectedFileName = selectedFileName.split('.')
    const filetype = splitSelectedFileName[splitSelectedFileName.length - 1]
    if (filetype !== xml) return properties
  }
  return xml
})
watchEffect(() => content.value = fileString.value)

const change = () => {
  store.dispatch('fileEditorModule/setIsFileContentModified', content.value !== fileString.value)
  store.dispatch('fileEditorModule/setModifiedFileString', content.value)
}

const init = (editor: any) => {
  editor.commands.addCommand({
    name: 'save',
    bindKey: { win: "Ctrl-S", "mac": "Cmd-S" },
    exec: () => store.dispatch('fileEditorModule/saveModifiedFile')
  })
}
</script>

<style lang="scss" scoped>
.editor-with-console {
  height: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--feather-border-on-surface)
}
.help-open {
  height: calc(100vh - 180px)
}
.help-closed {
  height: calc(100vh - 120px)
}
</style>
