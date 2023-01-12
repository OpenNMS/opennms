import API from '@/services'
import {
  QueryParameters,
  VuexContext
} from '@/types'

const getAllIpInterfaces = async (context: VuexContext, queryParameters?: QueryParameters) => {
  const defaultParams = queryParameters || { limit: 5000, offset: 0 }
  const resp = await API.getIpInterfaces(defaultParams)

  if (resp) {
    context.commit('SAVE_ALL_IP_INTERFACES_TO_STATE', resp.ipInterface)
  }
}

export default {
  getAllIpInterfaces
}
