import axios from 'axios'
import { VuexContext } from '@/types'

let typesUrl = 'src/components/Common/Demo/MockupData/types.json'
let periodUrl = 'src/components/Common/Demo/MockupData/schedulePeriod.json'
let advDropdownUrl = 'src/components/Common/Demo/MockupData/advancedDropdown.json'

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

export default {
  getDropdownTypes,
  getSchedulePeriod,
  getAdvancedDropdown
}
