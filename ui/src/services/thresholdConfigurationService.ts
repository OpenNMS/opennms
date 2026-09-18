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
  ThreshdConfiguration,
  ThreshdPackage,
  ThreshdPackageSummary,
  ThresholdGroup,
  ThresholdGroupSummary,
  ThresholdingMetadata
} from '@/types/thresholdConfig'
import { v2 } from './axiosInstances'

const thresholdingEndpoint = '/thresholding'
const threshdEndpoint = '/threshd'

/**
 * Pulls the server's own message out of an axios error so validation failures reach the user verbatim
 * ("ds-name ... is longer than 19 characters") instead of a generic "request failed".
 */
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
  return createFailureResult(getErrorMessage(error, fallbackMessage))
}

const failureWithPayload = <T>(error: unknown, fallbackMessage: string): ValidationResultWithPayload<T> => {
  console.error(fallbackMessage, error)
  return createResultWithPayload<T>(false, getErrorMessage(error, fallbackMessage))
}

/** The ETag a GET returned, ready to be sent back as If-Match. */
const ifMatchHeaders = (version?: string) => (version ? { headers: { 'If-Match': `"${version}"` }} : undefined)

const encodeName = (name: string) => encodeURIComponent(name)

// -------------------------------------------------------------------- thresholds.xml

export const getThresholdGroups = async (): Promise<ValidationResultWithPayload<ThresholdGroupSummary[]>> => {
  try {
    const response = await v2.get(`${thresholdingEndpoint}/groups`)
    return createResultWithPayload<ThresholdGroupSummary[]>(true, '', response.data ?? [])
  } catch (error) {
    return failureWithPayload<ThresholdGroupSummary[]>(error, 'Failed to retrieve threshold groups.')
  }
}

export const getThresholdGroup = async (name: string): Promise<ValidationResultWithPayload<ThresholdGroup>> => {
  try {
    const response = await v2.get(`${thresholdingEndpoint}/groups/${encodeName(name)}`)
    return createResultWithPayload<ThresholdGroup>(true, '', response.data)
  } catch (error) {
    return failureWithPayload<ThresholdGroup>(error, `Failed to retrieve threshold group '${name}'.`)
  }
}

export const createThresholdGroup = async (group: ThresholdGroup): Promise<ValidationResult> => {
  try {
    await v2.post(`${thresholdingEndpoint}/groups`, group)
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to create threshold group '${group.name}'.`)
  }
}

export const updateThresholdGroup = async (name: string, group: ThresholdGroup): Promise<ValidationResult> => {
  try {
    await v2.put(`${thresholdingEndpoint}/groups/${encodeName(name)}`, group, ifMatchHeaders(group.version))
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to save threshold group '${name}'.`)
  }
}

