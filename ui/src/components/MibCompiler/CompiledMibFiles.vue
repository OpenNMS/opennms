<template>
  <TableCard class="compiled-mib-files-container">
    <div class="header">
      <div class="section-left">
        <h3>Compiled MIB Files</h3>
      </div>
      <div class="section-right">
        <FeatherInput
          label="Search MIBs"
          placeholder="Search Compiled MIB Files"
          :modelValue="store.compiledMibFilesSearchTerm"
          @update:modelValue="store.onCompiledMibFilesSearchChange"
        >
          <template #pre>
            <FeatherIcon :icon="Search" />
          </template>
        </FeatherInput>
      </div>
    </div>
    <div class="container">
      <table
        class="data-table"
        aria-label="Compiled MIB Files Table"
      >
        <thead>
          <tr>
            <FeatherSortHeader
              v-for="col of columns"
              :key="col.label"
              scope="col"
              :property="col.id"
              :sort="store.compiledMibFilesSort.property === col.id ? store.compiledMibFilesSort.value : SORT.NONE"
              v-on:sort-changed="sortChanged"
            >
              {{ col.label }}
            </FeatherSortHeader>
            <th>Actions</th>
          </tr>
        </thead>
        <TransitionGroup
          name="data-table"
          tag="tbody"
        >
          <tr
            v-for="config in store.paginatedCompiledMibFiles"
            :key="config.fileName"
          >
            <td>
              <div
                class="hyperlink"
                @click="onViewDetailsClick(config)"
                data-test="file-name"
              >
                {{ config.fileName }}
              </div>
            </td>
            <td>
              <div class="action-container">
                <FeatherButton
                  icon="Generate Events"
                  data-test="generate-events-button"
                  @click="onGenerateEventsClick(config)"
                >
                  <FeatherIcon :icon="Generic" />
                </FeatherButton>
                <FeatherButton
                  icon="View Details"
                  data-test="view-button"
                >
                  <FeatherIcon :icon="StackedBarChart" />
                </FeatherButton>
                <FeatherButton
                  icon="Delete"
                  data-test="delete-button"
                  @click="onDeleteClick(config)"
                >
                  <FeatherIcon :icon="Delete" />
                </FeatherButton>
                <FeatherButton
                  icon="Download File"
                  data-test="download-button"
                  @click="onDownloadClick(config)"
                >
                  <FeatherIcon :icon="DownloadFile" />
                </FeatherButton>
              </div>
            </td>
          </tr>
        </TransitionGroup>
      </table>
      <div
        class="alerts-pagination"
        v-if="store.searchedCompiledMibFiles.length"
      >
        <FeatherPagination
          :modelValue="store.compiledMibFilesPagination.page"
          :pageSize="store.compiledMibFilesPagination.pageSize"
          :total="store.searchedCompiledMibFiles.length"
          :pageSizes="[10, 20, 50, 100, 200]"
          @update:modelValue="store.onCompiledMibFilesPageChange"
          @update:pageSize="store.onCompiledMibFilesPageSizeChange"
          data-test="FeatherPagination"
        />
      </div>
      <div v-if="!store.searchedCompiledMibFiles.length">
        <EmptyList
          :content="emptyListContent"
          data-test="empty-list"
        />
      </div>
    </div>
    <ConfirmationDialog
      @ok="onDeleteConfirm"
      @cancel="onDeleteCancel"
      title="Delete Compiled MIB File"
      :visible="deleteDialogVisible"
    >
      <template #content>
        Are you sure you want to delete this compiled MIB file name:
        <strong>{{ selectedFile?.fileName
        }}</strong
        >?
      </template>
    </ConfirmationDialog>
    <FileText
      title="Compiled MIB File Details"
      @hidden="onCloseTextDrawer"
      :visible="textDrawerVisible"
    >
      <template #content>
        <div class="modal-content">
          <div class="header">
            <p>{{ selectedFile?.fileName }}</p>
          </div>
          <div class="subtitle">
            <p>View MIB contents</p>
          </div>
          <div class="content">
            <pre data-test="file-text">
            {{ fileText }}
          </pre
            >
          </div>
        </div>
      </template>
    </FileText>
    <ConfirmationDialog
      @ok="onConfirmGenerateEvents"
      @cancel="onCancelGenerateEvents"
      title="Generate Events from MIB File"
      :visible="generateEventsDialogVisible"
    >
      <template #content>
        <div class="generate-modal-content">
          <p class="heading">Enter the UEI Base for the events to be generated from the selected MIB file.</p>
          <p class="subtitle">File name: {{ selectedFile?.fileName }}</p>
          <FeatherInput
            label="UEI Base"
            v-model="uei"
            :error="ueiError"
          />
        </div>
      </template>
    </ConfirmationDialog>
  </TableCard>
