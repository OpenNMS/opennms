import { v2 } from './axiosInstances'
import { MonitoringLocationApiResponse } from '@/types'

const endpoint = '/monitoringLocations'

export const getMonitoringLocations = async (): Promise<MonitoringLocationApiResponse| false> => {
  try {
    const resp = await v2.get(endpoint)

    const data = resp.data as MonitoringLocationApiResponse

    // map these to typed fields for clarity in calling code
    data.location.forEach(loc => {
      loc.name = loc['location-name']
      loc.area = loc['monitoring-area']
    })

    return data
  } catch (err) {
    return false
  }
}
