import { rest } from '@/services/axiosInstances'
import { Store } from 'vuex'

const getProvisionD = '/cm/provisiond/default'

const populateProvisionD = (store: Store<unknown>) => {
  store.dispatch('configuration/getProvisionDService')
}

const getProvisionDService = async () => {
  try {
    const response = await rest.get(getProvisionD)
    if (response.status === 200) {
      return response.data
    }
  } catch (err) {
    console.error('issue with getProvisionDService api', err)
  }
}

const putProvisionDService = async (payload: any) => {
  const resp = await rest.put(getProvisionD, payload)

  try {
    if (resp.status === 200) {
      return resp
    }
  } catch {
    console.error('issue with putProvisionDService api')
  }
}

export { getProvisionDService, putProvisionDService, populateProvisionD }
