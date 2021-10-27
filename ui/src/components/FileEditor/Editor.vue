<template>
  <VAceEditor
    v-model:value="content"
    @init="editorInit"
    @change="change"
    lang="xml"
    theme="dracula"
    style="height: calc(100vh - 120px)"
    :printMargin="false"
  />
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue"
import { useStore } from 'vuex'
import { VAceEditor } from 'vue3-ace-editor'
import ace from 'ace-builds'
import 'ace-builds/src-noconflict/mode-xml'
import 'ace-builds/src-noconflict/mode-properties'
import 'ace-builds/src-noconflict/theme-chrome'
import 'ace-builds/src-noconflict/theme-dracula'
import workerXmlUrl from 'ace-builds/src-noconflict/worker-xml?url'
ace.config.setModuleUrl('ace/mode/xml_worker', workerXmlUrl)

const store = useStore()
const content = ref('')
const fileString = computed(() => store.state.fileEditorModule.file)

watch(fileString, (fileString) => content.value = fileString)

const change = () => {
  store.dispatch('fileEditorModule/setIsFileContentModified', content.value !== fileString.value)
  store.dispatch('fileEditorModule/setModifiedFileString', content.value)
}

const editorInit = (editor: any) => { }
</script>
