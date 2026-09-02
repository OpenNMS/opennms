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
import { KscReport } from '@/types/ksc'

const endpoint = '/ksc'

// The list read lets errors propagate so the store can tell a real failure
// (backend down, config unreadable) apart from an empty configuration instead
// of rendering both as "no reports". Single-report read and mutations are
// handled by their callers.

const getKscReports = async (): Promise<KscReport[]> => {
  const resp = await rest.get(endpoint)
  if (resp.status === 204) {
    return []
  }
  return resp.data?.kscReport ?? []
}

const getKscReport = async (id: number): Promise<KscReport | null> => {
  try {
    const resp = await rest.get(`${endpoint}/${id}`)
    return resp.data
  } catch (_err) {
    return null
  }
}

// Returns the id the server assigned to the new report (parsed from the
// Location header), or null if it could not be determined.
const createKscReport = async (report: KscReport): Promise<number | null> => {
  const resp = await rest.post(endpoint, report)
  const location = (resp.headers?.location ?? '') as string
  const id = Number(location.split('/').filter(Boolean).pop())
  return Number.isFinite(id) ? id : null
}

const updateKscReport = async (id: number, report: KscReport): Promise<void> => {
  await rest.post(`${endpoint}/${id}`, report)
}

const deleteKscReport = async (id: number): Promise<void> => {
  await rest.delete(`${endpoint}/${id}`)
}

const reloadKscConfig = async (): Promise<void> => {
  await rest.put(`${endpoint}/reloadConfig`)
}

export { getKscReports, getKscReport, createKscReport, updateKscReport, deleteKscReport, reloadKscConfig }
