import { queryParametersHandler } from './serviceHelpers'
import { rest } from './axiosInstances'
import { DeviceConfigBackup, DeviceConfigQueryParams } from '@/types/deviceConfig'

const endpoint = 'device-config'

const getDeviceConfigBackups = async (queryParameters: DeviceConfigQueryParams): Promise<DeviceConfigBackup[]> => {
  try {
    const endpointWithQueryString = queryParametersHandler(queryParameters, endpoint)
    const resp = await rest.get(endpointWithQueryString)
    return resp.data
  } catch (err) {
    return []
  }
}

const downloadDeviceConfigs = async (deviceIds: number[]) => {
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
    const resp = await rest.post(`${endpoint}/backup`, deviceConfig)
    return resp.data
  } catch (err) {
    return {}
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

export { 
  getDeviceConfigBackups, 
  backupDeviceConfig, 
  downloadDeviceConfigs, 
  getVendorOptions, 
  getOsImageOptions 
}
