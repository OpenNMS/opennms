<template>
  <TableCard class="http-transfer-card">
    <div class="section">
      <div class="section-heading">
        <h3>HTTP collector configuration</h3>
        <p>
          The settings and every collection, in the JSON form of this API or as the former
          http-datacollection-config.xml.
        </p>
      </div>

      <div class="actions">
        <OnmsButton variant="outlined" data-test="http-collection-download-json" @click="onDownload(false)">
          Download JSON
        </OnmsButton>
        <OnmsButton variant="outlined" data-test="http-collection-download-xml" @click="onDownload(true)">
          Download XML
        </OnmsButton>

        <label class="upload-label">
          <input
            type="file"
            accept=".json,.xml,application/json,application/xml,text/xml"
            data-test="http-collection-upload"
            @change="onUpload"
          >
          <span class="upload-button">Upload file</span>
        </label>
      </div>
    </div>

    <OnmsConfirmationDialog
      v-if="pendingUpload"
      :visible="true"
      title="Replace the stored configuration?"
      actionButtonText="Replace"
      data-test="http-collection-upload-confirm"
      @ok="onConfirmUpload"
      @cancel="onCancelUpload"
    >
      <template #content>
        <p>
          The uploaded file replaces the HTTP collector configuration in full. Collections not in the file are
          removed.
        </p>
      </template>
    </OnmsConfirmationDialog>
  </TableCard>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import TableCard from '@/components/Common/TableCard.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import useDownload from '@/composables/useDownload'
import API from '@/services'
import { useHttpCollectionStore } from '@/stores/httpCollectionStore'
import { OnmsButton, OnmsConfirmationDialog } from '@opennms/onms-ui'

const store = useHttpCollectionStore()
const { downloadFile } = useDownload()
const { report, showError } = useActionFeedback()

const pendingUpload = ref<{ file: File, isXml: boolean, input: HTMLInputElement } | null>(null)

const onDownload = async (isXml: boolean) => {
  const response = await API.downloadHttpCollectionConfiguration(isXml)

  if (!response) {
    showError('Failed to download the HTTP collector configuration.')
    return
  }

  downloadFile(response as never, true)
}

const onUpload = (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]

  if (!file) {
    return
  }

  // The upload replaces the whole document, so confirm before discarding what is stored.
  pendingUpload.value = { file, isXml: file.name.toLowerCase().endsWith('.xml'), input }
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

  const result = await API.uploadHttpCollectionConfiguration(upload.file, upload.isXml)

  if (report(result, 'HTTP collector configuration replaced.')) {
    await Promise.all([store.fetchCollections(), store.fetchSettings()])
  }
}
</script>

<style lang="scss" scoped>
.http-transfer-card {
  padding: 25px;
  border: 1px solid var(--p-content-border-color);
  border-radius: 5px;

  .section {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1em;
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
