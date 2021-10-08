import axios from 'axios'
import { axiosAuth } from './apiInterceptor'

let url = 'src/components/Common/Demo/MockupData/nodeData.json'
let typesUrl = 'src/components/Common/Demo/MockupData/types.json'
let periodUrl = 'src/components/Common/Demo/MockupData/schedulePeriod.json'
let advDropdownUrl = 'src/components/Common/Demo/MockupData/advancedDropdown.json'
let tableMock = 'src/components/Common/Demo/MockupData/tableMock.json'

const nodeData = axios.get(url)
const tableMockData = axios.get(tableMock)
let getProvisionD = '/opennms/rest/cm/provisiond/default'
const apigetProvisionD = axiosAuth.get(getProvisionD)

const getDropdownTypes = axios
  .get(typesUrl)
  .then((response: any) => {
    let dataLen = response.data.length
    if (dataLen > 0) {
      return response.data
    }
  })
  .catch((err: any) => {
    console.error('apiTypes Error ==>', err)
  })

const getSchedulePeriod = axios
  .get(periodUrl)
  .then((response: any) => {
    let dataLen = response.data.length
    if (dataLen > 0) {
      return response.data
    }
  })
  .catch((err: any) => {
    console.error('apiPeriod Error ==>', err)
  })

const getAdvancedDropdown = axios
  .get(advDropdownUrl)
  .then((response: any) => {
    let dataLen = response.data.length
    if (dataLen > 0) {
      return response.data
    }
  })
  .catch((err: any) => {
    console.error('apiAdvDropdown Error ==>', err)
  })

export { nodeData, getDropdownTypes, getSchedulePeriod, getAdvancedDropdown, tableMockData, apigetProvisionD }
