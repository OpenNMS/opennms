<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="card">
        <div class="feather-row">
          <div class="feather-col-2">
            <Files />
          </div>
          <div class="feather-col-9">
            <div class="headline2">Configuration file editor</div>
            <VAceEditor
              v-model:value="content"
              @init="editorInit"
              lang="xml"
              theme="dracula"
              style="height: 300px"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from "vue"
import { useStore } from 'vuex'
import { VAceEditor } from 'vue3-ace-editor'
import ace from 'ace-builds'
import 'ace-builds/src-noconflict/mode-xml'
import 'ace-builds/src-noconflict/mode-properties'
import 'ace-builds/src-noconflict/theme-chrome'
import 'ace-builds/src-noconflict/theme-dracula'
import workerXmlUrl from 'ace-builds/src-noconflict/worker-xml?url'
import Files from '@/components/FileEditor/Files.vue'

const store = useStore()

ace.config.setModuleUrl('ace/mode/xml_worker', workerXmlUrl)

const content = ref('')
const fileString = computed(() => store.state.fileEditorModule.file)
watch(fileString, (fileString) => content.value = fileString)

const editorInit = (editor: any) => {
  editor.setShowPrintMargin(false)
}

onMounted(() => {
  store.dispatch('fileEditorModule/getFileNames')
})
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/mixins/elevation";
.card {
  @include elevation(2);
  margin: 15px;
  padding: 15px;
}
</style>
