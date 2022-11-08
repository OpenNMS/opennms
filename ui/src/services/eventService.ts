import { v2 } from './axiosInstances'
import { QueryParameters, EventApiResponse } from '@/types'
import { queryParametersHandler } from './serviceHelpers'

const endpoint = '/events'

const getEvents = async (queryParameters?: QueryParameters): Promise<EventApiResponse | false> => {
  let endpointWithQueryString = ''

  if (queryParameters) {
    endpointWithQueryString = queryParametersHandler(queryParameters, endpoint)
  }

  try {
    const resp = await v2.get(endpointWithQueryString || endpoint)

    // no content from server
    if (resp.status === 204) {
      return { event: [], count: 0, offset: 0, totalCount: 0 }
    }

    return resp.data
  } catch (err) {
    return false
  }
}

export { getEvents }
