import { Category, MonitoringLocation, SetOperator } from '@/types'

export interface State {
  categories: Category[]
  categoryCount: number
  selectedCategories: Category[]
  categoryMode: SetOperator
  selectedFlows: string[]
  monitoringLocations: MonitoringLocation[]
  selectedMonitoringLocations: MonitoringLocation[]
}

const state: State = {
  categories: [],
  categoryCount: 0,
  selectedCategories: [],
  categoryMode: SetOperator.Union,
  selectedFlows: [],
  monitoringLocations: [],
  selectedMonitoringLocations: []
}

export default state
