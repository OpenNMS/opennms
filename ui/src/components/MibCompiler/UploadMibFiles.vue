<template>
  <div class="upload-files-tab">
    <div class="action-bar">
      <div>
        <OnmsButton
          variant="outlined"
          label="Upload MIB Files"
          data-test="upload-button"
          :disabled="isLoading"
          @click="fileInput?.click()"
        />
        <input
          type="file"
          :accept="VALID_FILE_EXTENSION.join(',')"
          multiple
          @change="handleUpload"
          data-test="mib-upload-input"
          ref="fileInput"
          :disabled="isLoading"
        />
      </div>
      <div>
        <OnmsButton
          variant="text"
          label="Clear Logs"
          data-test="clear-logs-button"
          :disabled="isLoading || (mibFiles.length === 0 && logs.length === 0)"
          @click="clear"
        />
      </div>
    </div>
    <div class="files">
      <div
        v-for="(mibFile, index) in mibFiles"
        :key="index"
        class="file"
      >
        <div class="text">
          <p
            :title="mibFile.file.name"
            class="name"
          >
            {{ ellipsify(mibFile.file.name, 30) }}
          </p>
        </div>
        <div class="action">
          <span
            v-if="mibFile.isDuplicate"
            class="warning-icon"
            v-onms-tooltip="'File is a duplicate of a MIB that already exists.'"
          >
            <WarningIcon />
          </span>
          <span
            v-if="mibFile.isValid && !mibFile.isDuplicate"
            class="success-icon"
            v-onms-tooltip="'File is valid'"
          >
            <CheckCircleIcon />
          </span>
          <span
            v-if="!mibFile.isValid"
            class="error-icon"
            v-onms-tooltip="mibFile.errors.join('. ')"
          >
            <ErrorIcon />
          </span>
          <OnmsIconButton
            :title="`Remove ${mibFile.file.name}`"
            data-test="remove-file-button"
            :icon="CancelIcon"
            @click="removeFile(index)"
          />
        </div>
      </div>
    </div>
    <div class="logs-container">
      <div class="header">
        <p>MIB Logs</p>
      </div>
      <div
        ref="logsContainerRef"
        class="logs"
        data-test="upload-logs"
      >
        <div
          v-for="(log, index) in logs"
          :key="index"
          :class="['log-entry', log.type]"
        >
          <span class="log-timestamp">{{ log.timestamp }}</span>
          <span class="log-type">{{ log.type.toUpperCase() }}</span>
          <span class="log-message">{{ log.message }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, ref } from 'vue'
import { OnmsButton, OnmsIconButton } from '@opennms/onms-ui'
import { ellipsify } from '@/lib/utils'
import { uploadMibFiles } from '@/services/mibCompilerService'
import { useMibCompilerStore } from '@/stores/mibCompilerStore'
import { UploadMibFileType } from '@/types/mibCompiler'
import CancelIcon from '@opennms/onms-ui/icons/action/Cancel.vue'
import CheckCircleIcon from '@opennms/onms-ui/icons/action/CheckCircle.vue'
import ErrorIcon from '@opennms/onms-ui/icons/notification/Error.vue'
import WarningIcon from '@opennms/onms-ui/icons/notification/Warning.vue'
import { format as fnsFormat } from 'date-fns'
import { getGeneralErrorMessage, isValidMibExtension, mibFilesValidator, VALID_FILE_EXTENSION } from './mibFilesValidator'

const isLoading = ref(false)
const store = useMibCompilerStore()
const fileInput = ref<HTMLInputElement | null>(null)
const mibFiles = ref<UploadMibFileType[]>([])
const logsContainerRef = ref<HTMLElement | null>(null)

interface LogEntry {
  type: 'info' | 'success' | 'error'
  timestamp: string
  message: string
}

const logs = ref<LogEntry[]>([])

const scrollLogsToBottom = async () => {
  await nextTick()
  if (logsContainerRef.value) {
    logsContainerRef.value.scrollTop = logsContainerRef.value.scrollHeight
  }
}

const addLog = (type: 'info' | 'success' | 'error', message: string) => {
  logs.value.push({
    type,
    timestamp: fnsFormat(new Date(), 'yyyy-MM-dd HH:mm:ss'),
    message
  })
  void scrollLogsToBottom()
}

const removeFile = (index: number) => {
  mibFiles.value.splice(index, 1)
}

