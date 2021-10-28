<template>
  <VAceEditor
    v-model:value="content"
    @change="change"
    :lang="lang"
    :theme="theme"
    style="height: calc(100vh - 120px)"
    :printMargin="false"
    :options="{ useWorker: true }"
  />
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue"
import { useStore } from 'vuex'
import { VAceEditor } from 'vue3-ace-editor'
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

const fileString = computed(() => store.state.fileEditorModule.file)
const lang = computed(() => {
  const selectedFileName = store.state.fileEditorModule.selectedFileName
  if (selectedFileName) return selectedFileName.split('.')[1]
  return 'xml'
})

watch(fileString, (fileString) => {
  console.log(fileString)
  content.value = fileString
})

const change = () => {
  store.dispatch('fileEditorModule/setIsFileContentModified', content.value !== fileString.value)
  store.dispatch('fileEditorModule/setModifiedFileString', content.value)
}
</script>
