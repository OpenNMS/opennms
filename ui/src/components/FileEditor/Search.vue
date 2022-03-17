<template>
  <div class="search-bar">
    <div class="search">
      <FeatherInput label="Search etc" :modelValue="searchValue" @update:modelValue="search" />
    </div>
    <div class="save">
      <FeatherButton :disabled="disableBtn" primary @click="save">Save</FeatherButton>
    </div>
    <div class="reset">
      <FeatherButton :disabled="disableBtn" primary @click="reset">Reset</FeatherButton>
    </div>
  </div>
  <hr />
</template>

<script setup lang="ts">
import { useStore } from 'vuex'
import { FeatherInput } from '@featherds/input'
import { FeatherButton } from '@featherds/button'

const store = useStore()

const contentModified = computed(() => store.state.fileEditorModule.contentModified)
const hasSelectedFile = computed(() => store.state.fileEditorModule.selectedFileName !== '')
const searchValue = computed(() => store.state.fileEditorModule.searchValue)
const disableBtn = computed(() => !contentModified.value || !hasSelectedFile.value)
const search = (val: string) => store.dispatch('fileEditorModule/setSearchValue', val || '')
const reset = () => store.dispatch('fileEditorModule/triggerFileReset')
const save = () => store.dispatch('fileEditorModule/saveModifiedFile')
</script>

<style scoped lang="scss">
.search-bar {
  display: flex;
  .search {
    width: 100%;
    .feather-input-container {
      padding: 0px;
      margin-top: -8px;
    }
  }
  .save,
  .reset {
    margin-left: 10px;
    button {
      margin-top: 5px;
      margin-bottom: 0px;
    }
  }
}
</style>
