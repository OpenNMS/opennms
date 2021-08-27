//this file is copied from vue-ui.
import { v2 } from './axiosInstances'
import {
    NodeApiResponse,
    QueryParameters,
} from '@/types'
import { queryParametersHandler } from './serviceHelpers'


const endpoint = '/nodes'

const getNodes = async (queryParameters?: QueryParameters): Promise<NodeApiResponse | false> => {
    let endpointWithQueryString = ''

    if (queryParameters) {
        endpointWithQueryString = queryParametersHandler(queryParameters, endpoint)
    }

    try {
        const resp = await v2.get(endpointWithQueryString || endpoint)

        // no content from server
        if (resp.status === 204) {
            return { node: [], totalCount: 0, count: 0, offset: 0 }
        }

        return resp.data
    } catch (err) {
        return false
    }
}

export {
    getNodes,
}