<template>
  <div class="mib-compiler-container">
    <div class="row">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
    <div class="header">
      <div class="heading">
        <p>SNMP MIB Compiler</p>
      </div>
    </div>
    <div class="tab-container">
      <OnmsTabs
        :value="activeTab"
        @update:value="(value: string | number) => (activeTab = Number(value))"
      >
        <OnmsTabList>
          <OnmsTab :value="0">View</OnmsTab>
          <OnmsTab :value="1">Upload MIB Files</OnmsTab>
        </OnmsTabList>
        <OnmsTabPanels>
          <OnmsTabPanel :value="0">
            <MibFilesTable
              location="pending"
              title="Pending MIB Files"
              :files="store.pendingFiles"
              @view="viewFile('pending', $event)"
              @edit="editFile"
              @compile="compileFile"
              @delete="confirmDelete('pending', $event)"
            />
            <MibFilesTable
              location="compiled"
              title="Compiled MIB Files"
              :files="store.compiledFiles"
              @view="viewFile('compiled', $event)"
              @delete="confirmDelete('compiled', $event)"
              @generate-events="openGenerateDialog('events', $event)"
              @generate-data-collection="openGenerateDialog('datacollection', $event)"
              @generate-graphs="openGenerateDialog('graphs', $event)"
            />
          </OnmsTabPanel>
          <OnmsTabPanel :value="1">
            <UploadMibFiles />
          </OnmsTabPanel>
        </OnmsTabPanels>
      </OnmsTabs>
    </div>

    <MibFileViewerDrawer
      :visible="viewer.visible"
      :fileName="viewer.fileName"
      :content="viewer.content"
      @close="viewer.visible = false"
    />

    <OnmsConfirmationDialog
      :visible="deleteDialog.visible"
      title="Delete MIB File"
      actionButtonText="Delete"
      cancelButtonText="Cancel"
      @ok="deleteFile"
      @cancel="deleteDialog.visible = false"
    >
      <p data-test="delete-confirmation-message">
        Are you sure you want to delete '{{ deleteDialog.file?.name }}' from the {{ deleteDialog.location }} directory?
      </p>
    </OnmsConfirmationDialog>

    <OnmsConfirmationDialog
      :visible="overwriteDialog.visible"
      title="Compiled MIB already exists"
      actionButtonText="Overwrite"
      cancelButtonText="Cancel"
      @ok="compileWithOverwrite"
      @cancel="overwriteDialog.visible = false"
    >
      <p data-test="overwrite-confirmation-message">
        A compiled MIB named '{{ overwriteDialog.targetFile }}' already exists.
        Do you want to overwrite it with the contents of '{{ overwriteDialog.pendingFile }}'?
      </p>
    </OnmsConfirmationDialog>

    <CompileErrorsDialog
      :visible="errorsDialog.visible"
      :fileName="errorsDialog.fileName"
      :result="errorsDialog.result"
      @close="errorsDialog.visible = false"
    />

    <GenerateEventsDialog
      :visible="generateDialog.type === 'events'"
      :fileName="generateDialog.fileName"
      @close="generateDialog.type = null"
      @failed="showGenerateFailure"
    />
    <GenerateDataCollectionDialog
      :visible="generateDialog.type === 'datacollection'"
      :fileName="generateDialog.fileName"
      @close="generateDialog.type = null"
      @failed="showGenerateFailure"
    />
    <GenerateGraphsDialog
      :visible="generateDialog.type === 'graphs'"
      :fileName="generateDialog.fileName"
      @close="generateDialog.type = null"
      @failed="showGenerateFailure"
    />
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  OnmsConfirmationDialog,
  OnmsTab,
  OnmsTabList,
  OnmsTabPanel,
  OnmsTabPanels,
  OnmsTabs
} from '@opennms/onms-ui'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import CompileErrorsDialog from '@/components/MibCompiler/Dialog/CompileErrorsDialog.vue'
import GenerateDataCollectionDialog from '@/components/MibCompiler/Dialog/GenerateDataCollectionDialog.vue'
import GenerateEventsDialog from '@/components/MibCompiler/Dialog/GenerateEventsDialog.vue'
import GenerateGraphsDialog from '@/components/MibCompiler/Dialog/GenerateGraphsDialog.vue'
import MibFilesTable from '@/components/MibCompiler/MibFilesTable.vue'
import MibFileViewerDrawer from '@/components/MibCompiler/MibFileViewerDrawer.vue'
import UploadMibFiles from '@/components/MibCompiler/UploadMibFiles.vue'
import useSnackbar from '@/composables/useSnackbar'
import { getGeneralErrorMessage } from '@/components/MibCompiler/mibFilesValidator'
import {
  compileMibFile,
  deleteMibFile,
  getMibFileContent
} from '@/services/mibCompilerService'
import { useMenuStore } from '@/stores/menuStore'
import { useMibCompilerStore } from '@/stores/mibCompilerStore'
import { BreadCrumb } from '@/types'
import { MibDirectory, MibFileInfo, MibParseResult } from '@/types/mibCompiler'
import axios from 'axios'