const clear = () => {
  if (fileInput.value) {
    fileInput.value.value = ''
  }
  mibFiles.value = []
  logs.value = []
}

const handleUpload = async (e: Event) => {
  const input = e.target as HTMLInputElement
  if (!input.files || input.files.length === 0) {
    addLog('info', 'No files selected.')
    return
  }

  isLoading.value = true
  const existingNames = new Set([
    ...store.pendingFiles.map(file => file.name.toLowerCase()),
    ...store.compiledFiles.map(file => file.name.toLowerCase()),
    ...mibFiles.value.map(mibFile => mibFile.file.name.toLowerCase())
  ])

  const toUpload: File[] = []
  for (const file of Array.from(input.files)) {
    if (!isValidMibExtension(file.name)) {
      addLog('error', `${file.name} - Invalid file type. Only ${VALID_FILE_EXTENSION.join(', ')} files are allowed.`)
      continue
    }
    const { isValid, errors } = await mibFilesValidator(file)
    const isDuplicate = existingNames.has(file.name.toLowerCase())
    mibFiles.value.push({ file, isValid, errors, isDuplicate })

    if (isDuplicate) {
      addLog('error', `${file.name} - A MIB with this name already exists`)
    } else if (!isValid) {
      addLog('error', `${file.name} - Validation failed: ${errors.join(', ')}`)
    } else {
      addLog('success', `${file.name} - Valid and ready for upload`)
      toUpload.push(file)
      existingNames.add(file.name.toLowerCase())
    }
  }

  if (toUpload.length > 0) {
    addLog('info', `Uploading ${toUpload.length} file(s)...`)
    try {
      const response = await uploadMibFiles(toUpload)
      for (const successItem of response.success ?? []) {
        addLog('success', `${successItem.file} - Uploaded successfully to the pending directory`)
      }
      for (const errorItem of response.errors ?? []) {
        addLog('error', `${errorItem.file} - ${errorItem.error}`)
      }
      await store.fetchMibFiles()
    } catch (error: unknown) {
      addLog('error', `Upload failed: ${getGeneralErrorMessage(error, 'Unknown error occurred')}`)
    }
  }
  input.value = ''
  isLoading.value = false
}
</script>

<style scoped lang="scss">
.upload-files-tab {
  width: 100%;
  padding: 25px;
  border-radius: 5px;
  margin-top: 10px;
  border: 1px solid var(--p-content-border-color);

  .action-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;

    input[type='file'] {
      display: none;
    }
  }

  .files {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;

    .file {
      display: flex;
      align-items: center;
      justify-content: space-between;
      width: calc((100% - 24px) / 3);
      padding: 15px 25px;
      border: 1px solid var(--p-content-border-color);
      border-radius: 5px;

      .text {
        p {
          margin: 0;
          font-weight: 600;
        }
      }

      .action {
        display: flex;
        align-items: center;
        gap: 5px;

        svg {
          height: 1.5em;
          width: 1.5em;
        }

        .success-icon {
          color: var(--p-green-600);
        }

        .error-icon {
          color: var(--p-red-600);
        }

        .warning-icon {
          color: var(--p-orange-600);
        }
      }

      @media (max-width: 768px) {
        width: calc((100% - 12px) / 2);
      }

      @media (max-width: 480px) {
        width: 100%;
      }
    }
  }

  .logs-container {
    margin-top: 30px;

    .header {
      p {
        margin: 0;
        font-weight: 600;
      }
    }

    .logs {
      margin-top: 10px;
      width: 100%;
      height: 400px;
      border-radius: 5px;
      border: 1px solid var(--p-content-border-color);
      padding: 15px;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: 8px;

      .log-entry {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        padding: 12px;
        border-radius: 4px;
        font-size: 14px;
        font-family: monospace;

        .log-type {
          font-weight: bold;
          min-width: 60px;
          text-transform: uppercase;
        }

        .log-timestamp {
          min-width: 150px;
          white-space: nowrap;
        }

        .log-message {
          flex: 1;
          word-break: break-word;
          white-space: pre-wrap;
        }

        &.info {
          background-color: rgba(33, 150, 243, 0.1);
          border-left: 3px solid #2196f3;
        }

        &.success {
          background-color: rgba(76, 175, 80, 0.1);
          border-left: 3px solid #4caf50;
        }

        &.error {
          background-color: rgba(244, 67, 54, 0.1);
          border-left: 3px solid #f44336;
        }
      }
    }
  }
}
</style>
