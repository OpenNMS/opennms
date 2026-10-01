<template>
  <div class="upload-files-tab">
    <div class="upload-section">
      <div class="header-row">
        <h2>Upload Event Configuration Files</h2>
        <input
          type="file"
          accept=".xml"
          multiple
          @change="handleEventConfUpload"
          data-test="event-conf-upload-input"
          ref="eventConfFileInput"
          class="hidden-input"
        />
        <input
          type="file"
          multiple
          webkitdirectory
          directory
          @change="handleFolderUpload"
          ref="eventFolderInput"
          class="hidden-input"
        />
      </div>
      <div
        class="selected-files-section"
        :class="{ 'drag-active': dragDepth > 0 }"
        data-test="drop-zone"
        @dragover.prevent
        @dragenter.prevent="onDragEnter"
        @dragleave="onDragLeave"
        @drop.prevent="onDrop"
      >
        <div v-if="eventFiles.length > 0">
          <div
            class="add-more-row"
            data-test="add-more-row"
          >
            <span>Drag more files or folders here, or</span>
            <OnmsButton
              variant="text"
              size="small"
              data-test="add-more-files"
              @click="openFileDialog"
            >browse files</OnmsButton>
            &middot;
            <OnmsButton
              variant="text"
              size="small"
              data-test="add-more-folder"
              @click="openFolderDialog"
            >a folder</OnmsButton>
          </div>
          <div class="chips-row">
            <span
              class="files-count"
              data-test="files-count"
            >{{ eventFiles.length === 1 ? '1 file' : `${eventFiles.length} files` }}</span>
            <button
              v-if="readyCount > 0"
              class="chip chip-ready"
              :class="{ active: activeFilter === 'ready' }"
              data-test="ready-chip"
              @click="toggleFilter('ready')"
            >
              <OnmsIcon
                :icon="CheckCircle"
                aria-hidden="true"
                focusable="false"
              />
              {{ readyCount }} ready
            </button>
            <button
              v-if="duplicateCount > 0"
              class="chip chip-replace"
              :class="{ active: activeFilter === 'replace' }"
              data-test="replace-chip"
              @click="toggleFilter('replace')"
            >
              <OnmsIcon
                :icon="Warning"
                aria-hidden="true"
                focusable="false"
              />
              {{ duplicateCount }} replace existing
            </button>
            <button
              v-if="invalidCount > 0"
              class="chip chip-invalid"
              :class="{ active: activeFilter === 'invalid' }"
              data-test="invalid-chip"
              @click="toggleFilter('invalid')"
            >
              <OnmsIcon
                :icon="Error"
                aria-hidden="true"
                focusable="false"
              />
              {{ invalidCount }} invalid
            </button>
            <span class="chips-spacer"></span>
            <OnmsButton
              v-if="duplicateCount > 0"
              variant="text"
              size="small"
              data-test="replace-all-button"
              :disabled="queueingCount > 0"
              @click="replaceAllDuplicates"
            >Replace all {{ duplicateCount }}</OnmsButton>
          </div>
          <div
            v-if="manifestFile"
            class="manifest-callout"
            data-test="manifest-callout"
          >
            <OnmsIcon
              :icon="ListIcon"
              aria-hidden="true"
              focusable="false"
            />
            <span class="manifest-text">
              <strong>{{ manifestFile.file.name }}</strong>
              &mdash; sets the evaluation order{{ manifestFile.manifestEntries ? ` (${manifestFile.manifestEntries} entries)` : '' }}. Not stored as a source; the catch-all stays last.
            </span>
            <OnmsIconButton
              title="Remove eventconf.xml"
              data-test="remove-manifest-button"
              :icon="Delete"
              @click="removeFileEntry(manifestFile)"
            />
          </div>
          <Draggable
            v-model="queueView"
            :item-key="fileItemKey"
            handle=".drag-handle"
            :disabled="activeFilter !== 'all' || queueingCount > 0"
            class="columns-drag-container"
          >
            <template #item="{ element }">
              <div class="file">
                <div class="file-icon">
                  <OnmsIcon :icon="Text" />
                  <span>
                    {{ ellipsify(element.file.name, 39) }}
                  </span>
                </div>
                <div class="actions">
                  <OnmsIconButton
                    v-if="element.isDuplicate"
                    :icon="Warning"
                    title="Rename, or confirm replacing the existing source"
                    tooltip="A source with this name already exists. Click to rename the file, or confirm that uploading it replaces the source."
                    class="warning-icon"
                    @click="openRenameFor(element)"
                  />
                  <OnmsIcon
                    v-if="element.isValid && !element.isDuplicate"
                    :icon="CheckCircle"
                    v-onms-tooltip="'File is valid'"
                    class="success-icon"
                  />
                  <OnmsIcon
                    v-if="!element.isValid"
                    :icon="Error"
                    v-onms-tooltip="element.errors.map((error: string) => `${error}. `).join('\n')"
                    class="error-icon"
                  />
                  <OnmsIconButton
                    title="Reorder"
                    class="close-icon drag-handle"
                    :icon="Apps"
                  />
                  <OnmsIconButton
                    title="Remove"
                    data-test="remove-files-button"
                    :icon="Delete"
                    @click="removeFileEntry(element)"
                  />
                </div>
              </div>
            </template>
          </Draggable>
          <div
            v-if="activeFilter !== 'all' && queueView.length === 0"
            class="filter-empty"
          >No files match this filter any more.</div>
        </div>
        <div
          v-else
          class="empty-state"
        >
          <svg
            class="empty-cloud"
            width="56"
            height="56"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.6"
            stroke-linecap="round"
            stroke-linejoin="round"
            aria-hidden="true"
          >
            <path d="M4 16.2A4.5 4.5 0 0 1 6.6 8a6 6 0 0 1 11.6 1.6A3.8 3.8 0 0 1 20 17"></path>
            <path d="M12 12v8"></path>
            <path d="M8.5 15.5 12 12l3.5 3.5"></path>
          </svg>
          <p class="empty-headline">Drag event files or a folder here</p>
          <p class="empty-links">
            or
            <OnmsButton
              variant="text"
              size="small"
              data-test="browse-files-link"
              @click="openFileDialog"
            >browse files</OnmsButton>
            &middot;
            <OnmsButton
              variant="text"
              size="small"
              data-test="browse-folder-link"
              @click="openFolderDialog"
            >browse a folder</OnmsButton>
          </p>
          <p class="empty-hint">Each .xml file becomes one event source. Include an eventconf.xml to set the evaluation order. Nothing uploads until you confirm.</p>
        </div>
      </div>
      <div
        v-if="eventFiles.length > 0"
        class="commit-bar"
        data-test="commit-bar"
      >
        <div class="commit-summary">
          <div
            class="commit-headline"
            :class="{ blocked: blockedReason !== '' }"
            data-test="commit-headline"
          >{{ blockedReason || 'Ready to upload' }}</div>
          <div
            class="commit-detail"
            data-test="commit-detail"
          >{{ commitDetail }}</div>
        </div>
        <OnmsButton
          variant="outlined"
          data-test="clear-queue-button"
          :disabled="isLoading"
          @click="clearQueue"
        >Clear queue</OnmsButton>
        <OnmsButton
          :label="eventFiles.length === 1 ? 'Upload 1 file' : `Upload ${eventFiles.length} files`"
          :disabled="shouldUploadDisabled"
          :loading="isLoading"
          @click="uploadFiles"
          data-test="upload-button"
        />
      </div>
      <div class="info-section">
        <h3>Instructions:</h3>
        <ul>
          <li>Each .xml file is stored as one event source and may define any number of events.</li>
          <li>Drag files or folders onto the box, or browse; selections combine with no limit. When two files share a name, only the first is kept.</li>
          <li>An eventconf.xml is not stored as a source: its entries set the source evaluation order, first entry evaluated first. Unlisted sources come before listed ones; the catch-all stays last.</li>
          <li>Files matching an existing source must be renamed, or confirmed one by one or with "Replace all" &mdash; uploading replaces the source.</li>
        </ul>
      </div>
    </div>
    <EventConfigFilesUploadReportDialog :report="uploadFilesReport" />
    <UploadedFileRenameDialog
      :visible="displayRenameDialog"
      :fileBucket="eventFiles"
      :index="selectedIndex ?? -1"
      :alreadyExistsNames="store.uploadedSources"
      @close="closeRenameDialog"
      @rename="renameFile"
      @overwrite="overwriteFile"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onUnmounted, ref, watch } from 'vue'

