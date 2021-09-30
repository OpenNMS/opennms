import { v2 } from './axiosInstances'
import {
    QueryParameters,
    GraphNodesApiResponse
} from '@/types'
import { queryParametersHandler } from './serviceHelpers'

const endpoint = '/graphs/nodes/nodes'

const getGraphNodesNodes = async (queryParameters?: QueryParameters): Promise<GraphNodesApiResponse | false> => {
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

export {
    getGraphNodesNodes,
}