import { Category, MonitoringLocation, SetOperator } from '@/types'
import { State } from './state'

const SAVE_CATEGORY_COUNT = (state: State, count: number) => {
  state.categoryCount = count
}

const SAVE_CATEGORIES_TO_STATE = (state: State, categories: Category[]) => {
  state.categories = [...categories]
}

const SAVE_SELECTED_CATEGORIES_TO_STATE = (state: State, categories: Category[]) => {
  state.selectedCategories = [...categories]
}

const SAVE_CATEGORY_MODE = (state: State, mode: SetOperator) => {
  state.categoryMode = mode
}

const SAVE_SELECTED_FLOWS_TO_STATE = (state: State, flows: string[]) => {
  state.selectedFlows = [...flows]
}

const SAVE_LOCATIONS_TO_STATE = (state: State, locations: MonitoringLocation[]) => {
  state.monitoringLocations = [...locations]
}

const SAVE_SELECTED_MONITORING_LOCATIONS_TO_STATE = (state: State, locations: MonitoringLocation[]) => {
  state.selectedMonitoringLocations = [...locations]
}

export default {
  SAVE_CATEGORY_COUNT,
  SAVE_CATEGORIES_TO_STATE,
  SAVE_SELECTED_CATEGORIES_TO_STATE,
  SAVE_CATEGORY_MODE,
  SAVE_SELECTED_FLOWS_TO_STATE,
  SAVE_LOCATIONS_TO_STATE,
  SAVE_SELECTED_MONITORING_LOCATIONS_TO_STATE
}
