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

export interface ValidationResult {
  success: boolean
  message: string
  errors?: string[]
}

export interface ValidationResultWithPayload<T> extends ValidationResult {
  payload?: T
}

export const createSuccessResponse = (): ValidationResult => {
  return {
    success: true,
    message: ''
  } as ValidationResult
}

export const createFailureResult = (message: string): ValidationResult => {
  return {
    success: false,
    message
  } as ValidationResult
}

export const createResultWithPayload = <T>(success: boolean, message: string, payload?: T): ValidationResultWithPayload<T> => {
  return {
    success,
    message,
    payload
  } as ValidationResultWithPayload<T>
}
