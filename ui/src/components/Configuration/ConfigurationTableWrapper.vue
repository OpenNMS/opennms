<template>
  <div class="white-bg">
    <div class="flex title-padding">
      <h3 class="title">
        External Requisitions
        {{ requisitionDCount }}
      </h3>
      <div
        class="flex button-wrapper"
        v-if="provisionDList?.length > 0"
      >
        <FeatherButton
          class="button"
          text
          @click="addNew"
          >Add External Requisition</FeatherButton
        >
      </div>
    </div>
    <ConfigurationTable
      v-if="provisionDList?.length > 0"
      :itemList="provisionDList"
      :editClicked="editClicked"
      :deleteClicked="deleteClicked"
      :setNewPage="setNewPage"
    />
    <ConfigurationEmptyTable
      v-if="provisionDList?.length === 0"
      :newDefinition="addNew"
    />
    <ConfigurationDrawer
      :loading="loading"
      :updateFormValue="updateFormValue"
      :edit="editing"
      :configurationDrawerActive="sidePanelState.isActive"
      :closePanel="closeConfigurationDrawer"
      :item="selectedProvisionDItem"
      :advancedActive="advancedActive.active"
      :activeUpdate="advanceActiveUpdate"
      :addAdvancedOption="addAdvancedOption"
      :deleteAdvancedOption="deleteAdvancedOption"
      :saveCurrentState="saveCurrentState"
      :advancedKeyUpdate="advancedKeyUpdate"
      :helpState="helpState"
    />
    <ConfigurationDoubleCheckModal
      :optionSelected="doubleCheck"
      :doubleCheckSelected="doubleCheckSelected"
    />
  </div>
</template>

<script
  lang="ts"
  setup
>
import { useConfigurationStore } from '@/stores/configurationStore'

import { FeatherButton } from '@featherds/button'

import { putProvisionDService } from '@/services/configurationService'
import { useProvisionD } from './hooks'
import useSnackbar from '@/composables/useSnackbar'
import { ConfigurationHelper } from './ConfigurationHelper'

import ConfigurationTable from './ConfigurationTable.vue'
import ConfigurationEmptyTable from './ConfigurationEmptyTable.vue'
import ConfigurationDrawer from './ConfigurationDrawer.vue'
import ConfigurationDoubleCheckModal from './ConfigurationDoubleCheckModal.vue'
import { RequisitionData } from './copy/requisitionTypes'

const configurationStore = useConfigurationStore()

/**
 * Local State
 */
const sidePanelState = reactive({ isActive: false })

const currentPage = reactive({ page: 1 })
const helpState = reactive({ open: false })
let advancedActive = reactive({ active: false })
const requisitionDCount = computed(() =>
  provisionDList?.value?.length > 0 ? `(${provisionDList?.value?.length})` : ''
)
const doubleCheck = reactive({ active: false, index: -1, title: '' })

/**
 * Hooks
 */
const {
  activeIndex,
  addAdvancedOption,
  advancedKeyUpdate,
  deleteAdvancedOption,
  selectedProvisionDItem,
  setEditingStateTo,
  setItemToEdit,
  updateActiveIndex,
  provisionDList,
  editing,
  updateFormValue,
  setLoading,
  loading
} = useProvisionD()

const { showSnackBar } = useSnackbar()

/**
 * Create a Blank Requisition Definition
 */
const addNew = () => {
  selectedProvisionDItem.config = ConfigurationHelper.createBlankLocal().config
  sidePanelState.isActive = true
  activeIndex.index = provisionDList.value.length
  setEditingStateTo(false)
  disableMainScroll()
}

/**
 * User has decided to edit a table entry.
 */
const editClicked = (index: number) => {
  updateActiveIndex(index)
  sidePanelState.isActive = true
  setEditingStateTo(true)
  setItemToEdit(index)
  disableMainScroll()
}

/**
 * User has decided to delete a table entry.
 */
const deleteClicked = (index: string) => {
  doubleCheck.active = true
  doubleCheck.index = parseInt(index)
  doubleCheck.title = provisionDList?.value[doubleCheck?.index]?.[RequisitionData.ImportName]
}

/**
 * When the user opens our side panel, we want to hide the main scroll bar so we don't
 * double up on them.
 */
const disableMainScroll = () => {
  const html = document.querySelector('html')
  if (html) {
    html.style.overflowY = 'hidden'
    html.style.height = '100vh'
  }
}

