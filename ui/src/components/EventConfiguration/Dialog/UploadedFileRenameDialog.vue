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
          The file name '<strong> {{ originalFileName }} </strong>' already exists in the system.
        </p>
        <p>Choose one of the following options:</p>
        <FeatherCheckboxGroup class="checkbox-group" label="" vertical>
          <FeatherCheckbox
            v-model="overwriteFile"
            @update:model-value="onChangeOverwriteFile"
          >
            Keep Original File Name: <strong>{{ originalFileName }}</strong> and Overwrite Existing File.
          </FeatherCheckbox>
          <FeatherCheckbox
            v-model="renameFile"
            @update:model-value="onChangeRenameFile"
          >
            Rename Uploaded File to:
          </FeatherCheckbox>
        </FeatherCheckboxGroup>
        <FeatherInput
          v-if="renameFile"
          class="new-file-name-input"
          v-model.trim="newFileName"
          label="New File Name"
          :error="error"
          :error-message="error || ''"
          placeholder="Enter new file name (must end with .events.xml)"
          @update:model-value="onChangeFileName"
        />
      </div>
      <template v-slot:footer>
        <FeatherButton @click="handleDialogHidden"> Cancel </FeatherButton>
        <FeatherButton
          primary
          :disabled="shouldRemainDisabled"
          @click="saveChanges"
        >
          Save Changes
        </FeatherButton>
      </template>
    </FeatherDialog>
  </div>
</template>

<script setup lang="ts">
import { UploadEventFileType } from '@/types/eventConfig'
import { FeatherButton } from '@featherds/button'
import { FeatherCheckbox, FeatherCheckboxGroup } from '@featherds/checkbox'
import { FeatherDialog } from '@featherds/dialog'
import { FeatherInput } from '@featherds/input'

const props = defineProps<{
  visible: boolean,
  fileBucket: UploadEventFileType[],
  alreadyExistsNames: string[],
  index: number
}>()

const emits = defineEmits<{
  (e: 'close'): void
  (e: 'rename', newFileName: string): void
  (e: 'overwrite'): void
}>()
const labels = {
  title: 'Rename Uploaded File'
}
const dialogVisible = ref(props.visible)
const renameFile = ref<boolean>(false)
const overwriteFile = ref<boolean>(false)
const error = ref<string | undefined>()
const newFileName = ref('')
const originalFileName = ref('')
const shouldRemainDisabled = computed(() => (
  (!renameFile.value && !overwriteFile.value) ||
  (renameFile.value && !!error.value)
))

const validateName = () => {
  let isValid = false
  if (newFileName.value === '') {
    error.value = 'File name cannot be empty.'
  } else if (!newFileName.value.endsWith('.events.xml')) {
    error.value = 'File name must end with .events.xml'
  } else if (newFileName.value === originalFileName.value) {
    error.value = 'New file name must be different from the original name.'
  } else if (props.fileBucket.map(f => f.file.name.toLowerCase()).includes(newFileName.value.trim().toLowerCase())) {
    error.value = 'A file with this name already exists in the current upload list.'
  } else if (props.alreadyExistsNames.map(n => n.replace('.xml', '').toLowerCase()).includes(newFileName.value.trim().replace('.xml', '').toLowerCase())) {
    error.value = 'A file with this name already exists in the system.'
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

const saveChanges = () => {
  if (overwriteFile.value) {
    emits('overwrite')
  } else if (renameFile.value && validateName() && props.index >= 0 && props.index < props.fileBucket.length) {
    emits('rename', newFileName.value)
  }
}

const handleDialogHidden = () => {
  renameFile.value = false
  overwriteFile.value = false
  newFileName.value = ''
  originalFileName.value = ''
  error.value = undefined
  emits('close')
}

const onChangeRenameFile = (value: boolean | undefined) => {
  renameFile.value = !!value
  if (value) {
    overwriteFile.value = false
  }
}

const onChangeOverwriteFile = (value: boolean | undefined) => {
  overwriteFile.value = !!value
  if (value) {
    renameFile.value = false
    newFileName.value = originalFileName.value
    error.value = undefined
  }
}

watch(() => props.visible, (val) => {
  dialogVisible.value = val
  if (val && props.index >= 0 && props.index < props.fileBucket.length) {
    originalFileName.value = props.fileBucket[props.index].file.name
    newFileName.value = originalFileName.value
    error.value = undefined
  } else {
    renameFile.value = false
    overwriteFile.value = false
    newFileName.value = ''
    originalFileName.value = ''
    error.value = undefined
  }
})
</script>

<style scoped lang="scss">
.modal-body {
  :deep(.checkbox-group) {
    .feather-input-sub-text {
      display: none !important;
    }
  }
  .new-file-name-input {
    margin-top: 15px;
  }
}
</style>

