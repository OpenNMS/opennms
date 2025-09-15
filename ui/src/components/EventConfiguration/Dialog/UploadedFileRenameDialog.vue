<template>
  <div class="uploaded-file-rename-dialog">
    <FeatherDialog
      v-model="dialogVisible"
      :labels="labels"
      hide-close
      @hidden="handleDialogHidden"
    >
      <div class="modal-body">
        <p>
          The file name '<strong> {{ originalFileName }} </strong>' already exists in the system. Please choose a
          different name.
        </p>
        <p>Please provide a new name for the file:</p>
        <FeatherInput
          class="new-file-name-input"
          v-model.trim="newFileName"
          label="New File Name"
          :error="error"
          :error-message="error || ''"
          placeholder="Enter new file name (must end with .xml)"
          @update:model-value="onChangeFileName"
        />
      </div>
      <template v-slot:footer>
        <FeatherButton @click="handleDialogHidden"> Cancel </FeatherButton>
        <FeatherButton
          primary
          :disabled="shouldRemainDisabled"
          @click="rename"
        >
          Rename
        </FeatherButton>
      </template>
    </FeatherDialog>
  </div>
</template>

<script setup lang="ts">
import { UploadEventFileType } from '@/types/eventConfig'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'
import { FeatherInput } from '@featherds/input'

const props = defineProps<{
  visible: boolean,
  fileBucket: UploadEventFileType[],
  alreadyExistsNames: string[],
  index: number
}>()

function handleDialogHidden() {
  emits('close')
}
const emits = defineEmits<{
  (e: 'close'): void
  (e: 'rename', newFileName: string): void
}>()
const dialogVisible = ref(props.visible)
const labels = {
  title: 'Rename Uploaded File'
}
const error = ref<string | undefined>()
const newFileName = ref('')
const originalFileName = ref('')
const shouldRemainDisabled = computed(() => (
  !newFileName.value ||
  newFileName.value.trim() === '' ||
  newFileName.value === originalFileName.value ||
  props.fileBucket.map(f => f.file.name.toLowerCase()).includes(newFileName.value.trim().toLowerCase()) ||
  props.alreadyExistsNames.map(n => n.toLowerCase()).includes(newFileName.value.trim().toLowerCase()) ||
  newFileName.value.endsWith('.events.xml') === false
))

const validateName = () => {
  let isValid = false
  if (newFileName.value === '') {
    error.value = 'File name cannot be empty'
  } else if (!newFileName.value.endsWith('.events.xml')) {
    error.value = 'File name must end with .events.xml'
  } else if (newFileName.value === originalFileName.value) {
    error.value = 'New file name must be different from the original name'
  } else if (props.fileBucket.map(f => f.file.name.toLowerCase()).includes(newFileName.value.trim().toLowerCase())) {
    error.value = 'A file with this name already exists in the current upload list'
  } else if (props.alreadyExistsNames.map(n => n.toLowerCase()).includes(newFileName.value.trim().toLowerCase())) {
    error.value = 'A file with this name already exists in the system'
  } else {
    error.value = undefined
    isValid = true
  }

  return isValid
}

const onChangeFileName = (value: any) => {
  if (value) {
    newFileName.value = value.trim()
    validateName()
  }
}

const rename = () => {
  if (validateName() && props.index >= 0 && props.index < props.fileBucket.length) {
    emits('rename', newFileName.value)
  } else {
    console.error('Invalid file index or name')
  }
}

watch(() => props.visible, (val) => {
  dialogVisible.value = val
  if (val && props.index >= 0 && props.index < props.fileBucket.length) {
    originalFileName.value = props.fileBucket[props.index].file.name
    newFileName.value = originalFileName.value
    error.value = undefined
  } else {
    originalFileName.value = ''
    newFileName.value = ''
    error.value = 'Invalid file selected for renaming.'
  }
})
</script>

<style scoped lang="scss">
.modal-body {
  .new-file-name-input {
    margin-top: 15px;
  }
}
</style>

