import { Category, MonitoringLocation } from '@/types'

export interface State {
  categories: Category[]
  categoryCount: number
  selectedCategories: Category[]
  selectedFlows: string[]
  monitoringLocations: MonitoringLocation[]
  selectedMonitoringLocations: MonitoringLocation[]
}

const state: State = {
  categories: [],
  categoryCount: 0,
  selectedCategories: [],
  selectedFlows: [],
  monitoringLocations: [],
  selectedMonitoringLocations: []
}

export default state
