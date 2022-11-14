import { queryParametersHandler } from './serviceHelpers'
import { rest } from './axiosInstances'
import { DeviceConfigBackup, DeviceConfigQueryParams } from '@/types/deviceConfig'
import { AxiosResponse } from 'axios'
import { orderBy } from 'lodash'
import useSpinner from '@/composables/useSpinner'

const endpoint = 'device-config'

const { startSpinner, stopSpinner } = useSpinner()

const getDeviceConfigBackups = async (queryParameters: DeviceConfigQueryParams): Promise<AxiosResponse | false> => {
  startSpinner()

  try {
    const endpointWithQueryString = queryParametersHandler(queryParameters, `${endpoint}/latest`)
    return await rest.get(endpointWithQueryString)
  } catch (err) {
    return false
  } finally {
    stopSpinner()
  }
}

const downloadDeviceConfigs = async (deviceIds: number[]) => {
  startSpinner()

  const queryString = `?id=${deviceIds.join(',')}`
  try {
    return await rest.get(`${endpoint}/download${queryString}`, { responseType: 'blob' })
  } catch (err) {
    return false
  } finally {
    stopSpinner()
  }
}

const backupDeviceConfig = async (configs: DeviceConfigBackup[]) => {
  startSpinner()

  try {
    const config = configs.map(({ ipAddress, location, serviceName }) => ({ ipAddress, location, serviceName }))
    const resp = await rest.post(`${endpoint}/backup`, config)
    return resp
  } catch (err) {
    return false
  } finally {
    stopSpinner()
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

const getHistoryByIpInterface = async (ipInterfaceId: number, configType: string): Promise<DeviceConfigBackup[]> => {
  startSpinner()

  try {
    const resp: { data: DeviceConfigBackup[], status: number } = await rest.get(`${endpoint}/interface/${ipInterfaceId}?configType=${configType}`)
    if (resp.status === 204) return []

    const devicesWithBackupDate = resp.data.filter((device) => device.lastBackupDate)
    return orderBy(devicesWithBackupDate, 'lastBackupDate', 'desc')
  } catch (err) {
    return []
  } finally {
    stopSpinner()
  }
}

export {
  getDeviceConfigBackups,
  backupDeviceConfig,
  downloadDeviceConfigs,
  getVendorOptions,
  getOsImageOptions,
  getHistoryByIpInterface
}
