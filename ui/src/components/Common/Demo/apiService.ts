import axios from 'axios'

let url = 'src/components/Common/Demo/MockupData/nodeData.json'
let typesUrl = 'src/components/Common/Demo/MockupData/types.json'
let periodUrl = 'src/components/Common/Demo/MockupData/schedulePeriod.json'
let advDropdownUrl = 'src/components/Common/Demo/MockupData/advancedDropdown.json'
let getProvisionD = 'http://20.102.41.29:8980/opennms/rest/cm/provisiond/default'
const nodeData = axios.get(url)
const apiTypes = axios.get(typesUrl)
const apiPeriod = axios.get(periodUrl)
const  apiAdvDropdown = axios.get(advDropdownUrl)

const apigetProvisionD =  axios.get(getProvisionD, {
    headers: { "Authorization": "Basic " + btoa("admin:admin") }
  }).then(function(response) {
    console.log('Authenticated');
    return response;
  }).catch(function(error) {
    console.log('Error on Authentication');
  });

  export { nodeData, apiTypes, apiPeriod, apiAdvDropdown, apigetProvisionD }
