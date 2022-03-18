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

<script setup lang=ts>
import { useStore } from 'vuex'
import { FeatherInput } from '@featherds/input'
import { IFile } from '@/store/fileEditor/state'
import { getExtensionFromFilenameSafely } from './utils'
import { PropType } from 'vue'

const store = useStore()
const input = ref()
const newFileName = ref('')
const allowedFileExtensions = computed(() => store.state.fileEditorModule.allowedFileExtensions)
const fileNames = computed(() => store.state.fileEditorModule.fileNames)

const props = defineProps({
  item: {
    required: true,
    type: Object as PropType<IFile>
  }
})
// eslint-disable-next-line vue/no-setup-props-destructure
const { item } = props

const addNewFile = () => {
  if (!item.isEditing) return
  item.isEditing = false

  if (!newFileName.value) {
    item.isHidden = true
    return
  }

  // check if file extension allowed
  const extension = getExtensionFromFilenameSafely(newFileName.value)
  if (!allowedFileExtensions.value.includes(extension)) {
    store.dispatch('fileEditorModule/addLog', { success: false, msg: `File not added: ( ${newFileName.value} ) has an unsupported file extension.` })
    store.dispatch('fileEditorModule/addLog', { success: false, msg: `File extensions include: ${allowedFileExtensions.value}` })
    store.dispatch('fileEditorModule/setIsConsoleOpen', true)
    item.isHidden = true
    return
  }

  const fullPath = `${item.fullPath ? item.fullPath + '/' + newFileName.value : newFileName.value}`

  // check if it is a duplicated file name
  if (fileNames.value.includes(fullPath)) {
    store.dispatch('fileEditorModule/addLog', { success: false, msg: 'File not added: Duplicate file names are not allowed.' })
    store.dispatch('fileEditorModule/setIsConsoleOpen', true)
    item.isHidden = true
    return
  }

  // save to list of unsaved files, used when deleting before save
  store.dispatch('fileEditorModule/addFileToUnsavedFilesList', fullPath)
  // set this new file as selected
  store.dispatch('fileEditorModule/setSelectedFileName', fullPath)
  // clear editor contents
  store.dispatch('fileEditorModule/clearEditor')
  // update vuex store with new file
  store.dispatch('fileEditorModule/addNewFileToState', fullPath)
  // update the search input with the new file name
  store.dispatch('fileEditorModule/setSearchValue', newFileName.value)
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
