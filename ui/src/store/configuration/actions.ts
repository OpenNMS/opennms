import axios from 'axios'
import { VuexContext } from '@/types'
import { axiosAuth } from '../../components/Common/Demo/apiInterceptor'

let typesUrl = 'src/components/Common/Demo/MockupData/types.json'
let periodUrl = 'src/components/Common/Demo/MockupData/schedulePeriod.json'
let advDropdownUrl = 'src/components/Common/Demo/MockupData/advancedDropdown.json'
let getProvisionD = '/opennms/rest/cm/provisiond/default'

const getDropdownTypes = (context: VuexContext) => {
  axios.get(typesUrl).then((response) => {
    context.commit('getDropdownTypes', response.data)
  })
}

const getSchedulePeriod = (context: VuexContext) => {
  axios.get(periodUrl).then((response) => {
    context.commit('getSchedulePeriod', response.data)
  })
}

const getAdvancedDropdown = (context: VuexContext) => {
  axios.get(advDropdownUrl).then((response) => {
    context.commit('getAdvancedDropdown', response.data)
  })
}

const getProvisionDService = (context: VuexContext) => {
  axiosAuth.get(getProvisionD).then((response) => {
    if (response.status === 200) {
      context.commit('getProvisionDService', response.data)
    } else {
      console.error('Please check API response')
    }
  })
}

export default {
  getDropdownTypes,
  getSchedulePeriod,
  getAdvancedDropdown,
  getProvisionDService
}
