<template>
  <Search />
  <div class="file-sidebar">
    <p
      class="pointer"
      v-for="(filename, index) in fileNames"
      :key="filename"
      @click="getFile(filename)"
    >
      <span
        class="subtitle1"
        :class="{ 'selected': filename === selectedFile }"
      >{{ index + 1 }}:&nbsp</span>
      <span class="subtitle2" :class="{ 'selected': filename === selectedFile }">{{ filename }}</span>
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useStore } from 'vuex'
import Search from './Search.vue'

const store = useStore()
const selectedFile = computed(() => store.state.fileEditorModule.selectedFileName)
const fileNames = computed(() => store.getters['fileEditorModule/getFilteredFileNames'])
const getFile = (filename: string) => store.dispatch('fileEditorModule/getFile', filename)
</script>

<style lang="scss" scoped>
p {
  margin-top: 8px;
  margin-bottom: 8px;
}
.file-sidebar {
  overflow-y: scroll;
  overflow-x: hidden;
  height: calc(100vh - 200px);
  word-break: break-all;
}
.selected {
  color: var(--feather-primary);
}
</style>
