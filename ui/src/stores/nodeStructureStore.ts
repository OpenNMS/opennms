///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements. See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version. You may not use this file except in
/// compliance with the License. You may obtain a copy of the
/// License at:
///
/// https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied. See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

import { useNodeQuery } from '@/components/Nodes/hooks/useNodeQuery'
import { defaultColumns } from '@/components/Nodes/utils'
import { hasNonEmptyProperty } from '@/lib/utils'
import API from '@/services'
import {
  Category,
  DrawerState,
  MonitoringLocation,
  NodeColumnSelectionItem,
  NodePreferences,
  NodeQueryFilter,
  SetOperator
} from '@/types'
import { IAutocompleteItemType } from '@featherds/autocomplete'
import { defineStore } from 'pinia'

const {
  getDefaultNodeQueryFilter,
  getDefaultNodeQueryExtendedSearchParams,
  getDefaultNodeQueryForeignSourceParams,
  getDefaultNodeQuerySnmpParams,
  getDefaultNodeQuerySysParams
} = useNodeQuery()

// const defaultDrawerState: DrawerState = {
//   visible: false,
//   isAdvanceFilterModal: false
// }

const getDefaultDrawerState = (): DrawerState => {
  return {
    visible: false,
    isAdvanceFilterModal: false
  }
}

