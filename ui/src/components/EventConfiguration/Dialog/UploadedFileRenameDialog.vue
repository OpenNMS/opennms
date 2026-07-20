<template>
  <div class="uploaded-file-rename-dialog">
    <Dialog
      v-model:visible="dialogVisible"
      :header="labels.title"
      :modal="true"
      :draggable="false"
      :closable="false"
      @hide="onHide"
      data-test="dialog-title"
    >
      <div class="modal-body">
        <p>
          The file name '<strong> {{ originalFileName }} </strong>' already exists in the system.
        </p>
        <p>Choose one of the following options:</p>
        <div class="checkbox-group">
          <div class="checkbox-row">
            <Checkbox
              :modelValue="overwriteFile"
              binary
              inputId="rename-overwrite-checkbox"
              @update:model-value="onChangeOverwriteFile"
            />
            <label for="rename-overwrite-checkbox">
              Keep Original File Name: <strong>{{ originalFileName }}</strong> and Overwrite Existing File.
            </label>
          </div>
          <div class="checkbox-row">
            <Checkbox
              :modelValue="renameFile"
              binary
              inputId="rename-rename-checkbox"
              @update:model-value="onChangeRenameFile"
            />
            <label for="rename-rename-checkbox">
              Rename Uploaded File to:
            </label>
          </div>
        </div>
        <div
          v-if="renameFile"
          class="new-file-name-input"
        >
          <FormField
            label="New File Name"
            :for="fileNameInputId"
            :error="error"
          >
            <InputText
              :id="fileNameInputId"
              :modelValue="newFileName"
              :invalid="!!error"
              placeholder="Enter new file name (must end with .xml)"
              fluid
              @update:model-value="onChangeFileName"
              data-test="file-name"
            />
          </FormField>
        </div>
      </div>
      <template #footer>
        <Button
          :disabled="shouldRemainDisabled"
          label="Save Changes"
          @click="saveChanges"
          data-test="save-button"
        />
        <Button
          text
          label="Cancel"
          @click="onCancel"
        />
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, useId, watch } from 'vue'

import { UploadedSourceNamesResponse, UploadEventFileType } from '@/types/eventConfig'
import Button from 'primevue/button'
import Checkbox from 'primevue/checkbox'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import FormField from '@/components/Common/FormField.vue'

const props = defineProps<{
  visible: boolean,
  fileBucket: UploadEventFileType[],
  alreadyExistsNames: UploadedSourceNamesResponse[],
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
const fileNameInputId = useId()
const dialogVisible = ref(props.visible)
const renameFile = ref<boolean>(false)
const overwriteFile = ref<boolean>(false)
const error = ref<string | undefined>()
const newFileName = ref('')
const originalFileName = ref('')

// Guards against the Dialog's `hide` event (which also fires on programmatic
// close) emitting a second `close` after the Cancel button already handled it.
let resolved = false

const shouldRemainDisabled = computed(() => (
  (!renameFile.value && !overwriteFile.value) ||
  (renameFile.value && !!error.value)
))

const validateName = () => {
  let isValid = false
  if (newFileName.value === '') {
    error.value = 'File name cannot be empty.'
  } else if (!newFileName.value.endsWith('.xml')) {
    error.value = 'File name must end with .xml'
  } else if (newFileName.value === originalFileName.value) {
    error.value = 'New file name must be different from the original name.'
  } else if (props.fileBucket.map(f => f.file.name.toLowerCase()).includes(newFileName.value.trim().toLowerCase())) {
    error.value = 'A file with this name already exists in the current upload list.'
  } else if (props.alreadyExistsNames.map(s => s.name.replace('.xml', '').toLowerCase()).includes(newFileName.value.trim().replace('.xml', '').toLowerCase())) {
    error.value = 'A file with this name already exists in the system.'
  } else {
    error.value = undefined
    isValid = true
  }

  return isValid
}

const onChangeFileName = (value: any) => {
  newFileName.value = (value ?? '').trim()
  validateName()
}

const saveChanges = () => {
  if (overwriteFile.value) {
    resolved = true
    emits('overwrite')
  } else if (renameFile.value && validateName() && props.index >= 0 && props.index < props.fileBucket.length) {
    resolved = true
    emits('rename', newFileName.value)
  }
}

const resetState = () => {
  renameFile.value = false
  overwriteFile.value = false
  newFileName.value = ''
  originalFileName.value = ''
  error.value = undefined
}

const handleDialogHidden = () => {
  resetState()
  emits('close')
}

const onCancel = () => {
  resolved = true
  dialogVisible.value = false
  handleDialogHidden()
}

const onHide = () => {
  if (!resolved) {
    handleDialogHidden()
  }
  resolved = false
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
  if (val) {
    resolved = false
  }
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
.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin: 0.5rem 0;
}

.checkbox-row {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
}

.new-file-name-input {
  margin-top: 1rem;
}
</style>
