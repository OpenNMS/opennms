import { queryParametersHandler } from './serviceHelpers'
import { v2 } from './axiosInstances'
import { DeviceConfigQueryParams } from '@/types/deviceConfig'

const endpoint = 'device-config'

const getDeviceConfigBackups = async (queryParameters: DeviceConfigQueryParams) => {
  try {
    const endpointWithQueryString = queryParametersHandler(queryParameters, endpoint)
    const resp = await v2.get(endpointWithQueryString)
    return resp.data
  } catch (err) {
    // mock data
    return [
      {
        id: '123',
        deviceName: 'Cisco-7201',
        location: 'location',
        ipAddress: '10.21.10.81',
        lastSucceeded: '1643831118973',
        lastUpdated: '1643831118973',
        backupStatus: 'Completed/Success',
        scheduleDate: '1643831118973',
        scheduleInterval: 'daily'
      },
      {
        id: '1234',
        deviceName: 'Aruba-7003-1',
        location: 'location',
        ipAddress: '10.21.10.81',
        lastSucceeded: '1643831118973',
        lastUpdated: '1643831118973',
        backupStatus: 'Failed',
        scheduleDate: '1643831118973',
        scheduleInterval: 'daily'
      }
    ]
  }
}

const downloadDeviceConfigById = async (id: number) => {
  try {
    const resp = await v2.get(`${endpoint}/download/${id}`)
    return resp.data
  } catch (err) {
    return {}
  }
}

const downloadDeviceConfigs = async (deviceIds: number[]) => {
  try {
    const resp = await v2.post(`${endpoint}/download`, deviceIds)
    return resp.data
  } catch (err) {
    return {}
  }
}

const backupDeviceConfigByIds = async (ids: number[]) => {
  try {
    const resp = await v2.post(endpoint, { ids })
    return resp.data
  } catch (err) {
    return {}
  }
}

export { getDeviceConfigBackups, downloadDeviceConfigById, backupDeviceConfigByIds, downloadDeviceConfigs }
