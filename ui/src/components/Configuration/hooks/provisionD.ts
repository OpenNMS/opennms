import { computed, reactive, ref } from 'vue'
import { useStore } from 'vuex'
import { ConfigurationService } from '../ConfigurationService'
export type LocalErrors = {
  hasErrors: boolean
  host: string
  name: string
}
export type LocalConfigurationWrapper = {
  config: LocalConfiguration
  errors: LocalErrors
}
export const useProvisionD = () => {
  const store = useStore()
  const activeIndex = reactive({ index: -1 })

  const editing = ref(false)

  const setEditingStateTo = (editingValue: boolean) => {
    editing.value = editingValue
  }

  const provisionDList = computed(() => {
    let defaultList: Array<ProvisionDServerConfiguration> =
      store?.state?.configuration?.provisionDService?.['requisition-def']
    return (
      defaultList?.map((item, index): ProvisionDServerConfiguration => {
        item.originalIndex = index
        return item
      }) || []
    )
  })
  const selectedProvisionD = reactive<LocalConfigurationWrapper>({
    config: {
      name: '',
      type: { name: '', id: 0 },
      subType: { value: '', id: 0, name: '' },
      host: '',
      occurance: { name: '', id: 0 },
      time: '00:00',
      rescanBehavior: 1,
      advancedOptions: [{ key: { name: '', _text: '' }, value: '' }]
    },
    errors: { hasErrors: false, host: '', name: '' }
  })

  const setItemToEdit = (index: number) => {
    selectedProvisionD.config = ConfigurationService.convertServerConfigurationToLocal(provisionDList.value[index])
  }

  const updateActiveIndex = (index: number) => {
    activeIndex.index = index
  }

  const addAdvancedOption = () => {
    if (selectedProvisionD && !selectedProvisionD?.config?.advancedOptions) {
      selectedProvisionD.config.advancedOptions = []
    }
    if (selectedProvisionD?.config?.advancedOptions) {
      selectedProvisionD.config.advancedOptions.push({ key: { name: '', _text: '' }, value: '' })
    }
  }

  const deleteAdvancedOption = (index: number) => {
    if (selectedProvisionD?.config?.advancedOptions[index]) {
      selectedProvisionD.config.advancedOptions.splice(index, 1)
    }
  }

  return {
    setEditingStateTo,
    provisionDList,
    selectedProvisionDItem: selectedProvisionD,
    setItemToEdit,
    activeIndex,
    editing,
    updateActiveIndex,
    addAdvancedOption,
    deleteAdvancedOption
  }
}
