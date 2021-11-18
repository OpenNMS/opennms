<template>
  <FeatherInput
    class="new-input"
    ref="input"
    label="New file name"
    @blur="addNewFile"
    @keyup.enter="addNewFile"
    v-model="newFileName"
    :error="fileNameError"
  />
</template>

<script setup lang=ts>
import { ref, onMounted, PropType, computed } from 'vue'
import { useStore } from 'vuex'
import { FeatherInput } from '@featherds/input'
import { IFile } from "@/store/fileEditor/state"
import { getExtensionFromFilenameSafely } from './utils'

const store = useStore()
const input = ref()
const newFileName = ref('')
const fileNameError = ref('')
const allowedFileExtensions = computed(() => store.state.fileEditorModule.allowedFileExtensions)

const props = defineProps({
  item: {
    required: true,
    type: Object as PropType<IFile>
  }
})
const { item } = props

const addNewFile = () => {
  // clear any previous errors
  fileNameError.value = ''

  if (!item.isEditing) return
  item.isEditing = false

  if (!newFileName.value) {
    item.isHidden = true
    return
  }

  // check if file extension allowed
  const extension = getExtensionFromFilenameSafely(newFileName.value)
  if (!allowedFileExtensions.value.includes(extension)) {
    fileNameError.value = 'Unsupported file extension.'
    item.isEditing = true // leave editing on
  }

  const fullPath = `${item.fullPath ? item.fullPath + '/' + newFileName.value : newFileName.value}`

  if (!item.isEditing) {
    // set newly created file as selected
    store.dispatch('fileEditorModule/setSelectedFileName', fullPath)
    // clear editor contents
    store.dispatch('fileEditorModule/clearEditor')
    // update vuex store with new file
    store.dispatch('fileEditorModule/addNewFileToState', fullPath)
    // update the search input with the new file name
    store.dispatch('fileEditorModule/setSearchValue', newFileName.value)
  }
}

onMounted(() => input.value.focus())
</script>

<style lang="scss">
.new-input {
  padding-top: 0px !important;
  padding-bottom: 0px !important;

  .feather-input-wrapper-container {
    .feather-input-wrapper {
      margin-top: -10px !important;
      min-height: 31px !important;
    }
  }
}
</style>
