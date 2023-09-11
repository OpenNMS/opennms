import { defineStore } from 'pinia'
import API from '@/services'
import { Category, MonitoringLocation, NodeColumnSelectionItem, SetOperator } from '@/types'

export const defaultColumns: NodeColumnSelectionItem[] = [
  { id: 'id', label: 'ID', selected: false, order: 0 },
  { id: 'label', label: 'Node Label', selected: true, order: 1 },
  { id: 'location', label: 'Location', selected: true, order: 2 },
  { id: 'foreignSource', label: 'Foreign Source', selected: true, order: 3 },
  { id: 'foreignId', label: 'Foreign ID', selected: true, order: 4 },
  { id: 'sysContact', label: 'Sys Contact', selected: true, order: 5 },
  { id: 'sysLocation', label: 'Sys Location', selected: true, order: 6 },
  { id: 'sysDescription', label: 'Sys Description', selected: true, order: 7 },
  { id: 'flows', label: 'Flows', selected: true, order: 8 }
]

export const useNodeStructureStore = defineStore('nodeStructureStore', () => {
  const categories = ref<Category[]>([])
  const categoryCount = ref(0)
  const categoryMode = ref(SetOperator.Union)
  const columns = ref<NodeColumnSelectionItem[]>(defaultColumns)
  const monitoringLocations = ref<MonitoringLocation[]>([])
  const selectedCategories = ref<Category[]>([])
  const selectedFlows = ref<string[]>([])
  const selectedMonitoringLocations = ref<MonitoringLocation[]>([])

  const getCategories = async () => {
    const resp = await API.getCategories()

    if (resp) {
      categoryCount.value = resp.totalCount
      categories.value = resp.category
    }
  }

  const getMonitoringLocations = async () => {
    const resp = await API.getMonitoringLocations()

    if (resp) {
      monitoringLocations.value = resp.location
    }
  }

  const resetColumnSelectionToDefault = async () => {
    columns.value = defaultColumns
  }

  const setSelectedCategories = async (cats: Category[]) => {
    selectedCategories.value = [...cats]
  }

  const setCategoryMode = async (mode: SetOperator) => {
    categoryMode.value = mode
  }

  const setSelectedFlows = async (flows: string[]) => {
    selectedFlows.value = [...flows]
  }

  const setSelectedMonitoringLocations = async (locations: MonitoringLocation[]) => {
    selectedMonitoringLocations.value = [...locations]
  }

  const setNodeColumnSelection = async (cols: NodeColumnSelectionItem[]) => {
    columns.value = [...cols]
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

  return {
    categories,
    categoryCount,
    categoryMode,
    columns,
    monitoringLocations,
    selectedCategories,
    selectedFlows,
    selectedMonitoringLocations,
    getCategories,
    getMonitoringLocations,
    resetColumnSelectionToDefault,
    setSelectedCategories,
    setCategoryMode,
    setSelectedFlows,
    setSelectedMonitoringLocations,
    setNodeColumnSelection,
    updateNodeColumnSelection
  }
})
