import axios from 'axios'
import { axiosAuth } from '../components/Common/Demo/apiInterceptor'

let typesUrl = 'src/components/Common/Demo/MockupData/types.json'
let periodUrl = 'src/components/Common/Demo/MockupData/schedulePeriod.json'
let advDropdownUrl = 'src/components/Common/Demo/MockupData/advancedDropdown.json'
let getProvisionD = '/opennms/rest/cm/provisiond/default'

const getDropdownTypes = axios.get(typesUrl).then((response) => {
  try {
    if (response.status === 200) {
      return response.data
    }
  } catch {
    console.error('issue with getDropdownTypes api')
  }
})

const getSchedulePeriod = axios.get(periodUrl).then((response) => {
  try {
    if (response.status === 200) {
      return response.data
    }
  } catch {
    console.error('issue with getSchedulePeriod api')
  }
})

const getAdvancedDropdown = axios.get(advDropdownUrl).then((response) => {
  try {
    if (response.status === 200) {
      return response.data
    }
  } catch {
    console.error('issue with getAdvancedDropdown api')
  }
})

const getProvisionDService = axiosAuth.get(getProvisionD).then((response) => {
  try {
    if (response.status === 200) {
      return response.data
    }
  } catch {
    console.error('issue with getProvisionDService api')
  }
})

export { getDropdownTypes, getSchedulePeriod, getAdvancedDropdown, getProvisionDService }