export const deleteThresholdGroup = async (name: string, version?: string): Promise<ValidationResult> => {
  try {
    await v2.delete(`${thresholdingEndpoint}/groups/${encodeName(name)}`, ifMatchHeaders(version))
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to delete threshold group '${name}'.`)
  }
}

export const getThresholdingMetadata = async (): Promise<ValidationResultWithPayload<ThresholdingMetadata>> => {
  try {
    const response = await v2.get(`${thresholdingEndpoint}/metadata`)
    return createResultWithPayload<ThresholdingMetadata>(true, '', response.data)
  } catch (error) {
    return failureWithPayload<ThresholdingMetadata>(error, 'Failed to retrieve threshold metadata.')
  }
}

export const reloadThresholdingConfiguration = async (): Promise<ValidationResult> => {
  try {
    await v2.post(`${thresholdingEndpoint}/reload`)
    return createSuccessResponse()
  } catch (error) {
    return failure(error, 'Failed to reload the thresholding configuration.')
  }
}

export const downloadThresholdingConfiguration = async (isXml: boolean) => {
  try {
    return await v2.get(`${thresholdingEndpoint}/download`, {
      params: { format: isXml ? 'xml' : 'json' },
      responseType: 'blob'
    })
  } catch (error) {
    console.error('Failed to download the thresholding configuration.', error)
    return false
  }
}

export const uploadThresholdingConfiguration = async (file: File, isXml: boolean): Promise<ValidationResult> => {
  try {
    const formData = new FormData()
    formData.append('upload', file)
    await v2.post(`${thresholdingEndpoint}/upload${isXml ? '/xml' : ''}`, formData)
    return createSuccessResponse()
  } catch (error) {
    return failure(error, 'Failed to upload the thresholding configuration.')
  }
}

// --------------------------------------------------------- threshd-configuration.xml

export const getThreshdConfiguration = async (): Promise<ValidationResultWithPayload<ThreshdConfiguration>> => {
  try {
    const response = await v2.get(`${threshdEndpoint}/config`)
    return createResultWithPayload<ThreshdConfiguration>(true, '', response.data)
  } catch (error) {
    return failureWithPayload<ThreshdConfiguration>(error, 'Failed to retrieve the threshd configuration.')
  }
}

export const updateThreshdConfiguration = async (config: ThreshdConfiguration): Promise<ValidationResult> => {
  try {
    await v2.put(`${threshdEndpoint}/config`, config, ifMatchHeaders(config.version))
    return createSuccessResponse()
  } catch (error) {
    return failure(error, 'Failed to save the threshd configuration.')
  }
}

export const getThreshdPackages = async (): Promise<ValidationResultWithPayload<ThreshdPackageSummary[]>> => {
  try {
    const response = await v2.get(`${threshdEndpoint}/packages`)
    return createResultWithPayload<ThreshdPackageSummary[]>(true, '', response.data ?? [])
  } catch (error) {
    return failureWithPayload<ThreshdPackageSummary[]>(error, 'Failed to retrieve threshd packages.')
  }
}

export const getThreshdPackage = async (name: string): Promise<ValidationResultWithPayload<ThreshdPackage>> => {
  try {
    const response = await v2.get(`${threshdEndpoint}/packages/${encodeName(name)}`)
    return createResultWithPayload<ThreshdPackage>(true, '', response.data)
  } catch (error) {
    return failureWithPayload<ThreshdPackage>(error, `Failed to retrieve threshd package '${name}'.`)
  }
}

export const createThreshdPackage = async (pkg: ThreshdPackage): Promise<ValidationResult> => {
  try {
    await v2.post(`${threshdEndpoint}/packages`, pkg)
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to create threshd package '${pkg.name}'.`)
  }
}

export const updateThreshdPackage = async (name: string, pkg: ThreshdPackage): Promise<ValidationResult> => {
  try {
    await v2.put(`${threshdEndpoint}/packages/${encodeName(name)}`, pkg, ifMatchHeaders(pkg.version))
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to save threshd package '${name}'.`)
  }
}

export const deleteThreshdPackage = async (name: string, version?: string): Promise<ValidationResult> => {
  try {
    await v2.delete(`${threshdEndpoint}/packages/${encodeName(name)}`, ifMatchHeaders(version))
    return createSuccessResponse()
  } catch (error) {
    return failure(error, `Failed to delete threshd package '${name}'.`)
  }
}

export const reloadThreshdConfiguration = async (): Promise<ValidationResult> => {
  try {
    await v2.post(`${threshdEndpoint}/reload`)
    return createSuccessResponse()
  } catch (error) {
    return failure(error, 'Failed to reload the threshd configuration.')
  }
}

export const downloadThreshdConfiguration = async (isXml: boolean) => {
  try {
    return await v2.get(`${threshdEndpoint}/download`, {
      params: { format: isXml ? 'xml' : 'json' },
      responseType: 'blob'
    })
  } catch (error) {
    console.error('Failed to download the threshd configuration.', error)
    return false
  }
}

export const uploadThreshdConfiguration = async (file: File, isXml: boolean): Promise<ValidationResult> => {
  try {
    const formData = new FormData()
    formData.append('upload', file)
    await v2.post(`${threshdEndpoint}/upload${isXml ? '/xml' : ''}`, formData)
    return createSuccessResponse()
  } catch (error) {
    return failure(error, 'Failed to upload the threshd configuration.')
  }
}
