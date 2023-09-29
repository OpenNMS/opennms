<template>
  <FeatherInput
    class="new-input"
    ref="input"
    label="New file name"
    @blur="addNewFile"
    @keyup.enter="addNewFile"
    v-model="newFileName"
  />
</template>

<script setup lang="ts">
import { FeatherInput } from '@featherds/input'
import { useFileEditorStore, IFile } from '@/stores/fileEditorStore'
import { getExtensionFromFilenameSafely } from './utils'
import { PropType } from 'vue'

const fileEditorStore = useFileEditorStore()
const input = ref()
const newFileName = ref('')
const allowedFileExtensions = computed(() => fileEditorStore.allowedFileExtensions)
const fileNames = computed(() => fileEditorStore.fileNames)

const props = defineProps({
  item: {
    required: true,
    type: Object as PropType<IFile>
  }
})

// eslint-disable-next-line vue/no-setup-props-destructure
const { item } = props

const addNewFile = () => {
  if (!item.isEditing) {
    return
  }

  item.isEditing = false

  if (!newFileName.value) {
    item.isHidden = true
    return
  }

  // check if file extension allowed
  const extension = getExtensionFromFilenameSafely(newFileName.value)

  if (!allowedFileExtensions.value.includes(extension)) {
    fileEditorStore.addLog({ success: false, msg: `File not added: ( ${newFileName.value} ) has an unsupported file extension.` })
    fileEditorStore.addLog({ success: false, msg: `File extensions include: ${allowedFileExtensions.value}` })
    fileEditorStore.setIsConsoleOpen(true)
    item.isHidden = true

    return
  }

  const fullPath = `${item.fullPath ? item.fullPath + '/' + newFileName.value : newFileName.value}`

  // check if it is a duplicated file name
  if (fileNames.value.includes(fullPath)) {
    fileEditorStore.addLog({ success: false, msg: 'File not added: Duplicate file names are not allowed.' })
    fileEditorStore.setIsConsoleOpen(true)
    item.isHidden = true

    return
  }

  // save to list of unsaved files, used when deleting before save
  fileEditorStore.addFileToUnsavedFilesList(fullPath)

  // set this new file as selected
  fileEditorStore.setSelectedFileName(fullPath)

  // clear editor contents
  fileEditorStore.clearEditor()

  // update store with new file
  fileEditorStore.saveNewFileToState(fullPath)

  // update the search input with the new file name
  fileEditorStore.setSearchValue(newFileName.value)
}

onMounted(() => input.value.focus())
</script>

<style lang="scss">
.new-input {
  padding-top: 0px !important;
  padding-bottom: 0px !important;

  .feather-input-wrapper-container {
    .feather-input-wrapper {
      margin-bottom: -25px !important;
      min-height: 31px !important;
    }
  }
}
</style>