import useSnackbar from '@/composables/useSnackbar'
import { ellipsify } from '@/lib/utils'
import { uploadEventConfigFiles } from '@/services/eventConfigService'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { EventConfigFilesUploadResponse, UploadEventFileType } from '@/types/eventConfig'
import { OnmsButton, OnmsIcon, OnmsIconButton } from '@opennms/onms-ui'
import CheckCircle from '@opennms/onms-ui/icons/action/CheckCircle.vue'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import ListIcon from '@opennms/onms-ui/icons/action/List.vue'
import Text from '@opennms/onms-ui/icons/file/Text.vue'
import Apps from '@opennms/onms-ui/icons/navigation/Apps.vue'
import Error from '@opennms/onms-ui/icons/notification/Error.vue'
import Warning from '@opennms/onms-ui/icons/notification/Warning.vue'
import Draggable from 'vuedraggable'
import EventConfigFilesUploadReportDialog from './Dialog/EventConfigFilesUploadReportDialog.vue'
import UploadedFileRenameDialog from './Dialog/UploadedFileRenameDialog.vue'
import { isDuplicateFile, validateEventConfigFile } from './eventConfigXmlValidator'
import { collectDroppedFiles } from './fileDropTraversal'

const eventFolderInput = ref<HTMLInputElement | null>(null)
const eventConfFileInput = ref<HTMLInputElement | null>(null)
const uploadFilesReport = ref<EventConfigFilesUploadResponse>({} as EventConfigFilesUploadResponse)
const store = useEventConfigStore()
const isLoading = ref(false)
const snackbar = useSnackbar()
const eventFiles = ref<UploadEventFileType[]>([])
const displayRenameDialog = ref(false)
const selectedIndex = ref<number | null>(null)
const fileItemKey = (item: UploadEventFileType) => item.file.name

