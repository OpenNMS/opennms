<template>
  <div class="snmp-config">
    <div class="feather-row">
      <div class="feather-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>
    <div class="header">
      <div class="heading">
        <h1>Manage SNMP Configuration</h1>
      </div>
      <div class="action">
        <ActionDropdownButton
          label="Upload"
          icon="upload"
          :actions="[
            { label: 'Upload XML', action: 'uploadXml' },
            { label: 'Upload JSON', action: 'uploadJson' }
          ]"
          @action-click="onActionClick"
        />
        <ActionDropdownButton
          label="Download"
          icon="download"
          :actions="[
            { label: 'Download XML', action: 'downloadXml' },
            { label: 'Download JSON', action: 'downloadJson' }
          ]"
          @action-click="onActionClick"
        />
        <FeatherButton
          primary
          @click="onCreateDefinition"
        >
          Create New Definition
        </FeatherButton>
      </div>
    </div>
    <div class="tabs">
      <SnmpConfigTabContainer />
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

<script lang="ts" setup>
import { FeatherButton } from '@featherds/button'
import ConfirmationDialog from '@/components/Common/ConfirmationDialog.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import SnmpConfigTabContainer from '@/components/SnmpConfiguration/SnmpConfigTabContainer.vue'
import ActionDropdownButton from '@/components/Common/ActionDropdownButton.vue'
import useDownload from '@/composables/useDownload'
import useSnackbar from '@/composables/useSnackbar'
import useSpinner from '@/composables/useSpinner'
import { downloadSnmpConfig, uploadSnmpConfig } from '@/services/snmpConfigService'
import { useMenuStore } from '@/stores/menuStore'
import { useScvStore } from '@/stores/scvStore'
import { useSnmpConfigStore, ActiveTabs, ViewConfigurationsTabs, SnmpConfigEditMode } from '@/stores/snmpConfigStore'
import { BreadCrumb } from '@/types'

const { downloadFile } = useDownload()
const snackbar = useSnackbar()
const { startSpinner, stopSpinner } = useSpinner()
const store = useSnmpConfigStore()
const menuStore = useMenuStore()
const scvStore = useScvStore()
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)

const confirmationDialogVisible = ref(false)
const uploadType = ref<'xml' | 'json' | null>(null)
const uploadFile = ref<File | null>(null)

const breadcrumbs = computed<BreadCrumb[]>(() => {
  return [
    { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
    { label: 'Manage SNMP Configuration', to: '#', position: 'last' }
  ]
})

const onCreateDefinition = () => {
  store.setDefinitionCreateEditMode(SnmpConfigEditMode.Create)
  store.resetCurrentDefinition()
  store.setActiveTab(ActiveTabs.ViewConfigurations)
  store.setActiveViewConfigurationsTab(ViewConfigurationsTabs.Definitions)
}

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
    input.onchange = () => {
      const selectedFile = input.files ? input.files[0] : null
      resolve(selectedFile)
    }
    input.click()
  })

  if (!file) {
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

      await store.populateSnmpConfig()
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

const onActionClick = async (action: string) => {
  if (action === 'downloadXml') {
    await onDownload(true)
  } else if (action === 'downloadJson') {
    await onDownload(false)
  } else if (action === 'uploadXml') {
    await initiateUpload(true)
  } else if (action === 'uploadJson') {
    await initiateUpload(false)
  }
}

onMounted(async () => {
  store.resetState()
  store.fetchMonitoringLocations()
  store.populateSnmpConfig()
  scvStore.getAliases()
  scvStore.populate()
})
</script>

<style lang="scss" scoped>
.snmp-config {
  padding: 20px;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .feather-menu.feather-dropdown-menu-container.actions-dropdown-button {
      margin-right: 1em;
    }
  }
}
</style>
