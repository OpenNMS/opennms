import axios from 'axios'
import { axiosAuth } from './apiIinterceptor'


let url = 'src/components/Common/Demo/MockupData/nodeData.json'
let typesUrl = 'src/components/Common/Demo/MockupData/types.json'
let periodUrl = 'src/components/Common/Demo/MockupData/schedulePeriod.json'
let advDropdownUrl = 'src/components/Common/Demo/MockupData/advancedDropdown.json'
let getProvisionD = '/opennms/rest/cm/provisiond/default'
const nodeData = axios.get(url)
const apiTypes = axios.get(typesUrl)
const apiPeriod = axios.get(periodUrl)
const  apiAdvDropdown = axios.get(advDropdownUrl)

const apigetProvisionD =  axiosAuth.get(getProvisionD)

  export { nodeData, apiTypes, apiPeriod, apiAdvDropdown, apigetProvisionD }