// selections still being read and validated; uploading mid-stream would send a partial queue
const queueingCount = ref(0)

const shouldUploadDisabled = computed(() => {
  return (
    eventFiles.value.length === 0 ||
    isLoading.value ||
    queueingCount.value > 0 ||
    !eventFiles.value.every(f => f.isValid) ||
    eventFiles.value.some(f => f.isDuplicate)
  )
})

const matchesExistingSource = (fileName: string) => store.uploadedSources
  .map(source => source.name.replace('.xml', '').toLowerCase())
  .includes(fileName.replace('.xml', '').toLowerCase())

// The ordering manifest is a different kind of object than an event file: it gets its own
// callout row instead of a place in the queue list.
const isManifest = (entry: UploadEventFileType) => entry.file.name.toLowerCase() === 'eventconf.xml'
const manifestFile = computed(() => eventFiles.value.find(isManifest))
const listFiles = computed(() => eventFiles.value.filter(entry => !isManifest(entry)))

const readyCount = computed(() => listFiles.value.filter(f => f.isValid && !f.isDuplicate).length)
const duplicateCount = computed(() => eventFiles.value.filter(f => f.isDuplicate).length)
const invalidCount = computed(() => eventFiles.value.filter(f => !f.isValid).length)

// The chips double as filters: at a couple of hundred files, finding the one invalid
// file or the three unconfirmed replacements is the actual job.
const activeFilter = ref<'all' | 'ready' | 'replace' | 'invalid'>('all')
const toggleFilter = (filter: 'ready' | 'replace' | 'invalid') => {
  activeFilter.value = activeFilter.value === filter ? 'all' : filter
}
const matchesActiveFilter = (entry: UploadEventFileType) => {
  switch (activeFilter.value) {
    case 'ready': return entry.isValid && !entry.isDuplicate
    case 'replace': return entry.isDuplicate
    case 'invalid': return !entry.isValid
    default: return true
  }
}

// Reorder writes only land on the unfiltered view; while a filter is active the drag is disabled.
const queueView = computed({
  get: () => listFiles.value.filter(matchesActiveFilter),
  set: (reordered: UploadEventFileType[]) => {
    if (activeFilter.value === 'all') {
      const manifest = manifestFile.value
      eventFiles.value = manifest ? [manifest, ...reordered] : reordered
    }
  }
})

// What the upload will do, and when it cannot run, why — never just a mute grey button
const blockedReason = computed(() => {
  if (queueingCount.value > 0) {
    return 'Still reading and validating the selection…'
  }
  const parts: string[] = []
  if (invalidCount.value > 0) {
    parts.push(`fix ${invalidCount.value} invalid ${invalidCount.value === 1 ? 'file' : 'files'}`)
  }
  if (duplicateCount.value > 0) {
    parts.push(`confirm ${duplicateCount.value} ${duplicateCount.value === 1 ? 'replacement' : 'replacements'}`)
  }
  if (parts.length === 0) {
    return ''
  }
  const sentence = parts.join(' and ')
  return sentence.charAt(0).toUpperCase() + sentence.slice(1) + ' to upload'
})

