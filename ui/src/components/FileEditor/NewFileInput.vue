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
import { ref, onMounted, PropType } from 'vue'
import { useStore } from 'vuex'
import { FeatherInput } from '@featherds/input'
import { IFile } from "@/store/fileEditor/state"

const store = useStore()
const input = ref()
const newFileName = ref('')

const props = defineProps({
  item: {
    required: true,
    type: Object as PropType<IFile>
  }
})
const { item } = props

const addNewFile = () => {
  if (!item.isEditing) return
  item.isEditing = false
  if (!newFileName.value) return
  const fullPath = `${item.fullPath ? item.fullPath + '/' + newFileName.value : newFileName.value}`

    // set newly created file as selected
  store.dispatch('fileEditorModule/setSelectedFileName', fullPath)
    // clear editor contents
  store.dispatch('fileEditorModule/clearEditor')
  // update vuex store with new file
  store.dispatch('fileEditorModule/addNewFileToState', fullPath)
}

onMounted(() => input.value.focus())
</script>

<style lang="scss">
.new-input {
  padding-top: 0px;
  padding-bottom: 0px;

  .feather-input-wrapper-container {
    .feather-input-wrapper {
      margin-top: -10px !important;
      min-height: 31px !important;
    }
  }
}
</style>
