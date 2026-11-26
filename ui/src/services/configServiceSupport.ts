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
import { createFailureResult, createResultWithPayload } from '@/types/validation'
import type { ValidationResult, ValidationResultWithPayload } from '@/types/validation'

// Error handling shared by the configuration services that write with If-Match (thresholds, XML collector).

/**
 * Pulls the server's own message out of an axios error so validation failures reach the user verbatim
 * ("ds-name ... is longer than 19 characters") instead of a generic "request failed".
 */
export const getErrorMessage = (error: unknown, fallbackMessage: string): string => {
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

export const failure = (error: unknown, fallbackMessage: string): ValidationResult => {
  console.error(fallbackMessage, error)
  const result = createFailureResult(getErrorMessage(error, fallbackMessage))
  // The stores tell a 412 apart: it means the copy they hold is stale, not that the change was invalid.
  if (axios.isAxiosError(error) && error.response) {
    result.status = error.response.status
  }
  return result
}

export const failureWithPayload = <T>(error: unknown, fallbackMessage: string): ValidationResultWithPayload<T> => {
  console.error(fallbackMessage, error)
  return createResultWithPayload<T>(false, getErrorMessage(error, fallbackMessage))
}

/** The ETag a GET returned, ready to be sent back as If-Match. */
export const ifMatchHeaders = (version?: string) => (version ? { headers: { 'If-Match': `"${version}"` }} : undefined)
