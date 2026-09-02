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

import useSnackbar from '@/composables/useSnackbar'
import useSpinner from '@/composables/useSpinner'
import { WsmanConfig } from '@/types/wsmanAdmin'
import { v2 } from './axiosInstances'

// Manage WS-Man (NMS-20286): read-only view of wsman-config.xml through
// /api/v2/wsman-config. Editing is a later slice.

const { showSnackBar } = useSnackbar()
const { startSpinner, stopSpinner } = useSpinner()
const endpoint = '/wsman-config'

// null on failure (not an empty config) so the page can show an error state
const getWsmanConfig = async (): Promise<WsmanConfig | null> => {
  try {
    startSpinner()
    const resp = await v2.get(endpoint, { headers: { Accept: 'application/json' }})
    const data = resp.data
    if (!data || !data.defaults) {
      return null
    }
    return {
      defaults: data.defaults,
      definitions: Array.isArray(data.definitions) ? data.definitions : []
    }
  } catch (_err) {
    showSnackBar({ msg: 'Failed to load the WS-Man configuration.', error: true })
    return null
  } finally {
    stopSpinner()
  }
}

export { getWsmanConfig }
