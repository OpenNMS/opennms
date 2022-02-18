import { QueryParameters } from '@/types'
import { VerticesAndEdges } from '@/types/topology'
import { queryParametersHandler } from './serviceHelpers'
import { v2 } from './axiosInstances'

const endpoint = '/graphs/nodes/nodes'

const getVerticesAndEdges = async (queryParameters?: QueryParameters): Promise<VerticesAndEdges | false> => {
  let endpointWithQueryString = ''

  if (queryParameters) {
    endpointWithQueryString = queryParametersHandler(queryParameters, endpoint)
  }

  try {
    const resp = await v2.get(endpointWithQueryString || endpoint)

    // no content from server
    if (resp.status === 204) {
      return { vertices: [], edges: [] }
    }

    return resp.data
  } catch (err) {
    return false
  }
}

const getTopologyDataByLevelAndFocus = async (semanticZoomLevel: number, focusIds: string[]): Promise<any> => {
  const payload = {
    semanticZoomLevel: semanticZoomLevel,
    verticesInFocus: focusIds
  }
  try {
    const response = await v2.post(endpoint, payload)
    return response.data
  } catch (error) {
    return []
  }
}

export { getVerticesAndEdges, getTopologyDataByLevelAndFocus }
