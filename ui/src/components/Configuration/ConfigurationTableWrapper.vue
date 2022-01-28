<template>
  <div class="white-bg">
    <div class="flex title-padding">
      <h3 class="title">Requisition Definitions {{ requisitionDCount }}</h3>
      <div class="flex button-wrapper" v-if="provisionDList?.length > 0">
        <FeatherButton class="button" text @click="addNew">
          <FeatherIcon class="buttonIcon" :icon="add" />New Definition
        </FeatherButton>
      </div>
    </div>
    <ConfigurationTable
      v-if="provisionDList?.length > 0"
      :itemList="provisionDList"
      :editClicked="editClicked"
      :deleteClicked="deleteClicked"
      :setNewPage="setNewPage"
    />
    <ConfigurationEmptyTable v-if="provisionDList?.length === 0" :newDefinition="addNew" />
    <ConfigurationDrawer
      :edit="editing"
      :configurationDrawerActive="sidePanelState.isActive"
      :activeUpdate="advanceActiveUpdate"
      :closePanel="closeConfigurationDrawer"
      :item="selectedProvisionDItem"
      :advancedActive="advancedActive.active"
      :addAdvancedOption="addAdvancedOption"
      :deleteAdvancedOption="deleteAdvancedOption"
      :saveCurrentState="saveCurrentState"
      :helpState="helpState"
    />
    <ConfigurationDoubleCheckModal
      :optionSelected="doubleCheck"
      :doubleCheckSelected="doubleCheckSelected"
    />
  </div>
</template>

<script lang="ts" setup>

import { computed, reactive } from 'vue'
import { useStore } from 'vuex'

import { FeatherIcon } from '@featherds/icon'
import { FeatherButton } from '@featherds/button'

import Add from '@featherds/icon/action/Add'

import { putProvisionDService } from '@/services/configurationService'

import { useConfigurationToast, useProvisionD } from './hooks'
import { ConfigurationService } from './ConfigurationService'

import ConfigurationTable from './ConfigurationTable.vue'
import ConfigurationEmptyTable from './ConfigurationEmptyTable.vue'
import ConfigurationDrawer from './ConfigurationDrawer.vue'
import ConfigurationDoubleCheckModal from './ConfigurationDoubleCheckModal.vue'

const store = useStore()

/**
 * Local State
 */
const add = computed(() => Add)
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
  updateActiveIndex,
  setEditingStateTo,
  setItemToEdit,
  activeIndex,
  deleteAdvancedOption,
  addAdvancedOption,
  selectedProvisionDItem,
  provisionDList,
  editing
} = useProvisionD()
const { updateToast } = useConfigurationToast()


/**
 * User has decided to edit a table entry.
 */
const editClicked = (index: number) => {
  updateActiveIndex(index)
  sidePanelState.isActive = true
  setEditingStateTo(true)
  const actual = (currentPage.page - 1) * 10 + index
  setItemToEdit(index)
}

/**
 * User has decided to delete a table entry.
 */
const deleteClicked = (index: string) => {
  doubleCheck.active = true
  doubleCheck.index = parseInt(index)
  doubleCheck.title = provisionDList?.value[doubleCheck?.index]?.['import-name']
}

/**
 * Disable the Drawer
 */
const closeConfigurationDrawer = () => {
  sidePanelState.isActive = false
  advancedActive.active = false
  helpState.open = false
  selectedProvisionDItem.errors = { hasErrors: false, host: '', name: '' }
}

/**
 * User has decided to save and upload the current state.
 */
const saveCurrentState = () => {
  // Clear our errors.
  selectedProvisionDItem.errors = { name: '', hasErrors: false, host: '' }

  // Validate the local state.
  const validatedItem = ConfigurationService.validateLocalItem(selectedProvisionDItem?.config)

  // If we're valid.
  if (!validatedItem.hasErrors) {
    //Convert Local Values to Server Ready Values
    const readyForServ = ConfigurationService.convertLocalToServer(selectedProvisionDItem?.config)

    //Update Local State
    provisionDList.value[activeIndex.index] = readyForServ

    //Get Existing State
    const updatedProvisionDData = store?.state?.configuration?.provisionDService

    //Set New State
    updatedProvisionDData['requisition-def'] = provisionDList.value

    //Actually Update the Server
    putProvisionDService(updatedProvisionDData)

    //Close The Drawer
    closeConfigurationDrawer()

    //Build Toast & Send.
    let mods = ['Addition', 'was']
    if (editing.value) {
      mods = ['Edits', 'were']
    }

    updateToast({
      basic: 'Success!',
      detail: `${mods[0]} to requisition definition ${mods[1]} successful.`,
      hasErrors: false
    })

  } else {
    // Inform User of Errors.
    selectedProvisionDItem.errors = validatedItem
  }
}

/**
 * Create a Blank Requisition Definition
 */
const addNew = () => {
  selectedProvisionDItem.config = {
    name: '',
    type: { name: '', id: 0 },
    subType: { name: '', id: 0, value: '' },
    host: '',
    occurance: { name: 'Weekly', id: 0 },
    time: '00:00',
    rescanBehavior: 1,
    advancedOptions: []
  }
  sidePanelState.isActive = true
  activeIndex.index = provisionDList.value.length
  setEditingStateTo(false)
}

/**
 * The user has made their deletion chicken switch selection.
 * @param selection Did the user choose to delete or not?
 */
const doubleCheckSelected = (selection: boolean) => {
  // The user opted to delete the entry
  if (selection) {
    // Update the local state to remove the value.
    provisionDList.value.splice(doubleCheck.index, 1)

    // Get a copy of the existing state.
    const updatedProvisionDData = store?.state?.configuration?.provisionDService

    // Remove the entry from the existing state.
    updatedProvisionDData['requisition-def'] = provisionDList.value

    // Actually Delete the Item from the server
    putProvisionDService(updatedProvisionDData)

    // Send the Toast Message
    updateToast({
      basic: 'Success!',
      detail: 'Deletion of requisition definition was successful.',
      hasErrors: false
    })
  }
  doubleCheck.active = false
  doubleCheck.index = -1
}

/**
 * Should we open the advanced panel when opened?
 * Don't do it if the user has previously explicitly hidden
 * the drawer.
 */
const advanceActiveUpdate = (newVal: boolean) => {
  advancedActive.active = newVal
  const disabled = localStorage.getItem('disable-help') === 'true'
  if (newVal && !disabled) {
    helpState.open = true
  } else {
    helpState.open = false
  }
}

/**
 * Called when the user updates the page.
 * @param newPage New Page Number
 */
const setNewPage = (newPage: number) => {
  currentPage.page = newPage
}

</script>

<style lang="scss" scoped>
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/mixins/elevation";
.title {
  @include headline3();
}
.title-padding {
  padding: 20px;
}
.margin-bottom {
  margin-bottom: 20px;
}
.white-bg {
  background-color: white;
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

<style lang="scss">
@import "@featherds/styles/mixins/typography";
.button-wrapper {
  .btn-content {
    display: flex;
    align-items: center;
  }
  .btn {
    margin-top: 0;
    margin-bottom: 0;
  }
}
</style>