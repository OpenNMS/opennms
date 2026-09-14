<template>
  <div class="search-bar">
    <div class="search">
      <FormField class="search-field">
        <OnmsSearchInput
          placeholder="Search"
          aria-label="Search"
          :modelValue="searchValue"
          @update:modelValue="(val) => search(val as string)"
        />
      </FormField>
    </div>
    <div class="save">
      <OnmsButton :disabled="disableBtn" @click="save">Save</OnmsButton>
    </div>
    <div class="reset">
      <OnmsButton :disabled="disableBtn" @click="reset">Reset</OnmsButton>
    </div>
  </div>
  <hr />
</template>

<script setup lang="ts">
import { computed } from 'vue'

import { OnmsButton, OnmsSearchInput } from '@opennms/onms-ui'
import FormField from '@/components/Common/FormField.vue'
import { useFileEditorStore } from '@/stores/fileEditorStore'

const fileEditorStore = useFileEditorStore()

const contentModified = computed(() => fileEditorStore.contentModified)
const hasSelectedFile = computed(() => fileEditorStore.selectedFileName !== '')
const searchValue = computed(() => fileEditorStore.searchValue)
const disableBtn = computed(() => !contentModified.value || !hasSelectedFile.value)

const search = (val: string) => fileEditorStore.setSearchValue(val || '')
const reset = () => fileEditorStore.triggerFileReset()
const save = () => fileEditorStore.saveModifiedFile()
</script>

<style scoped lang="scss">
.search-bar {
  display: flex;

  .search {
    width: 100%;

    .search-field {
      width: 100%;
    }
  }
  .save,
  .reset {
    align-content: center;
    margin-left: 10px;
  }
}
</style>
