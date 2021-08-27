import { v2 } from './axiosInstances'
import {
    NodeApiResponse,
    QueryParameters,
} from '@/types'
import { queryParametersHandler } from './serviceHelpers'


const endpoint = '/alarms'

const getAlarms = async (queryParameters?: QueryParameters): Promise<AlarmApiResponse | false> => {
    let endpointWithQueryString = ''

    if (queryParameters) {
        endpointWithQueryString = queryParametersHandler(queryParameters, endpoint)
    }

    try {
        const resp = await v2.get(endpointWithQueryString || endpoint)

        // no content from server
        if (resp.status === 204) {
            return { alarm: [], totalCount: 0, count: 0, offset: 0 }
        }

        return resp.data
    } catch (err) {
        return false
    }
}

export {
    getAlarms,
}