import { v2 } from './axiosInstances'
import {
  IpInterfaceApiResponse,
  QueryParameters
} from '@/types'
import { queryParametersHandler } from './serviceHelpers'

const endpoint = '/ipinterfaces'

export const getIpInterfaces = async (queryParameters?: QueryParameters): Promise<IpInterfaceApiResponse | false> => {
  let endpointWithQueryString = ''

  if (queryParameters) {
    endpointWithQueryString = queryParametersHandler(queryParameters, endpoint)
  }

  try {
    const resp = await v2.get(endpointWithQueryString || endpoint)
    return resp.data as IpInterfaceApiResponse
  } catch (err) {
    return false
  }
}

// TODO: May not need this
export const getIpInterfacesByNodeIds = async (nodeIds: number[]): Promise<IpInterfaceApiResponse | false> => {
  try {
    const query = nodeIds.map(id => `node.id==${id}`).join(',')

    const resp = await v2.get(`${endpoint}?_s=${query}`)
    return resp.data as IpInterfaceApiResponse
  } catch (err) {
    return false
  }
}
