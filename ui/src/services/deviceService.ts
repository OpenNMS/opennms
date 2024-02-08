///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

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
