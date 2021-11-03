<template>
  <Search />
  <div class="file-sidebar">
    <p
      :class="{ 'selected': filename === selectedFile }"
      class="pointer"
      v-for="(filename, index) in fileNames"
      :key="filename"
      @click="getFile(filename)"
    >
      <span class="subtitle1">
        {{ Number(index) + 1 }}:&nbsp
      </span>
      <span class="subtitle2">
        {{ filename }}
      </span>
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
  margin: 0px;
  padding: 5px;
  padding-left: 10px;
}
.file-sidebar {
  overflow-y: scroll;
  overflow-x: hidden;
  height: calc(100vh - 182px);
  word-break: break-all;
  border: 1px solid var(--feather-border-on-surface)
}
.selected {
  background: var(--feather-shade-3);
  span {
    color: var(--feather-primary);
  }
}
</style>
