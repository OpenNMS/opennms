import axios from 'axios'
import { rest } from '@/services/axiosInstances'
import { useStore } from 'vuex'
import typesUrl from '../../src/components/Common/Demo/MockupData/types.json'
import periodUrl from '../../src/components/Common/Demo/MockupData/schedulePeriod.json'
import advDropdownUrl from '../../src/components/Common/Demo/MockupData/advancedDropdown.json'

let getProvisionD = '/cm/provisiond/default'
const store = useStore()
const getDropdownTypes = typesUrl;
const getSchedulePeriod = periodUrl;
const getAdvancedDropdown = advDropdownUrl;
const getProvisionDService = rest.get(getProvisionD).then((response) => {
  try {
    console.log(response, response.data );
    if (response.status === 200) {
      return response.data
    }
  } catch {
    console.error('issue with getProvisionDService api')
  }
})

const putProvisionDService = async (payload: any) => {
  await rest.put(getProvisionD, payload).then((response) => {
   
    try {
      if (response.status === 200) {
        console.log('Data sucessfuly updated', response);
        return response;
      }
    } catch {
      console.error('issue with putProvisionDService api')
    }
  })
}

export { getDropdownTypes, getSchedulePeriod, getAdvancedDropdown, getProvisionDService, putProvisionDService }
