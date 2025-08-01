///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

import { useConfigurationStore } from '@/stores/configurationStore'
import { LocalConfiguration, LocalConfigurationWrapper, ProvisionDServerConfiguration } from '../configuration.types'
import { ConfigurationHelper } from '../ConfigurationHelper'

export const useProvisionD = () => {
  const configurationStore = computed(() => useConfigurationStore())

  /**
   * State
   */
  const activeIndex = reactive({ index: -1 })
  const editing = ref(false)
  const loading = ref(false)
  const provisionDList = computed(() => {
    const defaultList: Array<ProvisionDServerConfiguration> =
      configurationStore.value.provisionDService?.['requisition-def']

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
      selectedProvisionDItem.config.advancedOptions.push({ key: { name: '', _text: '' }, value: '', hint: '' })
    }
  }

  const advancedKeyUpdate = (key: { hint: string }, index: number) => {
    selectedProvisionDItem.config.advancedOptions[index].hint = key.hint
    selectedProvisionDItem.config.advancedOptions[index].value = ''
  }

  /**
   * User has changed types, so we need to clear out advanced options, reset the host and urlPath.
   *  - urlPath: used to extract the url query part to update the item's url (see convertItemToURL and getQueryStringFromAdvancedOptions)
   */
  const createNewRequisition = () => {
    selectedProvisionDItem.config.host = ''
    selectedProvisionDItem.config.advancedOptions = []
    selectedProvisionDItem.config.urlPath = ''
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
   * Updates our local crontab values.
   * @param key the key being updated
   * @param value the value for the key being updated
   */
  const updateCronTab = (key: string, value: string) => {
    if (key === 'advancedCrontab' && value) {
      selectedProvisionDItem.config.occuranceAdvanced = ConfigurationHelper.convertLocalToCronTab(
        selectedProvisionDItem.config
      )
    } else if (key === 'advancedCrontab' && !value) {
      const localTime = selectedProvisionDItem.config.time
      if (localTime.includes(',') || localTime.length > 5) {
        selectedProvisionDItem.config.time = '00:00'
      }
      selectedProvisionDItem.config.occuranceAdvanced = ConfigurationHelper.convertLocalToCronTab(
        selectedProvisionDItem.config
      )
    }
  }

  /**
   *
   * @param key Key in the LocalConfiguration we want to update
   * @param value New value associated to the key
   */
  const updateFormValue = (key: string, value: string) => {
    updateCronTab(key, value)
    ;(selectedProvisionDItem.config as Record<string, unknown>)[key] = value
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
    selectedProvisionDItem.errors = ConfigurationHelper.validateLocalItem(
      localConfig,
      provisionDList.value,
      activeIndex.index,
      true
    )
  }

  return {
    activeIndex,
    editing,
    loading,
    provisionDList,
    selectedProvisionDItem,
    advancedKeyUpdate,
    addAdvancedOption,
    createNewRequisition,
    deleteAdvancedOption,
    setEditingStateTo,
    setLoading: (val: boolean) => (loading.value = val),
    setItemToEdit,
    updateActiveIndex,
    updateFormValue,
    updateValidation
  }
}
