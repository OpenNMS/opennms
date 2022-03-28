import { queryParametersHandler } from './serviceHelpers'
import { rest } from './axiosInstances'
import { DeviceConfigBackup, DeviceConfigQueryParams } from '@/types/deviceConfig'
import { AxiosResponse } from 'axios'
import { orderBy } from 'lodash'

const endpoint = 'device-config'

const getDeviceConfigBackups = async (queryParameters: DeviceConfigQueryParams): Promise<AxiosResponse | false> => {
  try {
    const endpointWithQueryString = queryParametersHandler(queryParameters, endpoint)
    return await rest.get(endpointWithQueryString)
  } catch (err) {
    return false
  }
}

const downloadDeviceConfigs = async (deviceIds: number[]) => {
  const queryString = `?id=${deviceIds.join(',')}`
  try {
    return await rest.get(`${endpoint}/download${queryString}`)
  } catch (err) {
    return false
  }
}

const backupDeviceConfig = async ({ ipAddress, location, configType }: DeviceConfigBackup) => {
  try {
    const serviceName = `DeviceConfig-${configType}` 
    const resp = await rest.post(`${endpoint}/backup`, { ipAddress, location, serviceName })
    return resp.data
  } catch (err) {
    return false
  }
}

const getVendorOptions = async (): Promise<string[]> => {
  try {
    const resp = await rest.get(`${endpoint}/vendor-options`)
    return resp.data
  } catch (err) {
    return []
  }
}

const getOsImageOptions = async (): Promise<string[]> => {
  try {
    const resp = await rest.get(`${endpoint}/os-image-options`)
    return resp.data
  } catch (err) {
    return []
  }
}

const getHistoryByIpInterface = async (ipInterfaceId: number): Promise<DeviceConfigBackup[]> => {
  try {
    const resp: { data: DeviceConfigBackup[] } = await rest.get(`${endpoint}?ipInterfaceId=${ipInterfaceId}`)
    const devicesWithBackupDate = resp.data.filter((device) => device.lastBackupDate)
    return orderBy(devicesWithBackupDate, 'lastBackupDate', 'desc')
  } catch (err) {
    return []
  }
}

export { getDeviceConfigBackups, backupDeviceConfig, downloadDeviceConfigs, getVendorOptions, getOsImageOptions, getHistoryByIpInterface }
