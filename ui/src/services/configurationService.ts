import { rest } from '@/services/axiosInstances'
import { Store } from 'vuex'

const getProvisionD = '/cm/provisiond/default'

const populateProvisionD = (store: Store<unknown>) => {
  store.dispatch('configuration/getProvisionDService')
}

const getProvisionDService = rest.get(getProvisionD).then((response) => {
  try {
    console.log(response, response.data)
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
        return response
      }
    } catch {
      console.error('issue with putProvisionDService api')
    }
  })
}

export { getProvisionDService, putProvisionDService, populateProvisionD }
