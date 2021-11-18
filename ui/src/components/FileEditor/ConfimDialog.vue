<template>
  <FeatherDialog v-model="file" :labels="labels">
    <p class="subtitle2 dialog">Delete {{ file?.name }}?</p>

    <template v-slot:footer>
      <FeatherButton text @click="emit('closeModal')">Cancel</FeatherButton>
      <FeatherButton class="btn-delete" text @click="deleteFile">Confirm</FeatherButton>
    </template>
  </FeatherDialog>
</template>
<script setup lang=ts>
import { useStore } from 'vuex'
import { FeatherDialog } from "@featherds/dialog"
import { FeatherButton } from "@featherds/button"
import { PropType } from '@vue/runtime-core'
import { IFile } from '@/store/fileEditor/state'

const store = useStore()

const labels = {
  title: 'Delete confirmation',
  close: 'Close'
}

const props = defineProps({
  file: {
    type: Object as PropType<IFile | null>,
    required: true
  }
})

const emit = defineEmits(['closeModal'])
const deleteFile = () => {
  store.dispatch('fileEditorModule/deleteFile', props.file?.fullPath)
  emit('closeModal')
}
</script>

<style lang="scss" scoped>
.dialog {
  width: 300px;
}
.btn-delete {
  color: var(--feather-error);
}
</style>
