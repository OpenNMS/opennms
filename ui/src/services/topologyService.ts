import { QueryParameters } from '@/types'
import { SZLRequest, VerticesAndEdges } from '@/types/topology'
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
      return { vertices: [], edges: [], defaultFocus: { type: '', vertexIds: [{} as any] } }
    }

    return resp.data
  } catch (err) {
    return false
  }
}

const getTopologyDataByLevelAndFocus = async (payload: SZLRequest): Promise<VerticesAndEdges | false> => {
  try {
    const resp = await v2.post(endpoint, payload)

    // no content from server
    if (resp.status === 204) {
      return { vertices: [], edges: [], focus: { semanticZoomLevel: 1, vertices: [] } }
    }

    return resp.data
  } catch (error) {
    return false
  }
}

export { getVerticesAndEdges, getTopologyDataByLevelAndFocus }
