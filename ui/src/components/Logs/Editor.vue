<template>
  <div class="editor">
    <div class="toolbar">
      <FeatherButton
        v-if="reverseLog"
        :disabled="!selectedLog"
        class="btn"
        icon="Display log descending"
        @click="getLog(false)"
      >
        <FeatherIcon :icon="KeyboardArrowDown" />
      </FeatherButton>

      <FeatherButton
        v-if="!reverseLog"
        :disabled="!selectedLog"
        class="btn"
        icon="Display log acending"
        @click="getLog(true)"
      >
        <FeatherIcon :icon="KeyboardArrowUp" />
      </FeatherButton>
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
import { ref, computed, watchEffect } from "vue"
import { useStore } from 'vuex'
import { VAceEditor } from 'vue3-ace-editor'
import { FeatherIcon } from '@featherds/icon'
import { FeatherButton } from "@featherds/button"
import KeyboardArrowUp from '@featherds/icon/hardware/KeyboardArrowUp'
import KeyboardArrowDown from '@featherds/icon/hardware/KeyboardArrowDown'
import 'ace-builds/src-noconflict/mode-text'
import 'ace-builds/src-noconflict/theme-github'
import 'ace-builds/src-noconflict/theme-clouds_midnight'

const theme = computed(() => {
  const theme = store.state.appModule.theme
  if (theme === 'open-dark') return 'clouds_midnight'
  return 'github'
})

const store = useStore()
const reverseLog = ref(false)
const content = ref('')
const logString = computed(() => store.state.logsModule.log)
const selectedLog = computed(() => store.state.logsModule.selectedLog)

const getLog = (reverse: boolean) => {
  store.dispatch('logsModule/setReverseLog', reverse)
  store.dispatch('logsModule/getLog', selectedLog.value)
  reverseLog.value = reverse
}

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

  .toolbar {
    display: block;
    width: 100%;
    height: 30px;
    background: var(--feather-shade-3);

    .btn {
      margin: 0px;
      float: right;
      height: 25px !important;
      width: 25px !important;
      min-width: 25px !important;
      margin-right: 5px;
      margin-top: 2px;
      svg {
        font-size: 20px !important;
      }
    }
  }
}
</style>
