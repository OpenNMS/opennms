<template>
  <FeatherDialog v-model="open" :labels="labels">
    <p class="subtitle2 dialog">Delete {{ file?.name }}?</p>

    <template v-slot:footer>
      <FeatherButton text @click="cancel">Cancel</FeatherButton>
      <FeatherButton class="btn-delete" text @click="deleteFile">Confirm</FeatherButton>
    </template>
  </FeatherDialog>
</template>
<script setup lang=ts>
import { computed, ref } from 'vue'
import { useStore } from 'vuex'
import { FeatherDialog } from '@featherds/dialog'
import { FeatherButton } from '@featherds/button'

const store = useStore()

const labels = {
  title: 'Delete confirmation',
  close: 'Close'
}

const open = ref(false)
const file = computed(() => {
  const fileToDelete = store.state.fileEditorModule.fileToDelete
  // eslint-disable-next-line vue/no-side-effects-in-computed-properties
  open.value = Boolean(fileToDelete)
  return fileToDelete
})

const deleteFile = () => store.dispatch('fileEditorModule/deleteFile', file.value.fullPath)
const cancel = () => store.dispatch('fileEditorModule/setFileToDelete', null)
</script>

<style lang="scss" scoped>
@import "@featherds/styles/themes/variables";
.dialog {
  width: 300px;
}
.btn-delete {
  color: var($error);
}
</style>
