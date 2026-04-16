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

import { rest } from './axiosInstances'
import {
  UsageStatisticsData,
  UsageStatisticsMetadata,
  UsageStatisticsStatus,
  ProductUpdateEnrollmentStatus,
  ProductUpdateEnrollmentFormData
} from '@/types/usageStatistics'

const endpoint = '/datachoices'

const getUsageStatistics = async (): Promise<UsageStatisticsData | false> => {
  try {
    const url = `${endpoint}`
    const resp = await rest.get(url)
    return resp.data
  } catch (err) {
    return false
  }
}

const getUsageStatisticsMetadata = async (): Promise<UsageStatisticsMetadata | false> => {
  try {
    const url = `${endpoint}/meta`
    const resp = await rest.get(url)
    return resp.data
  } catch (err) {
    return false
  }
}

const getUsageStatisticsStatus = async (): Promise<UsageStatisticsStatus | false> => {
  try {
    const url = `${endpoint}/status`
    const resp = await rest.get(url)
    return resp.data
  } catch (err) {
    return false
  }
}

const setUsageStatisticsStatus = async (enabled: boolean) : Promise<any | false> => {
  try {
    const status = {
      enabled
    }
    const url = `${endpoint}/status`
    const resp = await rest.post(url, status)
    return resp
  } catch (err) {
    return false
  }
}

const acknowledgeUsageStatisticsNotice = async (): Promise<any | false> => {
  try {
    const url = `${endpoint}/status`
    const resp = await rest.post(url, { initialNoticeAcknowledged: true })
    return resp
  } catch (err) {
    return false
  }
}

const getProductUpdateEnrollmentStatus = async (): Promise<ProductUpdateEnrollmentStatus | false> => {
  try {
    const url = `${endpoint}/productupdate/status`
    const resp = await rest.get(url)
    return resp.data
  } catch (err) {
    return false
  }
}

const setProductUpdateEnrollmentStatus = async (optedIn: boolean, noticeAcknowledged: boolean): Promise<any | false> => {
  try {
    const url = `${endpoint}/productupdate/status`
    const resp = await rest.post(url, { optedIn, noticeAcknowledged })
    return resp
  } catch (err) {
    return false
  }
}

const submitProductUpdateEnrollmentForm = async (data: ProductUpdateEnrollmentFormData): Promise<any | false> => {
  try {
    const url = `${endpoint}/productupdate/submit`
    const resp = await rest.post(url, data)
    return resp
  } catch (err) {
    return false
  }
}

export {
  getUsageStatistics,
  getUsageStatisticsMetadata,
  getUsageStatisticsStatus,
  setUsageStatisticsStatus,
  acknowledgeUsageStatisticsNotice,
  getProductUpdateEnrollmentStatus,
  setProductUpdateEnrollmentStatus,
  submitProductUpdateEnrollmentForm
}
