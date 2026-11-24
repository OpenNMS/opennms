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

import { SCV_GET_ALL_ALIAS } from '@/lib/constants'
import { SCVCredentials } from '@/types/scv'
import { createFailureResult, createResultWithPayload, createSuccessResponse, ValidationResult, ValidationResultWithPayload } from '@/types/validation'
import { rest } from './axiosInstances'

const endpoint = '/scv'

const getAliases = async (): Promise<ValidationResultWithPayload<string[]>> => {
  try {
    const resp = await rest.get(endpoint)
    return createResultWithPayload(true, '', resp.data as string[])
  } catch (_err) {
    return createResultWithPayload<string[]>(false, 'Failed to return aliases.')
  }
}

const getCredentialsByAlias = async (alias: string): Promise<ValidationResultWithPayload<SCVCredentials>> => {
  try {
    const resp = await rest.get(`${endpoint}/${alias}`)
    return createResultWithPayload(true, '', resp.data as SCVCredentials)
  } catch (_err) {
    return createResultWithPayload<SCVCredentials>(false, 'Failed to retrieve credentials.')
  }
}

const getAllCredentials = async (): Promise<ValidationResultWithPayload<SCVCredentials[]>> => {
  try {
    const resp = await rest.get(`${endpoint}/${SCV_GET_ALL_ALIAS}`)
    return createResultWithPayload(true, '', resp.data as SCVCredentials[])
  } catch (_err) {
    return createResultWithPayload<SCVCredentials[]>(false, 'Failed to retrieve credentials.')
  }
}

const addCredentials = async (credentials: SCVCredentials): Promise<ValidationResult> => {
  try {
    await rest.post(endpoint, credentials)
    return createSuccessResponse()
  } catch (_err) {
    return createFailureResult('Failed to add credentials.')
  }
}

const updateCredentials = async (credentials: SCVCredentials): Promise<ValidationResult> => {
  try {
    await rest.put(`${endpoint}/${credentials.alias}`, credentials)
    return createSuccessResponse()
  } catch (_err) {
    return createFailureResult('Failed to update credentials.')
  }
}

export { addCredentials, getAliases, getAllCredentials, getCredentialsByAlias, updateCredentials }
