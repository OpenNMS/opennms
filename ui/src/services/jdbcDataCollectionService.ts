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
  JdbcCollection,
  JdbcDataCollectionMetadata,
  JdbcSource,
  JdbcSourceSummary
} from '@/types/jdbcDataCollection'
import { v2 } from './axiosInstances'

const endpoint = '/jdbc-datacollection'

/** The server's own message, so validation failures reach the user verbatim. */
const getErrorMessage = (error: unknown, fallbackMessage: string): string => {
  if (axios.isAxiosError(error)) {
    const responseData = error.response?.data

    if (typeof responseData === 'string' && responseData.trim().length > 0) {
      return responseData
    }

    if (typeof error.message === 'string' && error.message.trim().length > 0) {
      return error.message
    }
  }

  if (error instanceof Error && error.message.trim().length > 0) {
    return error.message
  }

  return fallbackMessage
}

const failure = (error: unknown, fallbackMessage: string): ValidationResult => {
  console.error(fallbackMessage, error)
  const result = createFailureResult(getErrorMessage(error, fallbackMessage))
  // The store tells a 412 apart: it means the copy it holds is stale, not that the change was invalid.
  if (axios.isAxiosError(error) && error.response) {
    result.status = error.response.status
  }
  return result
}

const failureWithPayload = <T>(error: unknown, fallbackMessage: string): ValidationResultWithPayload<T> => {
  const result: ValidationResultWithPayload<T> = failure(error, fallbackMessage)
  result.payload = undefined
  return result
}

/** The ETag a GET returned, ready to be sent back as If-Match. */
const ifMatchHeaders = (version?: string) => (version ? { headers: { 'If-Match': `"${version}"` }} : undefined)

const sourcePath = (id: string) => `${endpoint}/sources/${encodeURIComponent(id)}`
const collectionPath = (id: string, name: string) => `${sourcePath(id)}/collections/${encodeURIComponent(name)}`

export const getJdbcSources = async (): Promise<ValidationResultWithPayload<JdbcSourceSummary[]>> => {
  try {
    const response = await v2.get(`${endpoint}/sources`)
    return createResultWithPayload<JdbcSourceSummary[]>(true, '', response.data ?? [])
  } catch (error) {
    return failureWithPayload<JdbcSourceSummary[]>(error, 'Failed to retrieve the JDBC data collection sources.')
  }
}

export const getJdbcSource = async (id: string): Promise<ValidationResultWithPayload<JdbcSource>> => {
  try {
    const response = await v2.get(sourcePath(id))
    return createResultWithPayload<JdbcSource>(true, '', response.data)
  } catch (error) {
    return failureWithPayload<JdbcSource>(error, `Failed to retrieve JDBC data collection source '${id}'.`)
  }
}

export const createJdbcSource = async (source: JdbcSource): Promise<ValidationResult> => {
  try {
    await v2.post(`${endpoint}/sources`, source)
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to create JDBC data collection source '${source.id}'.`)
  }
}

export const updateJdbcSource = async (source: JdbcSource): Promise<ValidationResult> => {
  try {
    await v2.put(sourcePath(source.id), source, ifMatchHeaders(source.version))
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to save JDBC data collection source '${source.id}'.`)
  }
}

export const deleteJdbcSource = async (id: string, version?: string): Promise<ValidationResult> => {
  try {
    await v2.delete(sourcePath(id), ifMatchHeaders(version))
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to delete JDBC data collection source '${id}'.`)
  }
}

export const createJdbcCollection = async (id: string, collection: JdbcCollection, version?: string): Promise<ValidationResult> => {
  try {
    await v2.post(`${sourcePath(id)}/collections`, collection, ifMatchHeaders(version))
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to create JDBC collection '${collection.name}'.`)
  }
}

export const updateJdbcCollection = async (
  id: string,
  name: string,
  collection: JdbcCollection,
  version?: string
): Promise<ValidationResult> => {
  try {
    await v2.put(collectionPath(id, name), collection, ifMatchHeaders(version))
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to save JDBC collection '${name}'.`)
  }
}

export const deleteJdbcCollection = async (id: string, name: string, version?: string): Promise<ValidationResult> => {
  try {
    await v2.delete(collectionPath(id, name), ifMatchHeaders(version))
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to delete JDBC collection '${name}'.`)
  }
}

export const getJdbcDataCollectionMetadata = async (): Promise<ValidationResultWithPayload<JdbcDataCollectionMetadata>> => {
  try {
    const response = await v2.get(`${endpoint}/metadata`)
    return createResultWithPayload<JdbcDataCollectionMetadata>(true, '', response.data)
  } catch (error) {
    return failureWithPayload<JdbcDataCollectionMetadata>(error, 'Failed to retrieve the JDBC data collection metadata.')
  }
}

/** The raw response, for useDownload; null when the download failed. */
export const downloadJdbcSource = async (id: string, isXml: boolean) => {
  try {
    return await v2.get(`${sourcePath(id)}/download`, {
      params: { format: isXml ? 'xml' : 'json' },
      responseType: 'blob'
    })
  } catch (error) {
    console.error(`Failed to download JDBC data collection source '${id}'.`, error)
    return null
  }
}

export const uploadJdbcSource = async (
  file: File,
  id: string,
  replace: boolean
): Promise<ValidationResultWithPayload<JdbcSourceSummary>> => {
  try {
    const formData = new FormData()
    formData.append('upload', file)
    const params: Record<string, string | boolean> = { replace }
    if (id.trim()) {
      params.id = id.trim()
    }
    const response = await v2.post(`${endpoint}/upload`, formData, { params })
    return createResultWithPayload<JdbcSourceSummary>(true, '', response.data)
  } catch (error) {
    return failureWithPayload<JdbcSourceSummary>(error, `Failed to upload '${file.name}'.`)
  }
}