const router = useRouter()
const menuStore = useMenuStore()
const store = useMibCompilerStore()
const snackbar = useSnackbar()

const activeTab = ref(0)
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)
const breadcrumbs = computed<BreadCrumb[]>(() => ([
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: false },
  { label: 'MIB Compiler', to: '#', position: 'last' }
]))

const viewer = reactive({ visible: false, fileName: '', content: '' })
const deleteDialog = reactive<{ visible: boolean, location: MibDirectory, file: MibFileInfo | null }>({
  visible: false,
  location: 'pending',
  file: null
})
const overwriteDialog = reactive({ visible: false, pendingFile: '', targetFile: '' })
const errorsDialog = reactive<{ visible: boolean, fileName: string, result: MibParseResult | null }>({
  visible: false,
  fileName: '',
  result: null
})
const generateDialog = reactive<{ type: 'events' | 'datacollection' | 'graphs' | null, fileName: string }>({
  type: null,
  fileName: ''
})

const viewFile = async (location: MibDirectory, file: MibFileInfo) => {
  try {
    viewer.fileName = file.name
    viewer.content = await getMibFileContent(location, file.name)
    viewer.visible = true
  } catch (error: unknown) {
    snackbar.showSnackBar({ msg: getGeneralErrorMessage(error, `Failed to load '${file.name}'.`), error: true })
  }
}

const editFile = (file: MibFileInfo) => {
  router.push({ path: '/mib-compiler/edit', query: { name: file.name }})
}

const confirmDelete = (location: MibDirectory, file: MibFileInfo) => {
  deleteDialog.location = location
  deleteDialog.file = file
  deleteDialog.visible = true
}

const deleteFile = async () => {
  const { location, file } = deleteDialog
  deleteDialog.visible = false
  if (!file) {
    return
  }
  try {
    await deleteMibFile(location, file.name)
    snackbar.showSnackBar({ msg: `'${file.name}' deleted successfully.` })
    await store.fetchMibFiles()
  } catch (error: unknown) {
    snackbar.showSnackBar({ msg: getGeneralErrorMessage(error, `Failed to delete '${file.name}'.`), error: true })
  }
}

const compileFile = async (file: MibFileInfo) => {
  try {
    const result = await compileMibFile(file.name, false)
    if (result.success) {
      snackbar.showSnackBar({ msg: `'${file.name}' compiled successfully as '${result.targetFile}'.` })
      await store.fetchMibFiles()
    } else {
      errorsDialog.fileName = file.name
      errorsDialog.result = result
      errorsDialog.visible = true
    }
  } catch (error: unknown) {
    if (axios.isAxiosError(error) && error.response?.status === 409) {
      overwriteDialog.pendingFile = file.name
      overwriteDialog.targetFile = (error.response.data as { targetFile?: string })?.targetFile ?? ''
      overwriteDialog.visible = true
      return
    }
    snackbar.showSnackBar({ msg: getGeneralErrorMessage(error, `Failed to compile '${file.name}'.`), error: true })
  }
}

const compileWithOverwrite = async () => {
  const pendingFile = overwriteDialog.pendingFile
  overwriteDialog.visible = false
  try {
    const result = await compileMibFile(pendingFile, true)
    if (result.success) {
      snackbar.showSnackBar({ msg: `'${pendingFile}' compiled successfully as '${result.targetFile}'.` })
      await store.fetchMibFiles()
    } else {
      errorsDialog.fileName = pendingFile
      errorsDialog.result = result
      errorsDialog.visible = true
    }
  } catch (error: unknown) {
    snackbar.showSnackBar({ msg: getGeneralErrorMessage(error, `Failed to compile '${pendingFile}'.`), error: true })
  }
}

const openGenerateDialog = (type: 'events' | 'datacollection' | 'graphs', file: MibFileInfo) => {
  generateDialog.fileName = file.name
  generateDialog.type = type
}

const showGenerateFailure = (result: MibParseResult) => {
  const fileName = generateDialog.fileName
  generateDialog.type = null
  errorsDialog.fileName = fileName
  errorsDialog.result = result
  errorsDialog.visible = true
}

onMounted(async () => {
  await store.fetchMibFiles()
})
</script>

<style lang="scss" scoped>
.mib-compiler-container {
  padding: 20px;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 30px 40px 15px 40px;

    .heading {
      p {
        font-size: 24px;
        font-weight: 600;
        margin: 0;
      }
    }
  }

  .tab-container {
    padding: 0 40px;
  }
}
</style>