const commitDetail = computed(() => {
  const replacing = listFiles.value.filter(f => f.isDuplicate || f.replaceConfirmed).length
  const fresh = listFiles.value.filter(f => f.isValid && !f.isDuplicate && !f.replaceConfirmed).length
  const parts: string[] = []
  if (fresh > 0) {
    parts.push(`${fresh} new ${fresh === 1 ? 'source' : 'sources'}`)
  }
  if (replacing > 0) {
    parts.push(`${replacing} replaced`)
  }
  if (manifestFile.value) {
    parts.push('evaluation order will be applied')
  }
  return parts.join(' · ')
})

// the bulk form of the per-file confirmation, for re-importing a whole directory
const replaceAllDuplicates = () => {
  eventFiles.value = eventFiles.value.map(file =>
    (file.isDuplicate ? { ...file, isDuplicate: false, replaceConfirmed: true } : file))
}

const clearQueue = () => {
  eventFiles.value = []
  activeFilter.value = 'all'
}

const openFolderDialog = () => {
  eventFolderInput.value?.click()
}

/**
 * Both pickers and the drop zone feed the same queue under the same rules: .xml only, the first
 * queued basename wins, every file is validated, and one matching an existing source is flagged
 * so the user can rename it or confirm the replace. There is no count limit; selections
 * accumulate until Upload, so a folder of event files can be combined with an eventconf.xml
 * from elsewhere.
 */
const queueFileList = async (allFiles: File[], emptyMessage: string) => {
  const files = allFiles.filter(f =>
    f.name.endsWith('.xml')
  )

  if (files.length === 0) {
    snackbar.showSnackBar({
      msg: emptyMessage,
      error: true
    })
    return
  }

  queueingCount.value++
  try {
    for (const file of files) {
      try {
        if (isDuplicateFile(file.name, eventFiles.value)) {
          continue
        }

        const validation = await validateEventConfigFile(file)

        eventFiles.value.push({
          file,
          isValid: validation.isValid,
          errors: validation.errors,
          isDuplicate: matchesExistingSource(file.name),
          manifestEntries: validation.eventFileEntries
        })

        if (!validation.isValid) {
          snackbar.showSnackBar({
            msg: `Error processing ${file.name}`,
            error: true
          })
        }

      } catch (_err) {
        snackbar.showSnackBar({
          msg: `Error reading ${file.name}`,
          error: true
        })
      }
    }
  } finally {
    queueingCount.value--
  }
}

const queueFiles = async (e: Event, emptyMessage: string) => {
  const input = e.target as HTMLInputElement
  if (!input.files || input.files.length === 0) {
    console.warn(emptyMessage)
    return
  }
  await queueFileList(Array.from(input.files), emptyMessage)
  // Reset the input value to allow re-selecting the same files if needed
  input.value = ''
  input.files = null
}

const handleFolderUpload = (e: Event) => queueFiles(e, 'Folder contains no .xml files')

const handleEventConfUpload = (e: Event) => queueFiles(e, 'No .xml files selected')

// Drag-and-drop is the folder path without the browser's "upload N files?" interstitial:
// the drop gesture itself is the consent, so the files queue silently.
const dragDepth = ref(0)
const onDragEnter = () => {
  dragDepth.value++
}
const onDragLeave = () => {
  dragDepth.value = Math.max(0, dragDepth.value - 1)
}
const onDrop = async (e: DragEvent) => {
  dragDepth.value = 0
  if (!e.dataTransfer) {
    return
  }
  const files = await collectDroppedFiles(e.dataTransfer)
  await queueFileList(files, 'The drop contained no .xml files')
}

const openFileDialog = () => {
  eventConfFileInput.value?.click()
}

const openFileRenameDialog = (index: number) => {
  displayRenameDialog.value = true
  selectedIndex.value = index
}

const openRenameFor = (entry: UploadEventFileType) => {
  openFileRenameDialog(eventFiles.value.indexOf(entry))
}

const closeRenameDialog = () => {
  displayRenameDialog.value = false
  selectedIndex.value = null
}

