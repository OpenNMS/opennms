<template>
  <div class="search-bar">
    <div class="search">
      <FeatherInput label="Search files" v-model="searchValue" @update:modelValue="search" />
    </div>
    <div class="save">
      <FeatherButton :disabled="!contentModified" primary @click="save">Save</FeatherButton>
    </div>
    <div class="reset">
      <FeatherButton :disabled="!contentModified" primary @click="reset">Reset</FeatherButton>
    </div>
  </div>
  <hr />
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { FeatherInput } from '@featherds/input'
import { FeatherButton } from '@featherds/button'

const store = useStore()
const searchValue = ref('')

const contentModified = computed(() => store.state.fileEditorModule.contentModified)
const search = () => store.dispatch('fileEditorModule/setSearchValue', searchValue.value)
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
    }
  }
}
</style>
