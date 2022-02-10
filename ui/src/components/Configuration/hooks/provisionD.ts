import { computed, reactive, ref } from 'vue'
import { useStore } from 'vuex'
import { LocalConfiguration, LocalConfigurationWrapper, ProvisionDServerConfiguration } from '../configuration.types'
import { ConfigurationHelper } from '../ConfigurationHelper'

export const useProvisionD = () => {
  const store = useStore()

  /**
   * State
   */
  const activeIndex = reactive({ index: -1 })
  const editing = ref(false)
  const loading = ref(false)
  const provisionDList = computed(() => {
    const defaultList: Array<ProvisionDServerConfiguration> =
      store?.state?.configuration?.provisionDService?.['requisition-def']
    return (
      defaultList?.map((item, index): ProvisionDServerConfiguration => {
        item.originalIndex = index
        return item
      }) || []
    )
  })

  const selectedProvisionDItem = reactive<LocalConfigurationWrapper>(ConfigurationHelper.createBlankLocal())

  /**
   * User wants another advanced option
   */
  const addAdvancedOption = () => {
    if (selectedProvisionDItem && !selectedProvisionDItem?.config?.advancedOptions) {
      selectedProvisionDItem.config.advancedOptions = []
    }
    if (selectedProvisionDItem?.config?.advancedOptions) {
      selectedProvisionDItem.config.advancedOptions.push({ key: { name: '', _text: '' }, value: '' })
    }
  }

  /**
   * User has changed types, so we need to clear out our advanced options and reset the host.
   */
  const createNewRequisition = () => {
    selectedProvisionDItem.config.host = ''
    selectedProvisionDItem.config.advancedOptions = []
  }

  /**
   * User wants to remove an advanced option
   */
  const deleteAdvancedOption = (index: number) => {
    if (selectedProvisionDItem?.config?.advancedOptions[index]) {
      selectedProvisionDItem.config.advancedOptions.splice(index, 1)
    }
  }

  /**
   * Sets the editing state
   * @param editingValue Are we editing or adding?
   */
  const setEditingStateTo = (editingValue: boolean) => {
    editing.value = editingValue
  }

  /**
   * 
   * @param index Index for the item we clicked in the list of ProvisionD items
   */
  const setItemToEdit = (index: number) => {
    selectedProvisionDItem.config = ConfigurationHelper.convertServerConfigurationToLocal(provisionDList.value[index])
  }

  /**
   * We use the activeIndex to know where to place the object being edited in the final list
   * of requisitions
   * @param index The index of the item we clicked on
   */
  const updateActiveIndex = (index: number) => {
    activeIndex.index = index
  }

  /**
   * 
   * @param key Key in the LocalConfiguration we want to update
   * @param value New value associated to the key
   */
  const updateFormValue = (key: string, value: string) => {
    (selectedProvisionDItem.config as Record<string, unknown>)[key] = value
    updateValidation(selectedProvisionDItem.config)
    if (key === 'type' || key === 'subType') {
      createNewRequisition()
    }
  }

  /**
   * Updates our current error/validation state.
   * @param localConfig Full Local Configuration Item
   */
  const updateValidation = (localConfig: LocalConfiguration) => {
    selectedProvisionDItem.errors = ConfigurationHelper.validateLocalItem(localConfig, true)
  }

  return {
    activeIndex,
    editing,
    loading,
    provisionDList,
    selectedProvisionDItem,

    addAdvancedOption,
    createNewRequisition,
    deleteAdvancedOption,
    setEditingStateTo,
    setLoading: (val: boolean) => loading.value = val,
    setItemToEdit,
    updateActiveIndex,
    updateFormValue,
    updateValidation,
  }
}