const renameFile = async (newFileName: string) => {
  if (selectedIndex.value !== null && selectedIndex.value >= 0 && selectedIndex.value < eventFiles.value.length) {
    const fileToRename = eventFiles.value[selectedIndex.value]
    const newFile = new File([fileToRename.file], newFileName, { type: fileToRename.file.type })
    const validationResult = await validateEventConfigFile(newFile)
    eventFiles.value[selectedIndex.value] = {
      file: newFile,
      isValid: validationResult.isValid,
      errors: validationResult.errors,
      isDuplicate: matchesExistingSource(newFileName)
    }
    closeRenameDialog()
  } else {
    console.error('Invalid index for renaming file')
    snackbar.showSnackBar({
      msg: 'Error renaming file',
      error: true
    })
  }
}

const overwriteFile = () => {
  if (selectedIndex.value !== null && selectedIndex.value >= 0 && selectedIndex.value < eventFiles.value.length) {
    eventFiles.value[selectedIndex.value].isDuplicate = false
    eventFiles.value[selectedIndex.value].replaceConfirmed = true
    closeRenameDialog()
  } else {
    console.error('Invalid index for overwriting file')
    snackbar.showSnackBar({
      msg: 'Error overwriting file',
      error: true
    })
  }
}

const removeFileEntry = (entry: UploadEventFileType) => {
  eventFiles.value = eventFiles.value.filter(file => file !== entry)
}

const uploadFiles = async () => {
  if (eventFiles.value.length === 0) {
    console.warn('No files to upload')
    return
  }
  if (!eventFiles.value.every(f => f.file.name.endsWith('.xml'))) {
    snackbar.showSnackBar({
      msg: 'All files must be XML files with .xml extension',
      error: true
    })
    return
  }
  isLoading.value = true
  try {
    const response = await uploadEventConfigFiles(eventFiles.value.filter(f => f.isValid).map(f => f.file))
    uploadFilesReport.value = {
      errors: [...response.errors],
      success: [...response.success]
    }
    isLoading.value = false
    eventFiles.value = []
    activeFilter.value = 'all'
    eventConfFileInput.value!.value = ''
    store.uploadedEventConfigFilesReportDialogState.visible = true
  } catch (err) {
    console.error(err)
    isLoading.value = false
    snackbar.showSnackBar({
      msg: 'Error uploading files',
      error: true
    })
  }
}

// The dialog's visibility lives in the store while the report lives here: leaving the page
// with the dialog open must not pop an empty report on the next visit.
onUnmounted(() => {
  store.uploadedEventConfigFilesReportDialogState.visible = false
})

watch(
  () => store.uploadedSources,
  () => {
    // refreshed source names re-evaluate the flags, but never resurrect a question the
    // user already answered with Overwrite or Replace all
    eventFiles.value = eventFiles.value.map(file => ({
      ...file,
      isDuplicate: !file.replaceConfirmed && matchesExistingSource(file.file.name)
    }))
  }, { immediate: true, deep: true }
)
</script>

