<template>
  <FeatherDialog v-model="open" :labels="labels">
    <p class="subtitle2 dialog">Delete {{ file?.name }}?</p>

    <template v-slot:footer>
      <FeatherButton text @click="cancel">Cancel</FeatherButton>
      <FeatherButton class="btn-delete" text @click="deleteFile">Confirm</FeatherButton>
    </template>
  </FeatherDialog>
</template>
<script setup lang="ts">
import { FeatherDialog } from '@featherds/dialog'
import { FeatherButton } from '@featherds/button'
import { useFileEditorStore } from '@/stores/fileEditorStore'

const fileEditorStore = useFileEditorStore()

const labels = {
  title: 'Delete confirmation',
  close: 'Close'
}

const open = ref(false)
const file = computed(() => fileEditorStore.fileToDelete)

watchEffect(() => open.value = Boolean(file.value))

const deleteFile = () => fileEditorStore.deleteFile(file.value?.fullPath || '')
const cancel = () => fileEditorStore.setFileToDelete(null)
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
