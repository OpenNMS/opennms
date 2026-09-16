<template>
  <div class="transfer-tab">
    <TableCard v-for="section in sections" :key="section.key">
      <div class="section">
        <div class="section-heading">
          <h3>{{ section.title }}</h3>
          <p>{{ section.description }}</p>
        </div>

        <div class="actions">
          <OnmsButton variant="outlined" :data-test="`${section.key}-download-json`" @click="onDownload(section, false)">
            Download JSON
          </OnmsButton>
          <OnmsButton variant="outlined" :data-test="`${section.key}-download-xml`" @click="onDownload(section, true)">
            Download XML
          </OnmsButton>

          <label class="upload-label">
            <input
              type="file"
              accept=".json,.xml,application/json,application/xml,text/xml"
              :data-test="`${section.key}-upload`"
              @change="onUpload(section, $event)"
            >
            <span class="upload-button">Upload file</span>
          </label>
        </div>
      </div>
    </TableCard>

    <OnmsConfirmationDialog
      v-if="pendingUpload"
      :visible="true"
      title="Replace the stored configuration?"
      actionButtonText="Replace"
      data-test="transfer-upload-confirm"
      @ok="onConfirmUpload"
      @cancel="onCancelUpload"
    >
      <template #content>
        <p>
          The uploaded file replaces {{ pendingUpload.section.title.toLowerCase() }} in full. Anything not in
          the file is removed.
        </p>
      </template>
    </OnmsConfirmationDialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import TableCard from '@/components/Common/TableCard.vue'
import useDownload from '@/composables/useDownload'
import useSnackbar from '@/composables/useSnackbar'
import { useThreshdConfigurationStore } from '@/stores/threshdConfigurationStore'
import { useThresholdGroupStore } from '@/stores/thresholdGroupStore'
import API from '@/services'
import { OnmsButton, OnmsConfirmationDialog } from '@opennms/onms-ui'

interface TransferSection {
  key: string
  title: string
  description: string
  download: (isXml: boolean) => Promise<unknown>
  upload: (file: File, isXml: boolean) => Promise<{ success: boolean; message: string }>
  refresh: () => Promise<unknown>
}

const groupStore = useThresholdGroupStore()
const threshdStore = useThreshdConfigurationStore()
const { downloadFile } = useDownload()
const { showSnackBar } = useSnackbar()

const pendingUpload = ref<{ section: TransferSection, file: File, isXml: boolean, input: HTMLInputElement } | null>(null)

const sections: TransferSection[] = [
  {
    key: 'thresholding',
    title: 'Threshold groups',
    description: 'The thresholds and expression thresholds of every group.',
    download: API.downloadThresholdingConfiguration,
    upload: API.uploadThresholdingConfiguration,
    refresh: () => groupStore.fetchGroups()
  },
  {
    key: 'threshd',
    title: 'Threshd configuration',
    description: 'The threshd daemon packages, services and thresholders.',
    download: API.downloadThreshdConfiguration,
    upload: API.uploadThreshdConfiguration,
    refresh: () => threshdStore.fetchConfiguration()
  }
]

const onDownload = async (section: TransferSection, isXml: boolean) => {
  const response = await section.download(isXml)

  if (!response) {
    showSnackBar({ msg: `Failed to download the ${section.title.toLowerCase()}.`, error: true })
    return
  }

  downloadFile(response as never, true)
}

const onUpload = (section: TransferSection, event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]

  if (!file) {
    return
  }

  // The uploads replace the whole document, so confirm before discarding what is stored.
  pendingUpload.value = { section, file, isXml: file.name.toLowerCase().endsWith('.xml'), input }
}

const onCancelUpload = () => {
  if (pendingUpload.value) {
    // Reset the input so picking the same file again still fires a change event.
    pendingUpload.value.input.value = ''
  }
  pendingUpload.value = null
}

const onConfirmUpload = async () => {
  const upload = pendingUpload.value

  if (!upload) {
    return
  }

  pendingUpload.value = null
  upload.input.value = ''

  const result = await upload.section.upload(upload.file, upload.isXml)

  showSnackBar({
    msg: result.success ? `${upload.section.title} replaced.` : result.message,
    error: !result.success
  })

  if (result.success) {
    await upload.section.refresh()
  }
}
</script>

<style lang="scss" scoped>
.transfer-tab {
  display: flex;
  flex-direction: column;
  gap: 1em;

  .section {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1em;
    padding: 0 1em;
  }

  .section-heading {
    h3 {
      margin: 0;
    }

    p {
      margin: 0.25em 0 0 0;
      color: var(--p-text-muted-color);
    }
  }

  .actions {
    display: flex;
    align-items: center;
    gap: 0.5em;
  }

  .upload-label {
    input[type='file'] {
      display: none;
    }
  }

  .upload-button {
    display: inline-block;
    cursor: pointer;
    padding: 0.5rem 1rem;
    border: 1px solid var(--p-primary-color);
    border-radius: var(--p-border-radius-md, 4px);
    color: var(--p-primary-color);
  }
}
</style>
