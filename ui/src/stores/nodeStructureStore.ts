import { defineStore } from 'pinia'
import API from '@/services'
import {
  Category,
  MonitoringLocation,
  NodeColumnSelectionItem,
  NodeQueryFilter,
  NodePreferences,
  SetOperator,
  NodeQuerySnmpParams
} from '@/types'
import { useNodeQuery } from '@/components/Nodes/hooks/useNodeQuery'

const { getDefaultNodeQueryFilter, getDefaultNodeQuerySnmpParams } = useNodeQuery()

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

  const isSnmpParamSelected = (snmpParams?: NodeQuerySnmpParams) => {
    if (!snmpParams) {
      return false
    }

    const keys = Object.getOwnPropertyNames(snmpParams)

    return keys.some(k => !!(snmpParams as any)[k]?.length)
  }

  const isAnyFilterSelected = () => {
    return (
      queryFilter.value.searchTerm?.length > 0 ||
      queryFilter.value.selectedCategories.length > 0 ||
      queryFilter.value.selectedFlows.length > 0 ||
      queryFilter.value.selectedMonitoringLocations.length > 0 ||
      !!queryFilter.value.ipAddress?.length ||
      isSnmpParamSelected(queryFilter.value.snmpParams)
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
   * Set filter with IP address, clearing out any SNMP params (currently these are mutually exclusive extended searches)
  */
  const setFilterWithIpAddress = async (ipAddress: string) => {
    queryFilter.value = {
      ...queryFilter.value,
      ipAddress,
      snmpParams: getDefaultNodeQuerySnmpParams()
    }
  }

  /**
   * Set filter with SNMP parameters, clearing out any IP addresses (currently these are mutually exclusive extended searches)
  */
  const setFilterWithSnmpParams = async (key: string, value: string) => {
    // key should be an actual property of NodeQuerySnmpParams
    const snmpParams = {
      ...getDefaultNodeQuerySnmpParams(),
      [key]: value
    }

    queryFilter.value = {
      ...queryFilter.value,
      ipAddress: '',
      snmpParams
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

      if (prefs.nodeFilter.snmpParams) {
        filter.snmpParams = { ...prefs.nodeFilter.snmpParams }
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
    setFromNodePreferences,
    setNodeColumnSelection,
    setSearchTerm,
    setSelectedCategories,
    setSelectedFlows,
    setSelectedMonitoringLocations,
    updateNodeColumnSelection
  }
})
