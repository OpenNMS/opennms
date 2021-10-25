import { VuexContext } from '@/types'
import API from '@/services'

export const GET_TYPES_DROPDOWN = 'configuration/getDropdownTypes'
export const GET_SCHEDULE_PERIOD_DROPDOWN = 'configuration/getSchedulePeriod'
export const GET_ADVANCED_DROPDOWN = 'configuration/getAdvancedDropdown'

const getDropdownTypes = async (context: VuexContext) => {
  const resp = await API.getDropdownTypes
  if (resp) {
    context.commit('SAVE_TYPES', resp)
  }
}

const getSchedulePeriod = async (context: VuexContext) => {
  const resp = await API.getSchedulePeriod
  if (resp) {
    context.commit('SAVE_SCHEDULE_PERIOD', resp)
  }
}

const getAdvancedDropdown = async (context: VuexContext) => {
  const resp = await API.getAdvancedDropdown
  if (resp) {
    context.commit('SAVE_ADVANCE_DROPDOWN', resp)
  }
}

const getProvisionDService = async (context: VuexContext) => {
  const resp = await API.getProvisionDService
  if (resp) {
    context.commit('SAVE_PROVISION_SERVICE', resp)
  }
}

export default {
  getDropdownTypes,
  getSchedulePeriod,
  getAdvancedDropdown,
  getProvisionDService
}
