import { rest } from './axiosInstances'
import { LocationsApiResponse } from '@/types'

const endpoint = '/monitoringLocations'

const getLocations = async (): Promise<LocationsApiResponse | false> => {
  try {
    const resp = await rest.get(endpoint)

    if (resp.status === 204) {
      return {} as any
    }

    return resp.data
  } catch (err) {
    return false
  }
}

export { getLocations }
