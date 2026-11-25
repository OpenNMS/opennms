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

import axios from 'axios'
import { createFailureResult, createResultWithPayload, createSuccessResponse } from '@/types/validation'
import type { ValidationResult, ValidationResultWithPayload } from '@/types/validation'
import type {
  ResourceTypesConfig,
  ResourceTypesConfigSummary,
  ResourceTypesMetadata
} from '@/types/resourceTypesConfig'
import { v2 } from './axiosInstances'
import { serverMessage } from './serverMessage'

const endpoint = '/resource-types'

const configPath = (id: string) => `${endpoint}/configs/${encodeURIComponent(id)}`

const failure = (error: unknown, fallbackMessage: string): ValidationResult => {
  console.error(fallbackMessage, error)
  const result = createFailureResult(serverMessage(error, fallbackMessage))
  // The store tells a 412 apart: it means the copy it holds is stale, not that the change was invalid.
  if (axios.isAxiosError(error) && error.response) {
    result.status = error.response.status
  }
  return result
}

const failureWithPayload = <T>(error: unknown, fallbackMessage: string): ValidationResultWithPayload<T> => {
  console.error(fallbackMessage, error)
  return createResultWithPayload<T>(false, serverMessage(error, fallbackMessage))
}

/** The ETag a GET returned, ready to be sent back as If-Match. */
const ifMatchHeaders = (version?: string) => (version ? { headers: { 'If-Match': `"${version}"` }} : undefined)

export const getResourceTypesConfigs = async (): Promise<ValidationResultWithPayload<ResourceTypesConfigSummary[]>> => {
  try {
    const response = await v2.get(`${endpoint}/configs`)
    return createResultWithPayload<ResourceTypesConfigSummary[]>(true, '', response.data ?? [])
  } catch (error) {
    return failureWithPayload<ResourceTypesConfigSummary[]>(error, 'Failed to retrieve the resource types configurations.')
  }
}

export const getResourceTypesConfig = async (id: string): Promise<ValidationResultWithPayload<ResourceTypesConfig>> => {
  try {
    const response = await v2.get(configPath(id))
    return createResultWithPayload<ResourceTypesConfig>(true, '', response.data)
  } catch (error) {
    return failureWithPayload<ResourceTypesConfig>(error, `Failed to retrieve resource types configuration '${id}'.`)
  }
}

export const createResourceTypesConfig = async (config: ResourceTypesConfig): Promise<ValidationResult> => {
  try {
    await v2.post(`${endpoint}/configs`, config)
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to create resource types configuration '${config.id}'.`)
  }
}

/** Replaces the configuration stored under id; a different config.id renames it. */
export const updateResourceTypesConfig = async (id: string, config: ResourceTypesConfig): Promise<ValidationResult> => {
  try {
    await v2.put(configPath(id), config, ifMatchHeaders(config.version))
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to save resource types configuration '${id}'.`)
  }
}

export const deleteResourceTypesConfig = async (id: string, version?: string): Promise<ValidationResult> => {
  try {
    await v2.delete(configPath(id), ifMatchHeaders(version))
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to delete resource types configuration '${id}'.`)
  }
}

export const getResourceTypesMetadata = async (): Promise<ValidationResultWithPayload<ResourceTypesMetadata>> => {
  try {
    const response = await v2.get(`${endpoint}/metadata`)
    return createResultWithPayload<ResourceTypesMetadata>(true, '', response.data)
  } catch (error) {
    return failureWithPayload<ResourceTypesMetadata>(error, 'Failed to retrieve the resource types metadata.')
  }
}

export const downloadResourceTypesConfig = async (id: string, isXml: boolean) => {
  try {
    return await v2.get(`${configPath(id)}/download`, {
      params: { format: isXml ? 'xml' : 'json' },
      responseType: 'blob'
    })
  } catch (error) {
    console.error(`Failed to download resource types configuration '${id}'.`, error)
    return false
  }
}

/** Creates the configuration, or replaces it in full if it exists. */
export const uploadResourceTypesConfig = async (id: string, file: File, isXml: boolean): Promise<ValidationResult> => {
  try {
    const formData = new FormData()
    formData.append('upload', file)
    await v2.post(`${configPath(id)}/upload${isXml ? '/xml' : ''}`, formData)
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to upload resource types configuration '${id}'.`)
  }
}
