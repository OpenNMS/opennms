<template>
  <div class="editor">
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
import { ref, computed, watchEffect } from "vue"
import { useStore } from 'vuex'
import { VAceEditor } from 'vue3-ace-editor'
import 'ace-builds/src-noconflict/mode-text'
import 'ace-builds/src-noconflict/theme-github'
import 'ace-builds/src-noconflict/theme-clouds_midnight'

const theme = computed(() => {
  const theme = store.state.appModule.theme
  if (theme === 'open-dark') return 'clouds_midnight'
  return 'github'
})

const store = useStore()
const content = ref('')
const logString = computed(() => store.state.logsModule.log)

watchEffect(() => content.value = logString.value)
const init = (editor: any) => { 
  editor.setOptions({ readOnly: true })
  editor.renderer.setShowGutter(false)
  editor.renderer.$cursorLayer.element.style.display = "none"
}
</script>

<style lang="scss" scoped>
.editor {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
  border: 1px solid var(--feather-border-on-surface);
}
</style>
