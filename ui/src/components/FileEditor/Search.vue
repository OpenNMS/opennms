<template>
  <div class="search-bar">
    <div class="search">
      <FormField class="search-field">
        <IconField>
          <OnmsInputText
            placeholder="Search"
            aria-label="Search"
            :modelValue="searchValue"
            @update:modelValue="(val) => search(val as string)"
          />
          <InputIcon>
            <OnmsIcon :icon="IconSearch" />
          </InputIcon>
        </IconField>
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

import { OnmsButton, OnmsIcon, OnmsInputText } from '@opennms/onms-ui'
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import IconSearch from '@/components/icons/action/Search.vue'
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

      // make the input (and its IconField wrapper) fill the field so the
      // search icon sits at the input's right edge
      :deep(.p-iconfield) {
        display: block;
        width: 100%;
      }

      :deep(.p-inputtext) {
        width: 100%;
        padding-right: 2.75rem;
      }
    }
  }
  .save,
  .reset {
    align-content: center;
    margin-left: 10px;
  }
}
</style>
