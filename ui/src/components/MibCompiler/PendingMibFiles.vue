<template>
  <TableCard class="pending-mib-files-container">
    <div class="header">
      <div class="section-left">
        <h3>Pending MIB Files</h3>
      </div>
      <div class="section-right">
        <FeatherInput
          label="Search MIBs"
          placeholder="Search Pending MIB Files"
          :modelValue="store.pendingMibFilesSearchTerm"
          @update:modelValue="store.onPendingMibFilesSearchChange"
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
        aria-label="Pending MIB Files Table"
      >
        <thead>
          <tr>
            <FeatherSortHeader
              v-for="col of columns"
              :key="col.label"
              scope="col"
              :property="col.id"
              :sort="store.pendingMibFilesSort.property === col.id ? store.pendingMibFilesSort.value : SORT.NONE"
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
            v-for="config in store.paginatedPendingMibFiles"
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
                  icon="Edit"
                  data-test="edit-button"
                  @click="onEditClick(config)"
                >
                  <FeatherIcon :icon="Edit" />
                </FeatherButton>
                <FeatherButton
                  icon="View Details"
                  data-test="view-button"
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
              </div>
            </td>
          </tr>
        </TransitionGroup>
      </table>
      <div
        class="alerts-pagination"
        v-if="store.searchedPendingMibFiles.length"
      >
        <FeatherPagination
          :modelValue="store.pendingMibFilesPagination.page"
          :pageSize="store.pendingMibFilesPagination.pageSize"
          :total="store.searchedPendingMibFiles.length"
          :pageSizes="[10, 20, 50, 100, 200]"
          @update:modelValue="store.onPendingMibFilesPageChange"
          @update:pageSize="store.onPendingMibFilesPageSizeChange"
          data-test="FeatherPagination"
        />
      </div>
      <div v-if="!store.searchedPendingMibFiles.length">
        <EmptyList
          :content="emptyListContent"
          data-test="empty-list"
        />
      </div>
    </div>
    <ConfirmationDialog
      @ok="onDeleteConfirm"
      @cancel="onDeleteCancel"
      title="Delete Pending MIB File"
      :visible="deleteDialogVisible"
    >
      <template #content>
        Are you sure you want to delete this pending MIB file name:
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
  </TableCard>
</template>

<script setup lang="ts">
import useSnackbar from '@/composables/useSnackbar'
import { deleteFile, getFileText } from '@/services/mibCompilerService'
import { useMibCompilerStore } from '@/stores/mibCompilerStore'
import { MibCompilerFileInfo } from '@/types/mibCompiler'
import { FeatherButton } from '@featherds/button'
import Delete from '@featherds/icon/action/Delete'
import Edit from "@featherds/icon/action/Edit"
import Search from '@featherds/icon/action/Search'
import StackedBarChart from '@featherds/icon/datavis/StackedBarChart'
import Generic from '@featherds/icon/file/Generic'
import FeatherIcon from '@featherds/icon/src/components/FeatherIcon.vue'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSortHeader, SORT } from '@featherds/table'
import ConfirmationDialog from '../Common/ConfirmationDialog.vue'
import EmptyList from '../Common/EmptyList.vue'
import TableCard from '../Common/TableCard.vue'
import FileText from './Drawer/FileText.vue'
import { FOLDER_LOCATIONS } from './mibFilesValidator'

const router = useRouter()
const store = useMibCompilerStore()
const { showSnackBar } = useSnackbar()
const deleteDialogVisible = ref(false)
const textDrawerVisible = ref(false)
const selectedFile = ref<MibCompilerFileInfo | null>(null)
const fileText = ref('')
const emptyListContent = {
  msg: 'No results found.'
}

const columns = computed(() => [
  { id: 'fileName', label: 'MIB File' }
])

const onEditClick = async (file: MibCompilerFileInfo) => {
  if (!file.fileName) {
    showSnackBar({ msg: 'No file selected for editing.', error: true })
    return
  }

  try {
    const response = await getFileText(FOLDER_LOCATIONS.PENDING, file.fileName)
    store.setSelectedMibFile(response)
    router.push('/mib-compiler/edit')
  } catch (error) {
    showSnackBar({ msg: 'Failed to load pending MIB file details.', error: true })
  }
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
    await deleteFile(FOLDER_LOCATIONS.PENDING, selectedFile.value.fileName)
    await store.fetchMibFiles()
    showSnackBar({ msg: 'Pending MIB file deleted successfully.' })
  } catch (error) {
    showSnackBar({ msg: 'Failed to delete pending MIB file.', error: true })
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
    const response = await getFileText(FOLDER_LOCATIONS.PENDING, file.fileName)
    fileText.value = response.contents
    textDrawerVisible.value = true
  } catch (error) {
    showSnackBar({ msg: 'Failed to load pending MIB file details.', error: true })
  }
}

const onCloseTextDrawer = () => {
  fileText.value = ''
  selectedFile.value = null
  textDrawerVisible.value = false
}

const sortChanged = (sortObj: { property: string; value: SORT }) => {
  store.onPendingMibFilesSortChange({
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

.pending-mib-files-container {
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
</style>