/**
 * When the user closes the side panel, re-enable the main scroll bar.
 */
const enableMainScroll = () => {
  const html = document.querySelector('html')
  if (html) {
    html.style.overflowY = 'auto'
    html.style.height = 'auto'
  }
}

/**
 * Disable the Drawer
 */
const closeConfigurationDrawer = () => {
  sidePanelState.isActive = false
  advancedActive.active = false
  helpState.open = false
  selectedProvisionDItem.errors = ConfigurationHelper.createBlankErrors()
  enableMainScroll()
}

/**
 * User has decided to save and upload the current state.
 */
const saveCurrentState = async () => {
  setLoading(true)
  // Clear our errors.
  selectedProvisionDItem.errors = ConfigurationHelper.createBlankErrors()

  // Validate the local state.
  const validatedItem = ConfigurationHelper.validateLocalItem(selectedProvisionDItem?.config, provisionDList.value,activeIndex.index)

  // If we're valid.
  if (!validatedItem.hasErrors) {
    //Convert Local Values to Server Ready Values
    const readyForServ = ConfigurationHelper.convertLocalToServer(selectedProvisionDItem?.config, true)
    const forSending = [...provisionDList.value]

    //Update with our values
    forSending[activeIndex.index] = readyForServ

    //Get Existing Full State (including thread pools)
    let updatedProvisionDData = configurationStore.provisionDService

    if (!updatedProvisionDData) {
      updatedProvisionDData = {}
    }

    //Set New State with our requisition definitions
    updatedProvisionDData['requisition-def'] = ConfigurationHelper.stripOriginalIndexes(forSending)

    //Snackbar messages can differ depending on our editing state.
    let mods = ['Addition', 'was']
    if (editing.value) {
      mods = ['Edits', 'were']
    }

    try {
      //Actually Update the Server
      await putProvisionDService(updatedProvisionDData)
      await configurationStore.getProvisionDService()

      closeConfigurationDrawer()

      showSnackBar({
        msg: `${mods[0]} to requisition definition ${mods[1]} successful.`,
        center: false
      })
    } catch (err) {
      showSnackBar({
        msg: `${mods[0]} to requisition definition ${mods[1]} not successful. (${err})`,
        center: false,
        error: true
      })
    }
  } else {
    // Inform User of Errors.
    selectedProvisionDItem.errors = validatedItem
  }
  setLoading(false)
}

/**
 * The user has made their deletion chicken switch/double check selection.
 * @param selection Did the user choose to delete or not?
 */
const doubleCheckSelected = async (selection: boolean) => {
  // The user opted to delete the entry
  if (selection) {
    // Update the local state to remove the value.
    const copiedList = [...provisionDList.value]
    copiedList.splice(doubleCheck.index, 1)

    // Get a copy of the existing state.
    const updatedProvisionDData = configurationStore.provisionDService

    // Remove the entry from the existing state.
    updatedProvisionDData['requisition-def'] = ConfigurationHelper.stripOriginalIndexes(copiedList)

    try {
      await putProvisionDService(updatedProvisionDData)

      showSnackBar({
        msg: 'Deletion of requisition definition was successful.',
        center: false
      })
    } catch (err) {
      showSnackBar({
        msg: `Deletion of requisition definition was NOT successful. (${err})`,
        center: false,
        error: true
      })
    }

    await configurationStore.getProvisionDService()
  }

  doubleCheck.active = false
  doubleCheck.index = -1
}

/**
 * Called when the user updates the page.
 * @param newPage New Page Number
 */
const setNewPage = (newPage: number) => {
  currentPage.page = newPage
}

/**
 * Should we open the advanced panel when opened?
 * Don't do it if the user has previously explicitly hidden
 * the drawer.
 */
const advanceActiveUpdate = (newVal: boolean) => {
  advancedActive.active = newVal
}
</script>

<style
  lang="scss"
  scoped
>
@import "@featherds/styles/themes/variables";
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/mixins/elevation";

.title {
  @include headline3();
}
.title-padding {
  margin: 20px;
}
.margin-bottom {
  margin-bottom: 20px;
}
.white-bg {
  background-color: var($background);
  border: 1px solid #ebedf0;
  margin-top: 16px;
  margin-bottom: 24px;
  @include elevation(2);
}
.flex {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.buttonIcon {
  font-size: 24px;
}
</style>