<style scoped lang="scss">
.upload-files-tab {
  background: var(--p-content-background);
  width: 100%;
  padding: 25px;
  border-radius: 5px;
  margin-top: 10px;

  .upload-section {
    display: flex;
    flex-direction: column;
    gap: 16px;
    padding: 20px;

    .header-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      flex-wrap: wrap;
      gap: 10px;

      h2 {
        margin: 0;
      }
    }

    .info-section {
      font-size: 0.9rem;
    }

    // the box tracks its content: an invite when empty, growing with the queue; the page is
    // the only scroller so the chips and commit bar can pin to the viewport (like reorder mode)
    .selected-files-section {
      border: 1px solid var(--p-content-border-color);
      border-radius: 5px;
      padding: 10px;
      width: 100%;

      &.drag-active {
        border: 1px dashed var(--p-primary-color);
        background: var(--p-content-hover-background);
      }

      .empty-state {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 6px;
        min-height: 240px;
        padding: 24px;
        text-align: center;

        p {
          margin: 0;
        }

        .empty-cloud {
          color: var(--p-primary-color);
          margin-bottom: 6px;
        }

        .empty-headline {
          font-size: 1.15rem;
          font-weight: 600;
        }

        .empty-links {
          display: flex;
          align-items: center;
          gap: 2px;
          color: var(--p-text-muted-color);
        }

        .empty-hint {
          max-width: 42em;
          font-size: 0.85rem;
          color: var(--p-text-muted-color);
        }
      }

      .add-more-row {
        display: flex;
        align-items: center;
        gap: 4px;
        padding: 6px 10px;
        margin-bottom: 8px;
        border: 1px dashed var(--p-content-border-color);
        border-radius: 5px;
        font-size: 0.85rem;
        color: var(--p-text-muted-color);

        button {
          margin: 0;
        }
      }

      // the counts, filters and Replace all stay visible while the queue scrolls under them,
      // parked just below the fixed navbar
      .chips-row {
        position: sticky;
        top: var(--onms-header-height, 3.75rem);
        z-index: 2;
        background: var(--p-content-background);
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 8px;
        margin: 0 -10px 10px -10px;
        padding: 8px 12px;
        border-bottom: 1px solid var(--p-content-border-color);

        .files-count {
          font-size: 0.9rem;
          font-weight: 600;
          margin-right: 4px;
        }

        .chips-spacer {
          flex-grow: 1;
        }

        .chip {
          display: inline-flex;
          align-items: center;
          gap: 5px;
          padding: 4px 12px;
          border-radius: 999px;
          border: 1px solid transparent;
          font-size: 0.82rem;
          font-weight: 600;
          cursor: pointer;
          background: var(--p-content-hover-background);

          svg {
            height: 1.1em;
            width: 1.1em;
          }

          &.active {
            border-color: currentColor;
          }
        }

        .chip-ready {
          color: #0B720C;
          background: #0B720C1F;
        }

        .chip-replace {
          color: #8A4B00;
          background: #B458091F;
        }

        .chip-invalid {
          color: #A1251D;
          background: #B3261E1A;
        }
      }

      .manifest-callout {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 8px 12px;
        margin-bottom: 8px;
        border: 1px solid var(--p-content-border-color);
        border-left: 4px solid var(--p-primary-color);
        border-radius: 5px;
        background: var(--p-content-hover-background);
        font-size: 0.9rem;

        svg {
          color: var(--p-primary-color);
          flex-shrink: 0;
          height: 1.4em;
          width: 1.4em;
        }

        .manifest-text {
          flex-grow: 1;
        }

        button {
          margin: 0;
          flex-shrink: 0;
        }
      }

      .filter-empty {
        padding: 16px;
        text-align: center;
        font-size: 0.9rem;
        color: var(--p-text-muted-color);
      }

      .file {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 10px;
        border-bottom: 1px solid var(--p-content-border-color);
        margin-bottom: 5px;

        .file-icon {
          display: flex;
          align-items: center;
          gap: 10px;

          svg {
            font-size: 1.5rem;
          }

          span {
            font-size: 1rem;
          }

          .invalid-text {
            color: var(--onms-error);
          }
        }

        .actions {
          display: flex;
          align-items: center;
          gap: 10px;

          button {
            margin: 0px;
          }

          // Status indicators: plain icons, so these land on the <svg>.
          .success-icon {
            color: var(--onms-success);
            cursor: pointer;
            height: 2em;
            width: 2em;
          }

          .error-icon {
            color: var(--onms-error);
            cursor: pointer;
            height: 2em;
            width: 2em;
          }

          // The duplicate indicator IS actionable (it opens the rename dialog), so
          // it is an icon button and this lands on the <button>. Colour only: the
          // glyph inherits it via `fill: currentColor`, and the component sizes it
          // to match the reorder/remove buttons beside it.
          .warning-icon {
            color: var(--onms-major);
          }
        }
      }
    }

    // the commit bar says what the upload will do — or exactly why it cannot run yet —
    // and stays pinned to the viewport while the page scrolls
    .commit-bar {
      position: sticky;
      bottom: 0;
      z-index: 2;
      background: var(--p-content-background);
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 16px;
      border: 1px solid var(--p-content-border-color);
      border-radius: 8px;
      box-shadow: 0 -6px 10px -10px rgba(0, 0, 0, 0.35);

      .commit-summary {
        flex-grow: 1;
        min-width: 0;

        .commit-headline {
          font-weight: 600;

          &.blocked {
            color: var(--onms-major);
          }
        }

        .commit-detail {
          font-size: 0.85rem;
          color: var(--p-text-muted-color);
        }
      }

      button {
        margin: 0;
        white-space: nowrap;

        :deep(.spinner) {
          height: 1.5rem !important;
          width: 1.5rem !important;
        }
      }
    }

    .hidden-input {
      display: none;
    }
  }
}
</style>