</template>

<script setup lang="ts">
import useSnackbar from '@/composables/useSnackbar'
import { deleteFile, generateEvents, getFileText } from '@/services/mibCompilerService'
import { useMibCompilerStore } from '@/stores/mibCompilerStore'
import { MibCompilerFileInfo } from '@/types/mibCompiler'
import { FeatherButton } from '@featherds/button'
import Delete from '@featherds/icon/action/Delete'
import Search from '@featherds/icon/action/Search'
import StackedBarChart from '@featherds/icon/datavis/StackedBarChart'
import Generic from '@featherds/icon/file/Generic'
import DownloadFile from "@featherds/icon/action/DownloadFile";
import FeatherIcon from '@featherds/icon/src/components/FeatherIcon.vue'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSortHeader, SORT } from '@featherds/table'
import ConfirmationDialog from '../Common/ConfirmationDialog.vue'
import EmptyList from '../Common/EmptyList.vue'
import TableCard from '../Common/TableCard.vue'
import FileText from './Drawer/FileText.vue'
import { FOLDER_LOCATIONS, getGeneralErrorMessage } from './mibFilesValidator'

const store = useMibCompilerStore()
const { showSnackBar } = useSnackbar()
const deleteDialogVisible = ref(false)
const textDrawerVisible = ref(false)
const selectedFile = ref<MibCompilerFileInfo | null>(null)
const generateEventsDialogVisible = ref(false)
const uei = ref('')
const ueiError = ref('')
const fileText = ref('')
const emptyListContent = {
  msg: 'No results found.'
}

const columns = computed(() => [
  { id: 'fileName', label: 'MIB File' }
])

