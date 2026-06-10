<template>
  <div class="snmp-config-upload-download-tab">
    <div class="main-section">
        <h3>SNMP Configuration Upload/Download</h3>
        <div class="feather-row">
          <div class="feather-col-12">
            <span class="label">You can both download and upload the entire SNMP configuration in both XML and Json formats.
              <strong>Use caution</strong> when uploading SNMP configuration files, as this will overwrite the existing configuration and may impact device monitoring if the uploaded configuration is not correct.</span>
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
      title="Upload SNMP Configuration"
      actionButtonText="Upload"
      @cancel="onUploadCancel"
      @ok="onUploadConfirm"
    >
      <template v-slot:content>
        <p>Are you sure you want to upload the SNMP configuration? This will overwrite any existing configuration.</p>
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
import { downloadSnmpConfig, uploadSnmpConfig } from '@/services/snmpConfigService'
import { useSnmpConfigStore } from '@/stores/snmpConfigStore'
import ConfirmationDialog from '../Common/ConfirmationDialog.vue'

const { downloadFile } = useDownload()
const snackbar = useSnackbar()
const { startSpinner, stopSpinner } = useSpinner()
const snmpStore = useSnmpConfigStore()
const confirmationDialogVisible = ref(false)
const uploadType = ref<'xml' | 'json' | null>(null)
const uploadFile = ref<File | null>(null)

const onDownload = async (isXml: boolean) => {
  try {
    startSpinner()
    const response = await downloadSnmpConfig(isXml)

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
    const response = await uploadSnmpConfig(uploadFile.value, isXml)

    if (response) {
      snackbar.showSnackBar({
        msg: `Successfully uploaded ${isXml ? 'XML' : 'JSON'} file`,
        error: false
      })

      await snmpStore.populateSnmpConfig()
    } else {
      snackbar.showSnackBar({
        msg: `Error uploading ${isXml ? 'XML' : 'JSON'} file`,
        error: true
      })
    }
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

.snmp-config-upload-download-tab {
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
