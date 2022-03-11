import { queryParametersHandler } from './serviceHelpers'
import { rest } from './axiosInstances'
import { DeviceConfigBackup, DeviceConfigQueryParams } from '@/types/deviceConfig'

const endpoint = 'device-config'

const getDeviceConfigBackups = async (queryParameters: DeviceConfigQueryParams): Promise<DeviceConfigBackup[]>  => {
  try {
    const endpointWithQueryString = queryParametersHandler(queryParameters, endpoint)
    const resp = await rest.get(endpointWithQueryString)
    return resp.data
  } catch (err) {
    // mock data
    return [
      {
        id: 123,
        deviceName: 'Cisco-7201',
        location: 'location',
        ipAddress: '10.21.10.81',
        lastSucceeded: '1643831118973',
        lastUpdated: '1643831118973',
        backupStatus: 'Success',
        scheduleDate: '1643831118973',
        scheduleInterval: 'daily'
      } as any,
      {
        id: 12,
        deviceName: 'Aruba-7003-1',
        location: 'location',
        ipAddress: '10.21.10.81',
        lastSucceeded: '1643831118973',
        lastUpdated: '1643831118973',
        backupStatus: 'Failed',
        scheduleDate: '1643831118973',
        scheduleInterval: 'daily'
      } as any,
      {
        id: 122,
        deviceName: 'Cisco-7201',
        location: 'location',
        ipAddress: '10.21.10.81',
        lastSucceeded: '1643831118973',
        lastUpdated: '1643831118973',
        backupStatus: 'Paused',
        scheduleDate: '1643831118973',
        scheduleInterval: 'daily'
      } as any,
      {
        id: 55,
        deviceName: 'Aruba-7003-1',
        location: 'location',
        ipAddress: '10.21.10.81',
        lastSucceeded: '1643831118973',
        lastUpdated: '1643831118973',
        backupStatus: 'No Backup',
        scheduleDate: '1643831118973',
        scheduleInterval: 'daily'
      } as any,
    ]
  }
}

const downloadDeviceConfigs = async (deviceIds: number[])=> {
  const queryString = `?id=${deviceIds.join(',')}`
  try {
    const resp = await rest.get(`${endpoint}/download${queryString}`)
    return resp.data
  } catch (err) {
    return {}
  }
}

const backupDeviceConfig = async (deviceConfig: DeviceConfigBackup) => {
  try {
    const resp = await rest.post(endpoint, deviceConfig)
    return resp.data
  } catch (err) {
    return {}
  }
}

export { getDeviceConfigBackups, backupDeviceConfig, downloadDeviceConfigs }