const onDownloadClick = async (file: MibCompilerFileInfo) => {
  if (!file.fileName) {
    showSnackBar({ msg: 'No file selected for download.', error: true })
    return
  }

  try {
    const response = await getFileText(FOLDER_LOCATIONS.COMPILED, file.fileName)
    const blob = new Blob([response.contents], { type: 'text/plain' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = file.fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  } catch (error) {
    showSnackBar({ msg: 'Failed to download compiled MIB file.', error: true })
  }
}

const onGenerateEventsClick = (file: MibCompilerFileInfo) => {
  selectedFile.value = file
  uei.value = 'uei.opennms.org/'
  ueiError.value = ''
  generateEventsDialogVisible.value = true
}

const onConfirmGenerateEvents = async () => {
  if (!selectedFile.value) {
    showSnackBar({ msg: 'No file selected for generating events.', error: true })
    return
  }

  if (!uei.value.trim()) {
    ueiError.value = 'UEI Base is required.'
    return
  }

  if (!uei.value.startsWith('uei.opennms.org/')) {
    ueiError.value = 'UEI Base must start with "uei.opennms.org/".'
    return
  }

  if (uei.value === 'uei.opennms.org/') {
    ueiError.value = 'UEI Base must contain a path after "uei.opennms.org/".'
    return
  }

  if (uei.value.endsWith('/')) {
    ueiError.value = 'UEI Base should not end with a slash.'
    return
  }

  try {
    await generateEvents({
      name: selectedFile.value.fileName,
      ueiBase: uei.value
    })
    showSnackBar({ msg: 'Events generated successfully from the compiled MIB file.', error: false })
  } catch (error) {
    showSnackBar({ msg: getGeneralErrorMessage(error, 'Failed to generate events from the compiled MIB file.'), error: true })
  } finally {
    selectedFile.value = null
    uei.value = ''
    ueiError.value = ''
    generateEventsDialogVisible.value = false
  }
}

const onCancelGenerateEvents = () => {
  selectedFile.value = null
  uei.value = ''
  ueiError.value = ''
  generateEventsDialogVisible.value = false
}

const onDeleteClick = (file: MibCompilerFileInfo) => {
  selectedFile.value = file
  deleteDialogVisible.value = true
}

const onDeleteConfirm = async () => {
  if (!selectedFile.value) {
    showSnackBar({ msg: 'No file selected for deletion.', error: true })
    return
  }

  try {
    await deleteFile(FOLDER_LOCATIONS.COMPILED, selectedFile.value.fileName)
    await store.fetchMibFiles()
    showSnackBar({ msg: 'Compiled MIB file deleted successfully.' })
  } catch (error) {
    showSnackBar({ msg: 'Failed to delete compiled MIB file.', error: true })
  } finally {
    selectedFile.value = null
    deleteDialogVisible.value = false
  }
}

const onDeleteCancel = () => {
  selectedFile.value = null
  deleteDialogVisible.value = false
}

const onViewDetailsClick = async (file: MibCompilerFileInfo) => {
  if (!file.fileName) {
    showSnackBar({ msg: 'No file selected for viewing.', error: true })
    return
  }

  try {
    selectedFile.value = file
    const response = await getFileText(FOLDER_LOCATIONS.COMPILED, file.fileName)
    fileText.value = response.contents
    textDrawerVisible.value = true
  } catch (error) {
    showSnackBar({ msg: 'Failed to load compiled MIB file details.', error: true })
  }
}

const onCloseTextDrawer = () => {
  fileText.value = ''
  selectedFile.value = null
  textDrawerVisible.value = false
}

const sortChanged = (sortObj: { property: string; value: SORT }) => {
  store.onCompiledMibFilesSortChange({
    property: sortObj.property as 'fileName' | 'location',
    value: sortObj.value
  })
}
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';

.compiled-mib-files-container {
  margin-top: 10px;
  margin-bottom: 20px;
  padding: 25px;
  border: 1px solid var(--feather-border-on-surface);

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    div {
      flex: 1;
    }

    :deep(.section-right) {
      .feather-input-container {
        float: right;
        width: 50%;

        .feather-input-sub-text {
          display: none !important;
        }
      }
    }
  }

  .container {
    table {
      width: 100%;
      @include table.table;

      thead {
        background: var(variables.$background);
        text-transform: uppercase;
      }

      td {
        white-space: nowrap;
        box-shadow: none;
        border-bottom: 1px solid var(variables.$border-on-surface);

        div {
          border-radius: 5px;
          padding: 0px 5px 0px 5px;
        }

        .hyperlink {
          @include typography.body-large;
          color: var(variables.$primary);
          cursor: pointer;
        }

        .action-container {
          display: flex;
          align-items: center;
          gap: 5px;

          button {
            margin: 0px;
          }
        }
      }
    }
  }
}

.modal-content {
  display: flex;
  flex-direction: column;
  height: 100%;

  .header {
    p {
      @include typography.headline1;
      padding: 24px;
      border-bottom: 1px solid var(--feather-border-on-surface);
    }
  }

  .subtitle {
    p {
      @include typography.headline3;
      padding: 24px;
    }
  }

  .content {
    flex: 1;
    padding: 0px 24px 24px 24px;
    background: var(variables.$background);
    overflow-y: auto;

    pre {
      white-space: pre-wrap;
      word-break: break-word;
    }
  }
}

.generate-modal-content {
  width: 35rem;

  .heading {
    @include typography.headline4;
    margin-bottom: 1rem;
  }

  .subtitle {
    @include typography.subtitle1;
    margin-bottom: 0.5rem;
  }
}
</style>

