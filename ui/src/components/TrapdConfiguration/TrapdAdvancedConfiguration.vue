<template>
  <div class="trapd-config-upload-download-tab">
    <div class="main-section">
        <h3>Trap Configuration Upload/Download</h3>
        <div class="feather-row">
          <div class="feather-col-12">
            <span class="label">You can both download and upload the entire Trap configuration in both XML and JSON formats.
              <strong>Use caution</strong> when uploading Trap configuration files, as this will overwrite the existing configuration and may impact device monitoring if the uploaded configuration is not correct.</span>
          </div>
        </div>
        <div class="feather-row">
          <div class="feather-col-6">
            <label class="label">Download file in XML format:</label>
          </div>
          <div class="feather-col-6">
            <FeatherButton
              primary
              data-test="download-xml-button"
              class="upload-download-button"
              @click="onDownload(true)"
            >
              <template v-slot:icon>
                <FeatherIcon :icon="IconDownload" aria-hidden="true" focusable="false" class="upload-download-icon" />
                Download XML
              </template>
            </FeatherButton>
           </div>
        </div>
        <div class="feather-row">
          <div class="feather-col-6">
            <label class="label">Download file in JSON format:</label>
          </div>
          <div class="feather-col-6">
            <FeatherButton
              primary
              data-test="download-json-button"
              class="upload-download-button"
              @click="onDownload(false)"
            >
              <template v-slot:icon>
                <FeatherIcon :icon="IconDownload" aria-hidden="true" focusable="false" class="upload-download-icon" />
                Download JSON
              </template>
            </FeatherButton>
           </div>
        </div>
         <div class="feather-row">
          <div class="feather-col-6">
            <label class="label">Upload file in XML format:</label>
          </div>
          <div class="feather-col-6">
            <FeatherButton
              primary
              data-test="upload-xml-button"
              class="upload-download-button"
              @click="initiateUpload(true)"
            >
              <template v-slot:icon>
                <FeatherIcon :icon="IconUpload" aria-hidden="true" focusable="false" class="upload-download-icon" />
                Upload XML
              </template>
            </FeatherButton>
           </div>
        </div>
         <div class="feather-row">
          <div class="feather-col-6">
            <label class="label">Upload file in JSON format:</label>
          </div>
          <div class="feather-col-6">
            <FeatherButton
              primary
              data-test="upload-json-button"
              class="upload-download-button"
              @click="initiateUpload(false)"
            >
              <template v-slot:icon>
                <FeatherIcon :icon="IconUpload" aria-hidden="true" focusable="false" class="upload-download-icon" />
                Upload JSON
              </template>
            </FeatherButton>
           </div>
        </div>
    </div>
    <ConfirmationDialog
      :visible="confirmationDialogVisible"
      title="Upload Trap Configuration"
      actionButtonText="Upload"
      @cancel="onUploadCancel"
      @ok="onUploadConfirm"
    >
      <template v-slot:content>
        <p>Are you sure you want to upload the Trap configuration? This will overwrite any existing configuration.</p>
      </template>
    </ConfirmationDialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import IconDownload from '@featherds/icon/action/DownloadFile'
import IconUpload from '@featherds/icon/action/UploadFile'
import useDownload from '@/composables/useDownload'
import useSnackbar from '@/composables/useSnackbar'
import useSpinner from '@/composables/useSpinner'
import { validateTrapdXml, validateTrapdJson } from '@/lib/trapdValidator'
import { downloadTrapdConfig, uploadTrapdConfiguration } from '@/services/trapdConfigurationService'
import { useTrapdConfigStore } from '@/stores/trapdConfigStore'
import ConfirmationDialog from '../Common/ConfirmationDialog.vue'

const { downloadFile } = useDownload()
const snackbar = useSnackbar()
const { startSpinner, stopSpinner } = useSpinner()
const trapdConfigStore = useTrapdConfigStore()
const confirmationDialogVisible = ref(false)
const uploadType = ref<'xml' | 'json' | null>(null)
const uploadFile = ref<File | null>(null)

