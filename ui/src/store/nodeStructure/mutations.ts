import { Category, MonitoringLocation, NodeColumnSelectionItem, SetOperator } from '@/types'
import { State, defaultColumns } from './state'

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

const RESET_NODE_COLUMN_SELECTION = (state: State) => {
  state.columns = [...defaultColumns]
}

const SET_NODE_COLUMN_SELECTION = (state: State, columns: NodeColumnSelectionItem[]) => {
  state.columns = [...columns]
}

const UPDATE_NODE_COLUMN_SELECTION = (state: State, column: NodeColumnSelectionItem) => {
  const newColumns = [...state.columns].map(c => {
    if (c.id === column.id) {
      return {
        ...c,
        selected: column.selected
      }
    }
    return c
  })

  state.columns = [...newColumns]
}

const SAVE_SELECTED_MONITORING_LOCATIONS_TO_STATE = (state: State, locations: MonitoringLocation[]) => {
  state.selectedMonitoringLocations = [...locations]
}

export default {
  RESET_NODE_COLUMN_SELECTION,
  SAVE_CATEGORY_COUNT,
  SAVE_CATEGORIES_TO_STATE,
  SAVE_SELECTED_CATEGORIES_TO_STATE,
  SAVE_CATEGORY_MODE,
  SAVE_SELECTED_FLOWS_TO_STATE,
  SAVE_LOCATIONS_TO_STATE,
  SAVE_SELECTED_MONITORING_LOCATIONS_TO_STATE,
  SET_NODE_COLUMN_SELECTION,
  UPDATE_NODE_COLUMN_SELECTION
}
