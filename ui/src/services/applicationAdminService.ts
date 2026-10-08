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

import { ApplicationMembers, ApplicationMembersUpdate, ApplicationSummary, ServiceCandidatePage } from '@/types/applicationAdmin'
import { createFailureResult, createResultWithPayload, createSuccessResponse, ValidationResult, ValidationResultWithPayload } from '@/types/validation'
import { v2 } from './axiosInstances'

// Manage Applications (NMS-20425), over /api/v2/applications: the generic
// create and delete, plus the summaries, members and service-candidates operations.

const endpoint = '/applications'

// Only surface a server detail from a 4xx that looks like a short, plain message;
// a 500 carries an HTML page or a raw persistence exception, neither for the user.
const errorMessage = (err: any, fallback: string): string => {
  const detail = err?.response?.data
  const status = Number(err?.response?.status ?? 0)
  if (typeof detail === 'string' && status >= 400 && status < 500) {
    const trimmed = detail.trim()
    if (trimmed && trimmed.length <= 200 && !/[<>]/.test(trimmed)) {
      return trimmed
    }
  }
  return fallback
}

// null on failure (not []) so callers can keep showing the previous list
const listApplicationSummaries = async (): Promise<ApplicationSummary[] | null> => {
  try {
    const resp = await v2.get(`${endpoint}/summaries`)
    return Array.isArray(resp.data) ? resp.data : []
  } catch (err) {
    console.error('Error loading applications:', err)
    return null
  }
}

// the payload is the new id, read from the Location header; undefined if it is missing
const createApplication = async (name: string): Promise<ValidationResultWithPayload<number>> => {
  try {
    const resp = await v2.post(endpoint, { name })
    const location = String(resp?.headers?.location ?? '')
    const id = Number(location.match(/\/(\d+)\/?$/)?.[1])
    return createResultWithPayload(true, '', Number.isFinite(id) ? id : undefined)
  } catch (err: any) {
    console.error('Error creating application:', err)
    return createFailureResult(errorMessage(err, `Failed to create application '${name}'.`))
  }
}

const deleteApplication = async (id: number, name: string): Promise<ValidationResult> => {
  try {
    await v2.delete(`${endpoint}/${id}`)
    return createSuccessResponse()
  } catch (err: any) {
    console.error('Error deleting application:', err)
    return createFailureResult(errorMessage(err, `Failed to delete application '${name}'.`))
  }
}

// null when the members could not be read
const getApplicationMembers = async (id: number): Promise<ApplicationMembers | null> => {
  try {
    const resp = await v2.get(`${endpoint}/${id}/members`)
    const data = resp.data ?? {}
    return {
      id: data.id,
      name: data.name,
      services: Array.isArray(data.services) ? data.services : [],
      perspectiveLocations: Array.isArray(data.perspectiveLocations) ? data.perspectiveLocations : []
    }
  } catch (err) {
    console.error(`Error loading the members of application ${id}:`, err)
    return null
  }
}

const updateApplicationMembers = async (id: number, name: string, update: ApplicationMembersUpdate): Promise<ValidationResult> => {
  try {
    await v2.put(`${endpoint}/${id}/members`, update)
    return createSuccessResponse()
  } catch (err: any) {
    console.error('Error updating application members:', err)
    return createFailureResult(errorMessage(err, `Failed to save application '${name}'.`))
  }
}

// null when the search failed
const searchServiceCandidates = async (search: string, limit: number): Promise<ServiceCandidatePage | null> => {
  try {
    const resp = await v2.get(`${endpoint}/service-candidates`, { params: { search, limit }})
    const data = resp.data ?? {}
    return {
      totalCount: Number(data.totalCount ?? 0),
      services: Array.isArray(data.services) ? data.services : []
    }
  } catch (err) {
    console.error('Error searching monitored services:', err)
    return null
  }
}

export {
  createApplication,
  deleteApplication,
  getApplicationMembers,
  listApplicationSummaries,
  searchServiceCandidates,
  updateApplicationMembers
}