const onDownload = async (isXml: boolean) => {
  try {
    startSpinner()
    const response = await downloadTrapdConfig(isXml)

    if (response) {
      downloadFile(response, true)
    } else {
      snackbar.showSnackBar({
        msg: `Error downloading ${isXml ? 'XML' : 'JSON'} file`,
        error: true
      })
    }
  } finally {
    stopSpinner()
  }
}

const initiateUpload = async (isXml: boolean) => {
  uploadType.value = isXml ? 'xml' : 'json'

  const file = await new Promise<File | null>((resolve) => {
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = isXml ? '.xml' : '.json'

    const cleanup = () => {
      input.remove()
    }

    input.addEventListener('cancel', () => {
      cleanup()
      resolve(null)
    })

    input.onchange = () => {
      const selectedFile = input.files ? input.files[0] : null
      cleanup()
      resolve(selectedFile)
    }

    document.body.appendChild(input)
    input.click()
  })

  if (!file) {
    uploadType.value = null
    return
  }

  uploadFile.value = file
  confirmationDialogVisible.value = true
}

const onUploadCancel = () => {
  confirmationDialogVisible.value = false
  uploadType.value = null
  uploadFile.value = null
}

const onUploadConfirm = async () => {
  confirmationDialogVisible.value = false

  if (uploadType.value && uploadFile.value) {
    await performUpload(uploadType.value === 'xml')
    uploadType.value = null
    uploadFile.value = null
  }
}

const performUpload = async (isXml: boolean) => {
  if (!uploadFile.value) {
    return
  }

  try {
    startSpinner()

    const fileName = uploadFile.value.name
    const textContent = await uploadFile.value.text()

    if (isXml) {
      const validationResult = validateTrapdXml(textContent)

      if (!validationResult.valid) {
        const errorList = validationResult.errors.slice(0, 3).map(error => error.message).join(' | ')
        const moreCount = validationResult.errors.length - 3
        const suffix = moreCount > 0 ? ` (+${moreCount} more)` : ''
        snackbar.showSnackBar({ msg: `Invalid trap configuration XML: ${errorList}${suffix}`, error: true })
        return
      }
    } else {
      const validationResult = validateTrapdJson(textContent)

      if (!validationResult.valid) {
        const errorList = validationResult.errors.slice(0, 3).map(error => error.message).join(' | ')
        const moreCount = validationResult.errors.length - 3
        const suffix = moreCount > 0 ? ` (+${moreCount} more)` : ''
        snackbar.showSnackBar({ msg: `Invalid trap configuration JSON: ${errorList}${suffix}`, error: true })
        return
      }
    }

    await uploadTrapdConfiguration(uploadFile.value, isXml)

    snackbar.showSnackBar({
      msg: `Successfully uploaded Trap configuration from '${fileName}'.`,
      error: false
    })

    await trapdConfigStore.fetchTrapConfig()
  } catch (err) {
    const msg = err instanceof Error ? err.message : 'Error uploading Trap configuration'
    snackbar.showSnackBar({
      msg,
      error: true
    })
  } finally {
    stopSpinner()
  }
}
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/vars.scss';

.trapd-config-upload-download-tab {
  background: var(variables.$surface);
  width: 80%;
  padding: 0;
  border-radius: 5px;
  margin-top: 0;
  border: 1px solid var(variables.$border-on-surface);

  .main-section {
    display: flex;
    flex-direction: column;
    gap: 20px;
    padding: 20px;

    // make upload/download buttons the same length
    button.upload-download-button {
      width: 15em;
    }

    .feather-row {
      margin-bottom: 0.5rem;
    }

    button.btn.btn-icon .upload-download-icon {
      font-size: 1.1rem;
    }
  }
}
</style>
