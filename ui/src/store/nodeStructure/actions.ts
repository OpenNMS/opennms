import API from '@/services'
import { Category, SetOperator, VuexContext } from '@/types'

const getCategories = async (context: VuexContext) => {
  const resp = await API.getCategories()

  if (resp) {
    context.commit('SAVE_CATEGORY_COUNT', resp.totalCount)
    context.commit('SAVE_CATEGORIES_TO_STATE', resp.category)
  }
}

const getMonitoringLocations = async (context: VuexContext) => {
  const resp = await API.getMonitoringLocations()

  if (resp) {
    context.commit('SAVE_LOCATIONS_TO_STATE', resp.location)
  }
}

const setSelectedCategories = async (context: VuexContext, categories: Category[]) => {
  context.commit('SAVE_SELECTED_CATEGORIES_TO_STATE', categories)
}

const setCategoryMode = async (context: VuexContext, mode: SetOperator) => {
  context.commit('SAVE_CATEGORY_MODE', mode)
}

const setSelectedFlows = async (context: VuexContext, flows: string[]) => {
  context.commit('SAVE_SELECTED_FLOWS_TO_STATE', flows)
}

const setSelectedMonitoringLocations = async (context: VuexContext, locations: string[]) => {
  context.commit('SAVE_SELECTED_MONITORING_LOCATIONS_TO_STATE', locations)
}

export default {
  getCategories,
  getMonitoringLocations,
  setSelectedCategories,
  setCategoryMode,
  setSelectedFlows,
  setSelectedMonitoringLocations
}
