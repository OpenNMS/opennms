import axios from 'axios'

let url = 'src/components/Common/Demo/MockupData/nodeData.json'
let typesUrl = 'src/components/Common/Demo/MockupData/types.json'
let periodUrl = 'src/components/Common/Demo/MockupData/schedulePeriod.json'
let advDropdownUrl = 'src/components/Common/Demo/MockupData/advancedDropdown.json'

const nodeData = axios.get(url)
const apiTypes = axios.get(typesUrl)
const apiPeriod = axios.get(periodUrl)
const apiAdvDropdown = axios.get(advDropdownUrl)

export { nodeData, apiTypes, apiPeriod, apiAdvDropdown }
