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
  HttpCollection,
  HttpCollectionMetadata,
  HttpCollectionSettings,
  HttpCollectionSummary
} from '@/types/httpCollectionConfig'
import { v2 } from './axiosInstances'

const endpoint = '/httpcollection'

/** Pulls the server's own message out of an axios error, so validation failures reach the user verbatim. */
const getErrorMessage = (error: unknown, fallbackMessage: string): string => {
  if (axios.isAxiosError(error)) {
    const responseData = error.response?.data

    if (typeof responseData === 'string' && responseData.trim().length > 0) {
      return responseData
    }

    if (
      responseData &&
      typeof responseData === 'object' &&
      'message' in responseData &&
      typeof responseData.message === 'string' &&
      responseData.message.trim().length > 0
    ) {
      return responseData.message
    }
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
  console.error(fallbackMessage, error)
  const result = createResultWithPayload<T>(false, getErrorMessage(error, fallbackMessage))
  if (axios.isAxiosError(error) && error.response) {
    result.status = error.response.status
  }
  return result
}

/** The ETag a GET returned, ready to be sent back as If-Match. */
const ifMatchHeaders = (version?: string) => (version ? { headers: { 'If-Match': `"${version}"` }} : undefined)

const encodeName = (name: string) => encodeURIComponent(name)

export const getHttpCollectionSettings = async (): Promise<ValidationResultWithPayload<HttpCollectionSettings>> => {
  try {
    const response = await v2.get(`${endpoint}/config`)
    return createResultWithPayload<HttpCollectionSettings>(true, '', response.data)
  } catch (error) {
    return failureWithPayload<HttpCollectionSettings>(error, 'Failed to retrieve the HTTP collector settings.')
  }
}

export const updateHttpCollectionSettings = async (settings: HttpCollectionSettings): Promise<ValidationResult> => {
  try {
    await v2.put(`${endpoint}/config`, { rrdRepository: settings.rrdRepository }, ifMatchHeaders(settings.version))
    return createSuccessResponse()
  } catch (error) {
    return failure(error, 'Failed to save the HTTP collector settings.')
  }
}

export const getHttpCollections = async (): Promise<ValidationResultWithPayload<HttpCollectionSummary[]>> => {
  try {
    const response = await v2.get(`${endpoint}/collections`)
    return createResultWithPayload<HttpCollectionSummary[]>(true, '', response.data ?? [])
  } catch (error) {
    return failureWithPayload<HttpCollectionSummary[]>(error, 'Failed to retrieve the HTTP collections.')
  }
}

export const getHttpCollection = async (name: string): Promise<ValidationResultWithPayload<HttpCollection>> => {
  try {
    const response = await v2.get(`${endpoint}/collections/${encodeName(name)}`)
    return createResultWithPayload<HttpCollection>(true, '', response.data)
  } catch (error) {
    return failureWithPayload<HttpCollection>(error, `Failed to retrieve HTTP collection '${name}'.`)
  }
}

export const createHttpCollection = async (collection: HttpCollection): Promise<ValidationResult> => {
  try {
    const { version: _version, ...body } = collection
    await v2.post(`${endpoint}/collections`, body)
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to create HTTP collection '${collection.name}'.`)
  }
}

export const updateHttpCollection = async (name: string, collection: HttpCollection): Promise<ValidationResult> => {
  try {
    const { version, ...body } = collection
    await v2.put(`${endpoint}/collections/${encodeName(name)}`, body, ifMatchHeaders(version))
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to save HTTP collection '${name}'.`)
  }
}

export const deleteHttpCollection = async (name: string, version?: string): Promise<ValidationResult> => {
  try {
    await v2.delete(`${endpoint}/collections/${encodeName(name)}`, ifMatchHeaders(version))
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to delete HTTP collection '${name}'.`)
  }
}

export const getHttpCollectionMetadata = async (): Promise<ValidationResultWithPayload<HttpCollectionMetadata>> => {
  try {
    const response = await v2.get(`${endpoint}/metadata`)
    return createResultWithPayload<HttpCollectionMetadata>(true, '', response.data)
  } catch (error) {
    return failureWithPayload<HttpCollectionMetadata>(error, 'Failed to retrieve the HTTP collection metadata.')
  }
}

export const downloadHttpCollectionConfiguration = async (isXml: boolean) => {
  try {
    return await v2.get(`${endpoint}/download`, {
      params: { format: isXml ? 'xml' : 'json' },
      responseType: 'blob'
    })
  } catch (error) {
    console.error('Failed to download the HTTP collector configuration.', error)
    return false
  }
}

export const uploadHttpCollectionConfiguration = async (file: File, isXml: boolean): Promise<ValidationResult> => {
  try {
    const formData = new FormData()
    formData.append('upload', file)
    await v2.post(`${endpoint}/upload${isXml ? '/xml' : ''}`, formData)
    return createSuccessResponse()
  } catch (error) {
    return failure(error, 'Failed to upload the HTTP collector configuration.')
  }
}
