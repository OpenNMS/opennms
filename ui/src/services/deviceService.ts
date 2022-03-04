import { QueryParameters } from '@/types'
import { queryParametersHandler } from './serviceHelpers'
import { v2 } from './axiosInstances'

const endpoint = 'devicesConfigBackup'

const getDeviceConfigBackups = async (queryParameters: QueryParameters) => {
  try {
    const endpointWithQueryString = queryParametersHandler(queryParameters, endpoint)
    const resp = await v2.get(endpointWithQueryString)
    return resp.data
  } catch (err) {
    // mock data
    return [
      {
        id: '123',
        name: 'Cisco-7201',
        location: 'location',
        ipAddress: '10.21.10.81',
        lastBackup: '1643831118973',
        lastAttempted: '1643831118973',
        backupStatus: 'Completed/Success',
        scheduleDate: '1643831118973',
        scheduleInterval: 'daily'
      },
      {
        id: '1234',
        name: 'Aruba-7003-1',
        location: 'location',
        ipAddress: '10.21.10.81',
        lastBackup: '1643831118973',
        lastAttempted: '1643831118973',
        backupStatus: 'Failed',
        scheduleDate: '1643831118973',
        scheduleInterval: 'daily'
      }
    ]
  }
}

const downloadDeviceConfigById = async (id: string) => {
  try {
    const resp = await v2.post(`${endpoint}/download/${id}`)
    return resp.data
  } catch (err) {
    return {}
  }
}

const backupDeviceConfigByIds = async (ids: string[]) => {
  try {
    const resp = await v2.post(endpoint, { ids })
    return resp.data
  } catch (err) {
    return {}
  }
}

export { getDeviceConfigBackups, downloadDeviceConfigById, backupDeviceConfigByIds }
