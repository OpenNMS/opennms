<template>
  <div class="upload-files-tab">
    <div class="action-bar">
      <FeatherButton
        secondary
        data-test="upload-button"
        @click="fileInput?.click()"
        :disabled="isLoading"
      >
        Upload MIB Files
      </FeatherButton>
      <input
        type="file"
        :accept="VALID_FILE_EXTENSION"
        multiple
        @change="handleUpload"
        data-test="event-conf-upload-input"
        ref="fileInput"
        :disabled="isLoading"
      />
    </div>
    <div class="files">
      <div
        v-for="(mibFile, index) in mibFiles"
        :key="index"
        class="file"
      >
        <div class="text">
          <FeatherIcon :icon="Generic" />
          <p
            :title="mibFile.file.name"
            class="name"
          >
            {{ ellipsify(mibFile.file.name, 30) }}
          </p>
        </div>
        <div class="action">
          <FeatherTooltip
            v-if="mibFile.isDuplicate"
            :title="'File is a duplicate of another file that has been already uploaded.'"
            v-slot="{ attrs, on }"
          >
            <FeatherIcon
              :icon="Warning"
              v-bind="attrs"
              v-on="on"
              class="warning-icon"
            />
          </FeatherTooltip>
          <FeatherTooltip
            v-if="mibFile.isValid && !mibFile.isDuplicate"
            :title="'File is valid'"
            v-slot="{ attrs, on }"
          >
            <FeatherIcon
              :icon="CheckCircle"
              v-bind="attrs"
              v-on="on"
              class="success-icon"
            />
          </FeatherTooltip>
          <FeatherTooltip
            v-if="!mibFile.isValid"
            :title="mibFile.errors.map((error: string) => `${error}. `).join('\n')"
            v-slot="{ attrs, on }"
          >
            <FeatherIcon
              :icon="Error"
              v-bind="attrs"
              v-on="on"
              class="error-icon"
            />
          </FeatherTooltip>
          <FeatherButton
            text
            icon="Remove"
            data-test="remove-file-button"
            @click="removeFile(index)"
          >
            <FeatherIcon :icon="Cancel" />
          </FeatherButton>
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
import useSnackbar from '@/composables/useSnackbar'
import { ellipsify } from '@/lib/utils'
import { uploadMib } from '@/services/mibCompilerService'
import { useMibCompilerStore } from '@/stores/mibCompilerStore'
import { UploadMibFileType } from '@/types/mibCompiler'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import CheckCircle from '@featherds/icon/action/CheckCircle'
import Generic from '@featherds/icon/file/Generic'
import Cancel from '@featherds/icon/navigation/Cancel'
import Warning from '@featherds/icon/notification/Warning'
import { FeatherTooltip } from '@featherds/tooltip'
import { mibFilesValidator, VALID_FILE_EXTENSION } from './mibFilesValidator'

const isLoading = ref(false)
const snackbar = useSnackbar()
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

const formatTimestamp = (date: Date): string => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const addLog = (type: 'info' | 'success' | 'error', message: string) => {
  logs.value.push({
    type,
    timestamp: formatTimestamp(new Date()),
    message
  })

  void scrollLogsToBottom()
}

const removeFile = (index: number) => {
  mibFiles.value.splice(index, 1)
}

const handleUpload = async (e: Event) => {
  const input = e.target as HTMLInputElement
  if (!input.files || input.files.length === 0) {
    snackbar.showSnackBar({
      msg: 'No files selected.',
      error: true
    })
    addLog('info', 'No files selected.')
    return
  }

  isLoading.value = true
  const exts = VALID_FILE_EXTENSION.split(',').map(ext => ext.trim().toLowerCase())
  const files = Array.from(input.files).filter(f => {
    const fileExt = f.name.split('.').pop()?.toLowerCase() || ''
    const isValidExt = exts.includes(`.${fileExt}`)
    if (!isValidExt) {
      addLog('error', `${f.name} - Invalid file type`)
    }
    return isValidExt
  })
  if (files && files.length > 0) {
    addLog('info', `Processing ${files.length} file(s)...`)
    for (const file of files) {
      try {
        const { isValid, errors } = await mibFilesValidator(file)
        const fileNames = new Set([
          ...mibFiles.value.map(mibFile => mibFile.file.name.toLowerCase()),
          ...store.files.map(source => source.fileName.toLowerCase())
        ])
        const isDuplicate = fileNames.has(file.name.toLowerCase())
        const mibFile: UploadMibFileType = {
          file,
          isValid,
          errors,
          isDuplicate
        }
        mibFiles.value.push(mibFile)
        
        if (isDuplicate) {
          addLog('error', `${file.name} - Duplicate file detected`)
        } else if (!isValid) {
          addLog('error', `${file.name} - Validation failed: ${errors.join(', ')}`)
        } else {
          addLog('success', `${file.name} - Valid and ready for upload`)
        }

        addLog('info', `${file.name} - Uploading file...`)
        await uploadMib(file, file.name)
        addLog('success', `File uploaded successfully - ${file.name} - ${file.size / 1024} KB`)
        isLoading.value = false
      } catch (error) {
        addLog('error', `${file.name} - Error processing file`)
        isLoading.value = false
      }
    }
    input.value = ''
    input.files = null
  } else {
    addLog('info', 'No valid MIB files selected')
    isLoading.value = false
  }
}
</script>

<style scoped lang="scss">
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';

.upload-files-tab {
  background: var(variables.$surface);
  width: 100%;
  padding: 25px;
  border-radius: 5px;
  margin-top: 10px;

  .action-bar {
    display: flex;
    margin-bottom: 20px;

    input[type="file"] {
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
      padding: 25px;
      border: 1px solid var(variables.$border-on-surface);
      box-shadow:
        0px 1px 5px 0px rgba(0, 0, 0, 0.12),
        0px 2px 2px 0px rgba(0, 0, 0, 0.14),
        0px 3px 1px -2px rgba(0, 0, 0, 0.2);


      .text {
        display: flex;
        align-items: center;
        gap: 10px;

        svg {
          font-size: 24px;
        }

        p {
          @include typography.headline3;
          margin: 0;
        }
      }

      .action {
        display: flex;
        align-items: center;
        gap: 5px;

        .success-icon {
          color: var(variables.$success);
          cursor: pointer;
          height: 2em;
          width: 2em;
        }

        .error-icon {
          color: var(variables.$error);
          cursor: pointer;
          height: 2em;
          width: 2em;
        }

        .warning-icon {
          color: var(variables.$major);
          cursor: pointer;
          height: 2em;
          width: 2em;
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
        @include typography.headline2;
        margin: 0;
      }
    }

    .logs {
      margin-top: 10px;
      width: 100%;
      height: 500px;
      background: var(variables.$background);
      border-radius: 5px;
      border: 1px solid var(variables.$border-on-surface);
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
        }

        &.info {
          background-color: rgba(33, 150, 243, 0.1);
          border-left: 3px solid #2196f3;
          color: #1565c0;

          .log-type {
            color: #2196f3;
          }
        }

        &.success {
          background-color: rgba(76, 175, 80, 0.1);
          border-left: 3px solid #4caf50;
          color: #2e7d32;

          .log-type {
            color: #4caf50;
          }
        }

        &.error {
          background-color: rgba(244, 67, 54, 0.1);
          border-left: 3px solid #f44336;
          color: #c62828;

          .log-type {
            color: #f44336;
          }
        }
      }
    }
  }
}
</style>

