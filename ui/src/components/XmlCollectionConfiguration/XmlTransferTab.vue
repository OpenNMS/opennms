<template>
  <div class="xml-transfer-tab">
    <TableCard class="xml-transfer-card">
      <div class="section">
        <div class="section-heading">
          <h3>Complete configuration</h3>
          <p>Settings, collections and group files as one JSON file, e.g. to copy them to another OpenNMS.</p>
        </div>
        <div class="actions">
          <OnmsButton variant="outlined" data-test="xml-download-json" @click="onDownload">Download JSON</OnmsButton>
          <label class="upload-label">
            <input
              type="file"
              accept=".json,application/json"
              data-test="xml-upload-json"
              @change="onPickJson"
            >
            <span class="upload-button">Upload JSON</span>
          </label>
        </div>
      </div>
    </TableCard>

    <TableCard class="xml-transfer-card">
      <div class="section">
        <div class="section-heading">
          <h3>Import XML files</h3>
          <p>
            Files of the former format: <code>xml-datacollection/*.xml</code> become group files named after the
            file, <code>xml-datacollection-config.xml</code> replaces all collections and the settings. Select
            several files at once to import group files and the collections that use them together.
          </p>
        </div>
        <div class="actions">
          <label class="upload-label">
            <input
              type="file"
              multiple
              accept=".xml,application/xml,text/xml"
              data-test="xml-upload-xml"
              @change="onPickXml"
            >
            <span class="upload-button">Import XML files</span>
          </label>
        </div>
      </div>
    </TableCard>

    <OnmsConfirmationDialog
      v-if="pending"
      :visible="true"
      title="Replace the stored configuration?"
      actionButtonText="Replace"
      data-test="xml-transfer-confirm"
      @ok="onConfirm"
      @cancel="onCancel"
    >
      <template #content>
        <p v-if="pending.kind === 'json'">
          The uploaded file replaces the settings, every collection and every group file. Group files not in the
          file are deleted.
        </p>
        <p v-else>
          '{{ pending.configFile?.name }}' replaces every collection and the settings.
          <template v-if="pending.groupFiles.length">
            {{ plural(pending.groupFiles.length, 'group file') }} will be imported first.
          </template>
        </p>
      </template>
    </OnmsConfirmationDialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import TableCard from '@/components/Common/TableCard.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import useDownload from '@/composables/useDownload'
import { plural } from '@/lib/utils'
import API from '@/services'
import { useXmlCollectionConfigStore } from '@/stores/xmlCollectionConfigStore'
import { OnmsButton, OnmsConfirmationDialog } from '@opennms/onms-ui'

type Pending =
  | { kind: 'json', file: File, input: HTMLInputElement }
  | { kind: 'xml', groupFiles: File[], configFile: File | null, input: HTMLInputElement }

const store = useXmlCollectionConfigStore()
const { downloadFile } = useDownload()
const { report, showError, showSuccess, withSpinner } = useActionFeedback()

const pending = ref<Pending | null>(null)

const onDownload = async () => {
  const response = await API.downloadXmlCollectionConfiguration()

  if (!response) {
    showError('Failed to download the XML collector configuration.')
    return
  }
  downloadFile(response as never, true)
}

const onPickJson = (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]

  if (file) {
    pending.value = { kind: 'json', file, input }
  }
}

/** The local name of the root element, or '' when the file is not XML. */
const rootElementOf = async (file: File): Promise<string> => {
  const document = new DOMParser().parseFromString(await file.text(), 'application/xml')
  return document.getElementsByTagName('parsererror').length ? '' : document.documentElement.localName
}

const onPickXml = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const files = Array.from(input.files ?? [])

  if (!files.length) {
    return
  }

  const groupFiles: File[] = []
  const configFiles: File[] = []

  for (const file of files) {
    const root = await rootElementOf(file)

    if (root === 'xml-groups') {
      groupFiles.push(file)
    } else if (root === 'xml-datacollection-config') {
      configFiles.push(file)
    } else {
      showError(`'${file.name}' is neither an <xml-groups> nor an <xml-datacollection-config> document.`)
      input.value = ''
      return
    }
  }

  if (configFiles.length > 1) {
    showError('Select at most one xml-datacollection-config.xml.')
    input.value = ''
    return
  }

  const selection: Pending = { kind: 'xml', groupFiles, configFile: configFiles[0] ?? null, input }

  // Group files only add or replace single group files; only the collection file replaces everything.
  if (selection.configFile) {
    pending.value = selection
  } else {
    await importXml(selection)
  }
}

const onCancel = () => {
  if (pending.value) {
    // Reset the input so picking the same file again still fires a change event.
    pending.value.input.value = ''
  }
  pending.value = null
}

const importXml = async (selection: Extract<Pending, { kind: 'xml' }>) => {
  selection.input.value = ''

  await withSpinner(async () => {
    // Group files first, so the collections importing them validate.
    for (const file of selection.groupFiles) {
      const result = await API.uploadXmlCollectionXml(file)
      if (!report(result)) {
        await store.fetchAll()
        return
      }
    }
    if (selection.configFile && !report(await API.uploadXmlCollectionXml(selection.configFile))) {
      await store.fetchAll()
      return
    }

    const parts = []
    if (selection.groupFiles.length) {
      parts.push(plural(selection.groupFiles.length, 'group file'))
    }
    if (selection.configFile) {
      parts.push('the collections')
    }
    showSuccess(`Imported ${parts.join(' and ')}.`)
    await store.fetchAll()
  })
}

const onConfirm = async () => {
  const selection = pending.value

  if (!selection) {
    return
  }
  pending.value = null

  if (selection.kind === 'xml') {
    await importXml(selection)
    return
  }

  selection.input.value = ''
  const result = await withSpinner(() => API.uploadXmlCollectionConfiguration(selection.file))

  if (report(result, 'XML collector configuration replaced.')) {
    await store.fetchAll()
  }
}
</script>

<style lang="scss" scoped>
// Padded like the event configuration table and framed like the SNMP configuration tabs.
.xml-transfer-card {
  padding: 25px;
  border: 1px solid var(--p-content-border-color);
  border-radius: 5px;
}

.xml-transfer-tab {
  display: flex;
  flex-direction: column;
  gap: 1em;

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
    flex-shrink: 0;
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