export const useNodeStructureStore = defineStore('nodeStructureStore', () => {
  const categories = ref<Category[]>([])
  const categoryCount = computed(() => categories.value.length)
  const monitoringLocations = ref<MonitoringLocation[]>([])
  const columns = ref<NodeColumnSelectionItem[]>(defaultColumns)
  const queryFilter = ref<NodeQueryFilter>(getDefaultNodeQueryFilter())
  const drawerState = ref<DrawerState>(getDefaultDrawerState())
  const columnsDrawerState = ref<DrawerState>(getDefaultDrawerState())
  const selectedCategories = ref<IAutocompleteItemType[]>([])
  const selectedFlows = ref<IAutocompleteItemType[]>([])
  const selectedLocations = ref<MonitoringLocation[]>([])

  const getCategories = async () => {
    const resp = await API.getCategories()

    if (resp) {
      categories.value = resp.category
    }
  }

  const getMonitoringLocations = async () => {
    const resp = await API.getMonitoringLocations()

    if (resp) {
      monitoringLocations.value = resp.location
    }
  }

  const isAnyFilterSelected = () => {
    return (
      queryFilter.value.searchTerm?.length > 0 ||
      queryFilter.value.selectedCategories.length > 0 ||
      queryFilter.value.selectedFlows.length > 0 ||
      queryFilter.value.selectedMonitoringLocations.length > 0 ||
      !!queryFilter.value.extendedSearch?.ipAddress?.length ||
      hasNonEmptyProperty(queryFilter.value.extendedSearch.foreignSourceParams) ||
      hasNonEmptyProperty(queryFilter.value.extendedSearch.snmpParams) ||
      hasNonEmptyProperty(queryFilter.value.extendedSearch.sysParams)
    )
  }

  const resetColumnSelectionToDefault = async () => {
    columns.value = defaultColumns
  }

  const setSearchTerm = async (term: string) => {
    queryFilter.value = {
      ...queryFilter.value,
      searchTerm: term
    }
  }

  // const setSelectedCategories = async (cats: Category[]) => {
  // queryFilter.value = {
  // ...queryFilter.value,
  // selectedCategories: [...cats]
  // }
  // }

  const setCategoryMode = async (mode: SetOperator) => {
    queryFilter.value = {
      ...queryFilter.value,
      categoryMode: mode
    }
  }

  // const setSelectedFlows = async (flows: string[]) => {
  // queryFilter.value = {
  // ...queryFilter.value,
  // selectedFlows: [...flows]
  // }
  // }

  const setSelectedMonitoringLocations = async (locations: MonitoringLocation[]) => {
    queryFilter.value = {
      ...queryFilter.value,
      selectedMonitoringLocations: [...locations]
    }
  }

  const setNodeColumnSelection = async (cols: NodeColumnSelectionItem[]) => {
    columns.value = [...cols]
  }

  /**
  * Set filter with IP address, clearing out any other extended search params (currently these are mutually exclusive extended searches).
  */
  const setFilterWithIpAddress = async (ipAddress: string) => {
    queryFilter.value = {
      ...queryFilter.value,
      extendedSearch: {
        ...getDefaultNodeQueryExtendedSearchParams(),
        ipAddress
      }
    }
  }

  /**
  * Set filter with SNMP parameters, clearing out any other extended search params.
  */
  const setFilterWithSnmpParams = async (key: string, value: string) => {
    // key should be an actual property of NodeQuerySnmpParams
    const snmpParams = {
      ...getDefaultNodeQuerySnmpParams(),
      [key]: value
    }

    queryFilter.value = {
      ...queryFilter.value,
      extendedSearch: {
        ...getDefaultNodeQueryExtendedSearchParams(),
        snmpParams
      }
    }
  }

  /**
  * Set filter with sys parameters, clearing out any other extended search params.
  */
  const setFilterWithSysParams = async (key: string, value: string) => {
    // key should be an actual on of NodeQuerySysParams
    const sysParams = {
      ...getDefaultNodeQuerySysParams(),
      [key]: value
    }

    queryFilter.value = {
      ...queryFilter.value,
      extendedSearch: {
        ...getDefaultNodeQueryExtendedSearchParams(),
        sysParams
      }
    }
  }

  /**
  * Set filter with foreign source parameters, clearing out any other extended search params.
  */
  const setFilterWithForeignSourceParams = async (key: string, value: string) => {
    // key should be an actual property of NodeQueryForeignSourceParams
    const foreignSourceParams = {
      ...getDefaultNodeQueryForeignSourceParams(),
      [key]: value
    }

    queryFilter.value = {
      ...queryFilter.value,
      extendedSearch: {
        ...getDefaultNodeQueryExtendedSearchParams(),
        foreignSourceParams
      }
    }
  }

  const updateNodeColumnSelection = async (column: NodeColumnSelectionItem) => {
    const newColumns = [...columns.value].map((c) => {
      if (c.id === column.id) {
        return {
          ...c,
          selected: column.selected
        }
      }
      return c
    })

    columns.value = [...newColumns]
  }

  const clearAllFilters = async (mode?: SetOperator) => {
    const filter = getDefaultNodeQueryFilter()
    queryFilter.value = !mode
      ? filter
      : {
        ...filter,
        categoryMode: mode
      }
  }

  const getNodePreferences = async () => {
    const nodeColumns = columns.value

    const nodePrefs = {
      nodeColumns,
      nodeFilter: { ...queryFilter.value }
    } as NodePreferences
    return nodePrefs
  }

  const setFromNodePreferences = async (prefs: NodePreferences) => {
    if (prefs.nodeColumns?.length) {
      columns.value = [...prefs.nodeColumns]
    }

    const filter = getDefaultNodeQueryFilter()

    if (prefs.nodeFilter) {
      filter.searchTerm = prefs.nodeFilter.searchTerm
      filter.categoryMode = prefs.nodeFilter.categoryMode

      if (prefs.nodeFilter.selectedCategories?.length) {
        filter.selectedCategories = [...prefs.nodeFilter.selectedCategories]
      }

      if (prefs.nodeFilter.selectedFlows?.length) {
        filter.selectedFlows = [...prefs.nodeFilter.selectedFlows]
      }

      if (prefs.nodeFilter.selectedMonitoringLocations?.length) {
        filter.selectedMonitoringLocations = [...prefs.nodeFilter.selectedMonitoringLocations]
      }

      if (prefs.nodeFilter.extendedSearch) {
        filter.extendedSearch = { ...prefs.nodeFilter.extendedSearch }
      }
    }

    queryFilter.value = filter
  }

  const openInstancesDrawerModal = () => {
    drawerState.value.visible = true
  }

  const closeInstancesDrawerModal = () => {
    drawerState.value.visible = false
  }

  const openColumnsDrawerModal = () => {
    columnsDrawerState.value.visible = true
  }

  const closeColumnsDrawerModal = () => {
    columnsDrawerState.value.visible = false
  }
  const removeCategory = (item: IAutocompleteItemType) => {
    selectedCategories.value = selectedCategories.value.filter((i) => i._value !== item._value)
    queryFilter.value.selectedCategories = queryFilter.value.selectedCategories.filter((c) => c.id !== item._value)
  }

  const removeLocation = (item: IAutocompleteItemType) => {
    const locationName = item.name
    queryFilter.value.selectedMonitoringLocations = queryFilter.value.selectedMonitoringLocations.filter(
      (loc) => loc.name !== locationName
    )
  }

  const setSelectedCategories = (items: IAutocompleteItemType[]) => {
    selectedCategories.value = items
    // Also update the query filter
    queryFilter.value.selectedCategories = items.map((item) => ({
      id: item._value as number,
      name: item._text as string,
      authorizedGroups: [] as string[]
    }))
  }

  const setSelectedFlows = (items: IAutocompleteItemType[]) => {
    selectedFlows.value = items
    queryFilter.value.selectedFlows = items.map((item) => item._text as string)
  }

  const removeFlow = (item: IAutocompleteItemType) => {
    selectedFlows.value = selectedFlows.value.filter((i) => i._text !== item._text)
    queryFilter.value.selectedFlows = queryFilter.value.selectedFlows.filter((f) => f !== item._text)
  }

  const setSelectedLocations = async (locations: MonitoringLocation[]) => {
    queryFilter.value = {
      ...queryFilter.value,
      selectedMonitoringLocations: [...locations]
    }
  }

  return {
    categories,
    categoryCount,
    columns,
    monitoringLocations,
    queryFilter,
    drawerState,
    columnsDrawerState,
    clearAllFilters,
    getCategories,
    getMonitoringLocations,
    getNodePreferences,
    isAnyFilterSelected,
    resetColumnSelectionToDefault,
    setCategoryMode,
    setFilterWithIpAddress,
    setFilterWithSnmpParams,
    setFilterWithForeignSourceParams,
    setFilterWithSysParams,
    setFromNodePreferences,
    setNodeColumnSelection,
    setSearchTerm,
    // setSelectedCategories,
    // setSelectedFlows,
    setSelectedMonitoringLocations,
    updateNodeColumnSelection,
    openInstancesDrawerModal,
    closeInstancesDrawerModal,
    selectedCategories,
    selectedFlows,
    selectedLocations,
    removeCategory,
    removeFlow,
    removeLocation,
    setSelectedCategories,
    setSelectedFlows,
    setSelectedLocations,
    openColumnsDrawerModal,
    closeColumnsDrawerModal

  }
})