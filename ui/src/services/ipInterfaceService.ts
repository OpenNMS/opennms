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

/**
 * Construct the '_s' part of the getIpInterfaces query string with the given node ids and whether
 * to return only managed interfaces or all.
 * Use this in QueryParameters passed to getIpInterfaces.
 */
export const getNodeIpInterfaceQuery = (nodeIds: string[], managedOnly: boolean) => {
  const ids = nodeIds.map(id => `node.id==${id}`).join(',')

  const managedQuery = managedOnly ? ';isManaged==M' : ''

  return `(${ids}${managedQuery})`
}
