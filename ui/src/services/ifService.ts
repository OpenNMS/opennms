import { rest } from './axiosInstances'
import { QueryParameters, IfServiceApiResponse } from '@/types'
import { queryParametersHandler } from './serviceHelpers'

const endpoint = '/ifservices'

const getNodeIfServices = async (queryParameters?: QueryParameters): Promise<IfServiceApiResponse | false> => {
  let endpointWithQueryString = ''

  if (queryParameters) {
    endpointWithQueryString = queryParametersHandler(queryParameters, endpoint)
  }

  try {
    const resp = await rest.get(endpointWithQueryString || endpoint)

    if (resp.status === 204) {
      return { 'monitored-service': [], totalCount: 0, count: 0, offset: 0 }
    }

    return resp.data
  } catch (err) {
    return false
  }
}

export { getNodeIfServices }
