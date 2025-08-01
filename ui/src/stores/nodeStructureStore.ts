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

import { defineStore } from 'pinia'
import API from '@/services'
import { hasNonEmptyProperty } from '@/lib/utils'
import {
  Category,
  MonitoringLocation,
  NodeColumnSelectionItem,
  NodeQueryFilter,
  NodePreferences,
  SetOperator
} from '@/types'
import { useNodeQuery } from '@/components/Nodes/hooks/useNodeQuery'

const {
  getDefaultNodeQueryFilter,
  getDefaultNodeQueryExtendedSearchParams,
  getDefaultNodeQueryForeignSourceParams,
  getDefaultNodeQuerySnmpParams,
  getDefaultNodeQuerySysParams
} = useNodeQuery()

export const defaultColumns: NodeColumnSelectionItem[] = [
  { id: 'id', label: 'ID', selected: false, order: 0 },
  { id: 'label', label: 'Node Label', selected: true, order: 1 },
  { id: 'ipaddress', label: 'IP Address', selected: true, order: 2 },
  { id: 'location', label: 'Location', selected: true, order: 3 },
  { id: 'foreignSource', label: 'Foreign Source', selected: true, order: 4 },
  { id: 'foreignId', label: 'Foreign ID', selected: true, order: 5 },
  { id: 'sysContact', label: 'Sys Contact', selected: true, order: 6 },
  { id: 'sysLocation', label: 'Sys Location', selected: true, order: 7 },
  { id: 'sysDescription', label: 'Sys Description', selected: true, order: 8 },
  { id: 'flows', label: 'Flows', selected: true, order: 9 }
]

export const useNodeStructureStore = defineStore('nodeStructureStore', () => {
  const categories = ref<Category[]>([])
  const categoryCount = computed(() => categories.value.length)
  const monitoringLocations = ref<MonitoringLocation[]>([])
  const columns = ref<NodeColumnSelectionItem[]>(defaultColumns)
  const queryFilter = ref<NodeQueryFilter>(getDefaultNodeQueryFilter())

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

  const setSelectedCategories = async (cats: Category[]) => {
    queryFilter.value = {
      ...queryFilter.value,
      selectedCategories: [...cats]
    }
  }

  const setCategoryMode = async (mode: SetOperator) => {
    queryFilter.value = {
      ...queryFilter.value,
      categoryMode: mode
    }
  }

  const setSelectedFlows = async (flows: string[]) => {
    queryFilter.value = {
      ...queryFilter.value,
      selectedFlows: [...flows]
    }
  }

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
    const newColumns = [...columns.value].map(c => {
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
    queryFilter.value = !mode ? filter :
      {
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

  return {
    categories,
    categoryCount,
    columns,
    monitoringLocations,
    queryFilter,
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
    setSelectedCategories,
    setSelectedFlows,
    setSelectedMonitoringLocations,
    updateNodeColumnSelection
  }
})
