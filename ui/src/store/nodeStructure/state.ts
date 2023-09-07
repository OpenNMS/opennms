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

export interface State {
  categories: Category[]
  categoryCount: number
  selectedCategories: Category[]
  categoryMode: SetOperator
  selectedFlows: string[]
  monitoringLocations: MonitoringLocation[]
  selectedMonitoringLocations: MonitoringLocation[],
  columns: NodeColumnSelectionItem[]
}

const state: State = {
  categories: [],
  categoryCount: 0,
  selectedCategories: [],
  categoryMode: SetOperator.Union,
  selectedFlows: [],
  monitoringLocations: [],
  selectedMonitoringLocations: [],
  columns: defaultColumns
}

export default state
