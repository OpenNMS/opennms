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
import { FeatherInput } from '@featherds/input'
import { FeatherButton } from '@featherds/button'
import { useFileEditorStore } from '@/stores/fileEditorStore'
import { UpdateModelFunction } from '@/types'

const fileEditorStore = useFileEditorStore()

const contentModified = computed(() => fileEditorStore.contentModified)
const hasSelectedFile = computed(() => fileEditorStore.selectedFileName !== '')
const searchValue = computed(() => fileEditorStore.searchValue)
const disableBtn = computed(() => !contentModified.value || !hasSelectedFile.value)

const search: UpdateModelFunction = (val: string) => fileEditorStore.setSearchValue(val || '')
const reset = () => fileEditorStore.triggerFileReset()
const save = () => fileEditorStore.saveModifiedFile()
</script>

<style scoped lang="scss">
.search-bar {
  display: flex;
  .search {
    width: 100%;
    .feather-input-container {
      padding: 0px;
      margin-bottom: -26px;
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
