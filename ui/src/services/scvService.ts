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
import { SCVCredentials } from '@/types/scv'
import useSnackbar from '@/composables/useSnackbar'
import useSpinner from '@/composables/useSpinner'

const { showSnackBar } = useSnackbar()
const { startSpinner, stopSpinner } = useSpinner()
const endpoint = '/scv'

const getAliases = async (): Promise<string[]> => {
  try {
    startSpinner()
    const resp = await rest.get(endpoint)
    return resp.data
  } catch (err) {
    showSnackBar({ msg: 'Failed to return aliases.' })
    return []
  } finally {
    stopSpinner()
  }
}

const getCredentialsByAlias = async (alias: string): Promise<SCVCredentials | null> => {
  try {
    startSpinner()
    const resp = await rest.get(`${endpoint}/${alias}`)
    return resp.data
  } catch (err) {
    showSnackBar({ msg: 'Failed to retrieve credentials.' })
    return null
  } finally {
    stopSpinner()
  }
}

const addCredentials = async (credentials: SCVCredentials): Promise<number | null> => {
  try {
    startSpinner()
    const resp = await rest.post(endpoint, credentials)
    showSnackBar({ msg: 'Credentials added.' })
    return resp.status
  } catch (err) {
    showSnackBar({ msg: 'Failed to add credentials.' })
    return null
  } finally {
    stopSpinner()
  }
}

const updateCredentials = async (credentials: SCVCredentials): Promise<number | null> => {
  try {
    startSpinner()
    const resp = await rest.put(`${endpoint}/${credentials.alias}`, credentials)
    showSnackBar({ msg: 'Credentials updated.' })
    return resp.status
  } catch (err) {
    showSnackBar({ msg: 'Failed to update credentials.' })
    return null
  } finally {
    stopSpinner()
  }
}

export { getAliases, getCredentialsByAlias, addCredentials, updateCredentials }
